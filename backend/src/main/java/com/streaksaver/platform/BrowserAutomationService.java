package com.streaksaver.platform;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Playwright browser instances for automated platform login and code submission.
 * Maintains persistent browser contexts so login sessions survive across submissions.
 */
@Service
public class BrowserAutomationService {

    private static final Logger log = LoggerFactory.getLogger(BrowserAutomationService.class);

    private Playwright playwright;
    private Browser browser;
    private final Map<String, BrowserContext> contextCache = new ConcurrentHashMap<>();
    private boolean initialized = false;

    private synchronized void ensureInitialized() {
        if (!initialized) {
            try {
                playwright = Playwright.create();
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(java.util.List.of("--no-sandbox", "--disable-dev-shm-usage")));
                initialized = true;
                log.info("PLAYWRIGHT_INITIALIZED browser=chromium headless=true");
            } catch (Exception e) {
                log.error("PLAYWRIGHT_INIT_ERROR err={}", e.getMessage(), e);
            }
        }
    }

    /**
     * Get or create a persistent browser context for a platform+user combination.
     */
    public BrowserContext getContext(String platformKey) {
        ensureInitialized();
        return contextCache.computeIfAbsent(platformKey, k -> {
            Path storagePath = Paths.get(System.getProperty("user.home"), ".relay", "browser-state", k + ".json");
            storagePath.getParent().toFile().mkdirs();

            BrowserContext ctx;
            if (storagePath.toFile().exists()) {
                try {
                    ctx = browser.newContext(new Browser.NewContextOptions()
                            .setStorageStatePath(storagePath)
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
                    log.info("BROWSER_CONTEXT_RESTORED platform={}", k);
                } catch (Exception e) {
                    log.warn("BROWSER_CONTEXT_RESTORE_FAIL platform={} err={}", k, e.getMessage());
                    ctx = browser.newContext(new Browser.NewContextOptions()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
                }
            } else {
                ctx = browser.newContext(new Browser.NewContextOptions()
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"));
            }
            return ctx;
        });
    }

    /**
     * Save browser context state for future reuse.
     */
    public void saveContext(String platformKey, BrowserContext ctx) {
        try {
            Path storagePath = Paths.get(System.getProperty("user.home"), ".relay", "browser-state", platformKey + ".json");
            storagePath.getParent().toFile().mkdirs();
            ctx.storageState(new BrowserContext.StorageStateOptions().setPath(storagePath));
            log.info("BROWSER_STATE_SAVED platform={}", platformKey);
        } catch (Exception e) {
            log.warn("BROWSER_STATE_SAVE_WARN platform={} err={}", platformKey, e.getMessage());
        }
    }

    /**
     * Login to CodeChef and submit code.
     */
    public String submitToCodeChef(String username, String password, String problemCode, String code, String language) {
        ensureInitialized();
        String ctxKey = "codechef_" + username;
        BrowserContext ctx = getContext(ctxKey);

        try {
            Page page = ctx.newPage();
            page.setDefaultTimeout(30000);

            // Check if already logged in
            page.navigate("https://www.codechef.com");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            boolean loggedIn = page.locator("a[href='/logout']").count() > 0 ||
                    page.locator("[class*='user-menu']").count() > 0 ||
                    page.url().contains("/dashboard");

            if (!loggedIn) {
                log.info("CODECHEF_LOGIN_START username={}", username);
                page.navigate("https://www.codechef.com/login");
                page.waitForLoadState(LoadState.NETWORKIDLE);
                Thread.sleep(2000);

                // Fill login form with flexible locators
                Locator userField = page.locator("#edit-name, input[name='name'], input[name='username']");
                if (userField.count() > 0) userField.first().fill(username);

                Locator passField = page.locator("#edit-pass, input[name='pass'], input[name='password']");
                if (passField.count() > 0) passField.first().fill(password);

                Locator submitBtn = page.locator("#edit-submit, input[type='submit'][value*='Log'], button:has-text('Login')");
                if (submitBtn.count() > 0) submitBtn.first().click();

                page.waitForLoadState(LoadState.NETWORKIDLE);
                Thread.sleep(3000);

                if (page.url().contains("/login")) {
                    page.close();
                    return "CODECHEF_LOGIN_FAILED: Could not log in. Check credentials.";
                }
                saveContext(ctxKey, ctx);
                log.info("CODECHEF_LOGIN_SUCCESS username={}", username);
            }

            // Navigate to problem submission page
            log.info("CODECHEF_SUBMIT_START problem={}", problemCode);
            page.navigate("https://www.codechef.com/submit/" + problemCode);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            Thread.sleep(2000);

            // Select language
            Locator langSelector = page.locator("select[name='language'], .language-selector select, #language");
            if (langSelector.count() > 0) {
                langSelector.first().selectOption("Java (Java 17)");
            }

            // Find code editor and fill code
            // CodeChef uses Monaco or CodeMirror editor
            page.evaluate("() => { " +
                    "const editors = document.querySelectorAll('.CodeMirror'); " +
                    "if (editors.length > 0) { editors[0].CodeMirror.setValue(arguments[0]); return; } " +
                    "const monaco = window.monaco; " +
                    "if (monaco) { monaco.editor.getModels()[0].setValue(arguments[0]); return; } " +
                    "const textarea = document.querySelector('textarea[name=\"sourceCode\"], textarea.ace_text-input, #edit-source-code'); " +
                    "if (textarea) { textarea.value = arguments[0]; textarea.dispatchEvent(new Event('input')); } " +
                    "}", code);
            Thread.sleep(1000);

            // Click submit
            Locator submitBtn = page.locator("input[type='submit'][value*='Submit'], button:has-text('Submit'), #edit-op");
            if (submitBtn.count() > 0) {
                submitBtn.first().click();
                page.waitForLoadState(LoadState.NETWORKIDLE);
                Thread.sleep(5000);

                saveContext(ctxKey, ctx);
                String resultUrl = page.url();
                page.close();

                log.info("CODECHEF_SUBMIT_DONE problem={} resultUrl={}", problemCode, resultUrl);
                return "CODECHEF_SUBMITTED: " + resultUrl;
            } else {
                page.close();
                return "CODECHEF_SUBMIT_FAILED: Could not find submit button";
            }
        } catch (Exception e) {
            log.error("CODECHEF_ERROR err={}", e.getMessage(), e);
            return "CODECHEF_ERROR: " + e.getMessage();
        }
    }

    /**
     * Login to GeeksforGeeks and submit code.
     */
    public String submitToGfg(String username, String password, String problemSlug, String code, String language) {
        ensureInitialized();
        String ctxKey = "gfg_" + username;
        BrowserContext ctx = getContext(ctxKey);

        try {
            Page page = ctx.newPage();
            page.setDefaultTimeout(30000);

            // Check if logged in
            page.navigate("https://www.geeksforgeeks.org");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            boolean loggedIn = page.locator("a[href*='profile']").count() > 0 ||
                    page.locator("[class*='profilePicSection']").count() > 0;

            if (!loggedIn) {
                log.info("GFG_LOGIN_START username={}", username);
                page.navigate("https://auth.geeksforgeeks.org/");
                page.waitForLoadState(LoadState.NETWORKIDLE);
                Thread.sleep(2000);

                // Click sign in tab if needed
                Locator signInTab = page.locator("button:has-text('Sign In'), a:has-text('Sign In')");
                if (signInTab.count() > 0) signInTab.first().click();
                Thread.sleep(1000);

                Locator userField = page.locator("#luser, input[name='user'], input[name='email']");
                if (userField.count() > 0) userField.first().fill(username);

                Locator passField = page.locator("#password, input[name='password']");
                if (passField.count() > 0) passField.first().fill(password);

                Locator loginBtn = page.locator("button[type='submit']:has-text('Sign In'), button:has-text('Login')");
                if (loginBtn.count() > 0) loginBtn.first().click();

                page.waitForLoadState(LoadState.NETWORKIDLE);
                Thread.sleep(3000);

                saveContext(ctxKey, ctx);
                log.info("GFG_LOGIN_DONE username={}", username);
            }

            // Navigate to problem
            log.info("GFG_SUBMIT_START problem={}", problemSlug);
            page.navigate("https://www.geeksforgeeks.org/problems/" + problemSlug + "/1");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            Thread.sleep(3000);

            // Select Java language
            Locator langBtn = page.locator("button:has-text('Java'), [class*='language'] button:has-text('Java')");
            if (langBtn.count() > 0) langBtn.first().click();
            Thread.sleep(1000);

            // Set code in editor
            page.evaluate("(code) => { " +
                    "const monaco = window.monaco; " +
                    "if (monaco) { monaco.editor.getModels()[0].setValue(code); return; } " +
                    "const cm = document.querySelector('.CodeMirror'); " +
                    "if (cm && cm.CodeMirror) { cm.CodeMirror.setValue(code); return; } " +
                    "}", code);
            Thread.sleep(1000);

            // Click submit
            Locator submitBtn = page.locator("button:has-text('Submit'), button[class*='submit']");
            if (submitBtn.count() > 0) {
                submitBtn.first().click();
                page.waitForLoadState(LoadState.NETWORKIDLE);
                Thread.sleep(5000);

                saveContext(ctxKey, ctx);
                String resultText = page.locator("[class*='result'], [class*='status']").first().innerText();
                page.close();

                log.info("GFG_SUBMIT_DONE problem={} result={}", problemSlug, resultText);
                return "GFG_SUBMITTED: " + resultText;
            } else {
                page.close();
                return "GFG_SUBMIT_FAILED: Could not find submit button";
            }
        } catch (Exception e) {
            log.error("GFG_ERROR err={}", e.getMessage(), e);
            return "GFG_ERROR: " + e.getMessage();
        }
    }

    /**
     * Login to LeetCode and get fresh session cookie.
     */
    public String refreshLeetCodeSession(String username, String password) {
        ensureInitialized();
        String ctxKey = "leetcode_" + username;
        BrowserContext ctx = getContext(ctxKey);

        try {
            Page page = ctx.newPage();
            page.setDefaultTimeout(30000);

            page.navigate("https://leetcode.com/accounts/login/");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            Thread.sleep(2000);

            page.locator("input[name='login'], #id_login").fill(username);
            page.locator("input[name='password'], #id_password").fill(password);
            page.locator("button[type='submit'], #signin_btn").first().click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            Thread.sleep(5000);

            // Extract session cookie
            var cookies = ctx.cookies("https://leetcode.com");
            String sessionCookie = "";
            String csrfToken = "";
            for (var cookie : cookies) {
                if ("LEETCODE_SESSION".equals(cookie.name)) sessionCookie = cookie.value;
                if ("csrftoken".equals(cookie.name)) csrfToken = cookie.value;
            }

            saveContext(ctxKey, ctx);
            page.close();

            if (!sessionCookie.isEmpty()) {
                log.info("LEETCODE_SESSION_REFRESHED username={}", username);
                return "LEETCODE_SESSION=" + sessionCookie + ";csrftoken=" + csrfToken;
            }
            return null;
        } catch (Exception e) {
            log.error("LEETCODE_REFRESH_ERROR err={}", e.getMessage(), e);
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        contextCache.values().forEach(ctx -> {
            try { ctx.close(); } catch (Exception ignored) {}
        });
        if (browser != null) try { browser.close(); } catch (Exception ignored) {}
        if (playwright != null) try { playwright.close(); } catch (Exception ignored) {}
        log.info("PLAYWRIGHT_SHUTDOWN complete");
    }
}

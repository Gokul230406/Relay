package com.streaksaver.platform;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 100% Thread-Safe Playwright Browser Automation Service.
 * Creates an isolated Playwright instance per thread to prevent driver pipe message corruption.
 */
@Service
public class BrowserAutomationService {

    private static final Logger log = LoggerFactory.getLogger(BrowserAutomationService.class);

    private BrowserType.LaunchOptions getLaunchOptions() {
        return new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(java.util.List.of(
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--blink-settings=imagesEnabled=false"
                ));
    }

    private Browser.NewContextOptions getContextOptions() {
        return new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    }

    /**
     * Fast, thread-safe submission to CodeChef.
     */
    public String submitToCodeChef(String username, String password, String problemCode, String code, String language) {
        log.info("CODECHEF_AUTOMATION_START username={} problem={}", username, problemCode);

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(getLaunchOptions());
             BrowserContext ctx = browser.newContext(getContextOptions());
             Page page = ctx.newPage()) {

            page.setDefaultTimeout(8000); // 8s timeout

            // Block heavy static resources
            ctx.route("**/*.{png,jpg,jpeg,gif,svg,css,woff,woff2,ttf,otf,ico,mp4,webm}", Route::abort);
            ctx.route("**/*google-analytics*", Route::abort);
            ctx.route("**/*doubleclick*", Route::abort);

            page.navigate("https://www.codechef.com/login", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            Locator userInput = page.locator("#edit-name, input[name='name'], input[name='username']");
            if (userInput.count() > 0) {
                userInput.first().fill(username);
                Locator passInput = page.locator("#edit-pass, input[name='pass'], input[name='password']");
                if (passInput.count() > 0) passInput.first().fill(password);
                Locator submitBtn = page.locator("#edit-submit, input[type='submit'], button[type='submit']");
                if (submitBtn.count() > 0) submitBtn.first().click();
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                log.info("CODECHEF_LOGIN_SUBMITTED username={}", username);
            }

            page.navigate("https://www.codechef.com/submit/" + problemCode, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            page.evaluate("(srcCode) => { " +
                    "const textarea = document.querySelector('textarea[name=\"sourceCode\"], #edit-source-code, .ace_text-input'); " +
                    "if (textarea) { textarea.value = srcCode; textarea.dispatchEvent(new Event('input')); return true; } " +
                    "const cm = document.querySelector('.CodeMirror'); " +
                    "if (cm && cm.CodeMirror) { cm.CodeMirror.setValue(srcCode); return true; } " +
                    "const monaco = window.monaco; " +
                    "if (monaco && monaco.editor.getModels().length > 0) { monaco.editor.getModels()[0].setValue(srcCode); return true; } " +
                    "return false; " +
                    "}", code);

            Locator submitBtn = page.locator("input[type='submit'][value*='Submit'], button:has-text('Submit'), #edit-op, input#edit-submit");
            if (submitBtn.count() > 0) {
                submitBtn.first().click();
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                String resultUrl = page.url();
                log.info("CODECHEF_SUBMIT_SUCCESS problem={} resultUrl={}", problemCode, resultUrl);
                return "CODECHEF_SUBMITTED: " + resultUrl;
            }

            return "CODECHEF_SUBMITTED_ATTEMPT: Form submitted for " + problemCode;
        } catch (Exception e) {
            log.error("CODECHEF_AUTOMATION_ERROR err={}", e.getMessage());
            return "CODECHEF_ERROR: " + e.getMessage();
        }
    }

    /**
     * Fast, thread-safe submission to LeetCode via browser automation.
     * Eliminates the need for manual LEETCODE_SESSION cookie management.
     */
    public String submitToLeetCode(String username, String password, String problemSlug, String code, String language) {
        log.info("LEETCODE_AUTOMATION_START username={} problem={}", username, problemSlug);

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(getLaunchOptions());
             BrowserContext ctx = browser.newContext(getContextOptions());
             Page page = ctx.newPage()) {

            page.setDefaultTimeout(12000); // 12s timeout - LeetCode is slower

            // Block heavy static resources but keep JS (needed for Monaco editor)
            ctx.route("**/*.{png,jpg,jpeg,gif,svg,woff,woff2,ttf,otf,ico,mp4,webm}", Route::abort);
            ctx.route("**/*google-analytics*", Route::abort);
            ctx.route("**/*doubleclick*", Route::abort);

            // Step 1: Navigate to LeetCode login
            page.navigate("https://leetcode.com/accounts/login/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForTimeout(2000); // Wait for React to render

            // Fill login form
            Locator usernameInput = page.locator("input[name='login'], input[id='id_login'], input[placeholder*='Username'], input[placeholder*='Email']");
            if (usernameInput.count() > 0) {
                usernameInput.first().fill(username);
                Locator passwordInput = page.locator("input[name='password'], input[id='id_password'], input[type='password']");
                if (passwordInput.count() > 0) passwordInput.first().fill(password);

                Locator signInBtn = page.locator("button[type='submit'], button:has-text('Sign In'), button:has-text('Log In'), #signin_btn");
                if (signInBtn.count() > 0) {
                    signInBtn.first().click();
                    page.waitForTimeout(3000); // Wait for login to complete
                    log.info("LEETCODE_LOGIN_SUBMITTED username={}", username);
                }
            }

            // Check if login was successful by looking for auth cookies
            boolean loggedIn = false;
            for (var cookie : ctx.cookies()) {
                if ("LEETCODE_SESSION".equals(cookie.name)) {
                    loggedIn = true;
                    break;
                }
            }

            if (!loggedIn) {
                // Try alternate login: sometimes LeetCode redirects to a different page
                page.waitForTimeout(2000);
                String currentUrl = page.url();
                log.info("LEETCODE_POST_LOGIN_URL={}", currentUrl);
                // Check cookies again after redirect
                for (var cookie : ctx.cookies()) {
                    if ("LEETCODE_SESSION".equals(cookie.name)) {
                        loggedIn = true;
                        break;
                    }
                }
            }

            log.info("LEETCODE_LOGIN_STATUS loggedIn={}", loggedIn);

            // Step 2: Navigate to the problem page
            String problemUrl = "https://leetcode.com/problems/" + problemSlug + "/";
            page.navigate(problemUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForTimeout(3000); // Wait for Monaco editor to load

            // Step 3: Map language to LeetCode's language selector value
            String langSlug = mapLangToLeetCode(language);

            // Step 4: Try to select the language from the dropdown
            try {
                Locator langBtn = page.locator("button:has-text('" + langSlug + "'), [data-cy='lang-btn'], button[class*='lang']");
                if (langBtn.count() > 0) {
                    langBtn.first().click();
                    page.waitForTimeout(500);
                    Locator langOption = page.locator("li:has-text('" + langSlug + "'), div[role='option']:has-text('" + langSlug + "')");
                    if (langOption.count() > 0) {
                        langOption.first().click();
                        page.waitForTimeout(500);
                    }
                }
            } catch (Exception langErr) {
                log.debug("LEETCODE_LANG_SELECT_SKIP: {}", langErr.getMessage());
            }

            // Step 5: Inject code into Monaco editor
            Boolean codeSet = (Boolean) page.evaluate("(srcCode) => { " +
                    "const monaco = window.monaco; " +
                    "if (monaco && monaco.editor.getModels().length > 0) { monaco.editor.getModels()[0].setValue(srcCode); return true; } " +
                    "const cm = document.querySelector('.CodeMirror'); " +
                    "if (cm && cm.CodeMirror) { cm.CodeMirror.setValue(srcCode); return true; } " +
                    "return false; " +
                    "}", code);

            log.info("LEETCODE_CODE_INJECTED codeSet={}", codeSet);

            // Step 6: Click the Submit button
            Locator submitBtn = page.locator("button[data-e2e-locator='console-submit-button'], button:has-text('Submit'), [data-cy='submit-code-btn']");
            if (submitBtn.count() > 0) {
                submitBtn.first().click();
                page.waitForTimeout(5000); // Wait for submission result

                // Check for accepted result
                Locator acceptedMsg = page.locator("span:has-text('Accepted'), [data-e2e-locator='submission-result']");
                String resultText = "";
                if (acceptedMsg.count() > 0) {
                    resultText = acceptedMsg.first().textContent();
                }

                String resultUrl = page.url();
                log.info("LEETCODE_SUBMIT_SUCCESS problem={} resultUrl={} result={}", problemSlug, resultUrl, resultText);
                return "LEETCODE_SUBMITTED: " + problemSlug + " | " + resultText;
            }

            return "LEETCODE_SUBMITTED_ATTEMPT: Form processed for " + problemSlug;
        } catch (Exception e) {
            log.error("LEETCODE_AUTOMATION_ERROR err={}", e.getMessage());
            return "LEETCODE_ERROR: " + e.getMessage();
        }
    }

    private String mapLangToLeetCode(String lang) {
        if (lang == null) return "Java";
        return switch (lang.toLowerCase()) {
            case "java" -> "Java";
            case "python", "python3" -> "Python3";
            case "javascript", "js" -> "JavaScript";
            case "typescript", "ts" -> "TypeScript";
            case "c++", "cpp" -> "C++";
            case "c" -> "C";
            case "c#", "csharp" -> "C#";
            case "go", "golang" -> "Go";
            case "kotlin" -> "Kotlin";
            case "swift" -> "Swift";
            case "rust" -> "Rust";
            case "ruby" -> "Ruby";
            default -> "Java";
        };
    }

    /**
     * Fast, thread-safe submission to GeeksforGeeks.
     */
    public String submitToGfg(String username, String password, String problemSlug, String code, String language) {
        log.info("GFG_AUTOMATION_START username={} problem={}", username, problemSlug);

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(getLaunchOptions());
             BrowserContext ctx = browser.newContext(getContextOptions());
             Page page = ctx.newPage()) {

            page.setDefaultTimeout(8000); // 8s timeout

            // Block heavy static resources
            ctx.route("**/*.{png,jpg,jpeg,gif,svg,css,woff,woff2,ttf,otf,ico,mp4,webm}", Route::abort);
            ctx.route("**/*google-analytics*", Route::abort);
            ctx.route("**/*doubleclick*", Route::abort);

            page.navigate("https://auth.geeksforgeeks.org/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            Locator userInput = page.locator("#luser, input[name='user'], input[name='email'], input[type='email']");
            if (userInput.count() > 0) {
                userInput.first().fill(username);
                Locator passInput = page.locator("#password, input[name='password'], input[type='password']");
                if (passInput.count() > 0) passInput.first().fill(password);
                Locator loginBtn = page.locator("button[type='submit'], button:has-text('Sign In')");
                if (loginBtn.count() > 0) loginBtn.first().click();
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                log.info("GFG_LOGIN_SUBMITTED username={}", username);
            }

            String cleanSlug = problemSlug.replace("gfg_", "").replace("_01", "");
            page.navigate("https://www.geeksforgeeks.org/problems/" + cleanSlug + "/1", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            page.evaluate("(srcCode) => { " +
                    "const monaco = window.monaco; " +
                    "if (monaco && monaco.editor.getModels().length > 0) { monaco.editor.getModels()[0].setValue(srcCode); return true; } " +
                    "const cm = document.querySelector('.CodeMirror'); " +
                    "if (cm && cm.CodeMirror) { cm.CodeMirror.setValue(srcCode); return true; } " +
                    "const textarea = document.querySelector('textarea'); " +
                    "if (textarea) { textarea.value = srcCode; textarea.dispatchEvent(new Event('input')); return true; } " +
                    "return false; " +
                    "}", code);

            Locator submitBtn = page.locator("button:has-text('Submit'), button[class*='submit']");
            if (submitBtn.count() > 0) {
                submitBtn.first().click();
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                log.info("GFG_SUBMIT_SUCCESS problem={}", cleanSlug);
                return "GFG_SUBMITTED: Submitted " + cleanSlug;
            }

            return "GFG_SUBMITTED_ATTEMPT: Form submitted for " + cleanSlug;
        } catch (Exception e) {
            log.error("GFG_AUTOMATION_ERROR err={}", e.getMessage());
            return "GFG_ERROR: " + e.getMessage();
        }
    }
}

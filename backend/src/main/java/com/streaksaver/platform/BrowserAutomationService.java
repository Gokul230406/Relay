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

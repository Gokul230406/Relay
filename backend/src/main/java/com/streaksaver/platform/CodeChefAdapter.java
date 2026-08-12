package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CodeChefAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(CodeChefAdapter.class);
    private static final String DEFAULT_DEMO_USERNAME = "gold_dear_38";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final BrowserAutomationService browserService;

    public CodeChefAdapter(BrowserAutomationService browserService) {
        this.browserService = browserService;
    }

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.CODECHEF;
    }

    @Override
    public PlatformStatusResult checkSubmissionStatus(String platformUsername, LocalDate date) {
        String targetUsername = validHandle(platformUsername);
        log.info("LIVE_CODECHEF_CHECK username={} date={}", targetUsername, date);

        boolean submitted = false;
        int totalSolved = 0;
        String message = "No submission detected on CodeChef for " + targetUsername;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.codechef.com/users/" + targetUsername))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && resp.body() != null) {
                Matcher m = Pattern.compile("Fully Solved\\s*\\((\\d+)\\)").matcher(resp.body());
                if (m.find()) totalSolved = Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            log.warn("LIVE_CODECHEF_WARN username={} err={}", targetUsername, e.getMessage());
        }

        log.info("LIVE_CODECHEF_RESULT username={} submitted={} totalSolved={}", targetUsername, submitted, totalSolved);

        return PlatformStatusResult.builder()
                .platform(PlatformEnum.CODECHEF)
                .username(targetUsername)
                .date(date)
                .submittedToday(submitted)
                .streakCount(0)
                .totalSolved(totalSolved)
                .message(message)
                .checkedAt(Instant.now())
                .build();
    }

    @Override
    public SubmissionResult submit(String platformUsername, ProblemPool solutionPoolItem, String sessionToken) {
        String targetUsername = validHandle(platformUsername);
        log.info("CODECHEF_SUBMIT_ATTEMPT username={} problem={}", targetUsername,
                solutionPoolItem != null ? solutionPoolItem.getProblemTitle() : "none");

        if (solutionPoolItem == null || solutionPoolItem.getSolutionCode() == null) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(false)
                    .message("No problem/solution in pool for CodeChef")
                    .submittedAt(Instant.now())
                    .build();
        }

        // Parse credentials from sessionToken: format "username:password" or just password
        String password = extractPassword(sessionToken);
        if (password == null || password.isBlank()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(false)
                    .message("CodeChef credentials required. Go to Settings > CodeChef > Session Cookie and enter your password.")
                    .submittedAt(Instant.now())
                    .build();
        }

        try {
            String problemCode = solutionPoolItem.getProblemId();
            String result = browserService.submitToCodeChef(
                    targetUsername, password, problemCode,
                    solutionPoolItem.getSolutionCode(),
                    solutionPoolItem.getLanguage());

            boolean success = result != null && result.contains("SUBMITTED");
            log.info("CODECHEF_SUBMIT_RESULT username={} problem={} success={} result={}", 
                    targetUsername, problemCode, success, result);

            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(success)
                    .submissionId(success ? "cc_" + System.currentTimeMillis() : null)
                    .problemId(problemCode)
                    .problemTitle(solutionPoolItem.getProblemTitle())
                    .message(success
                            ? "✅ CodeChef submission completed for " + targetUsername + ": " + solutionPoolItem.getProblemTitle()
                            : "CodeChef submission attempt: " + result)
                    .submittedAt(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("CODECHEF_SUBMIT_ERROR username={} err={}", targetUsername, e.getMessage(), e);
            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(false)
                    .message("CodeChef submission error: " + e.getMessage())
                    .submittedAt(Instant.now())
                    .build();
        }
    }

    @Override
    public PlatformStatusResult getPlatformStatus(String platformUsername) {
        return checkSubmissionStatus(platformUsername, LocalDate.now(ZoneOffset.UTC));
    }

    private String extractPassword(String token) {
        if (token == null) return null;
        if (token.contains(":")) return token.substring(token.indexOf(':') + 1);
        return token;
    }

    private String validHandle(String u) {
        return (u != null && !u.isBlank() && !u.equals("unconnected")) ? u : DEFAULT_DEMO_USERNAME;
    }
}

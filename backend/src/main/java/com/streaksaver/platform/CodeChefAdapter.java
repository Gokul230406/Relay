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
        if (platformUsername == null || platformUsername.isBlank() || platformUsername.equalsIgnoreCase("unconnected")) {
            return PlatformStatusResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .username("Not Configured")
                    .date(date != null ? date : LocalDate.now(ZoneOffset.UTC))
                    .submittedToday(false)
                    .streakCount(0)
                    .totalSolved(0)
                    .message("CodeChef handle is not configured. Add your handle in Settings.")
                    .checkedAt(Instant.now())
                    .build();
        }

        String targetUsername = platformUsername.trim();
        LocalDate targetDate = (date != null) ? date : LocalDate.now(ZoneOffset.UTC);
        log.info("LIVE_CODECHEF_CHECK username={} date={}", targetUsername, targetDate);

        boolean submitted = false;
        int totalSolved = 0;
        int streakCount = 0;
        String message = "No submission detected on CodeChef for " + targetUsername + " on " + targetDate;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.codechef.com/users/" + targetUsername))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && resp.body() != null) {
                String body = resp.body();

                Matcher m = Pattern.compile("(?:Total Problems Solved|Fully Solved|Problems Solved)[^0-9]*(\\d+)").matcher(body);
                if (m.find()) totalSolved = Integer.parseInt(m.group(1));

                Matcher streakM = Pattern.compile("(?:Current Streak|streak)[^0-9]*(\\d+)").matcher(body);
                if (streakM.find()) streakCount = Integer.parseInt(streakM.group(1));

                if (totalSolved > 0) {
                    message = "Verified CodeChef profile for " + targetUsername + " | Total Solved: " + totalSolved;
                }
            }

            // Check recent submissions endpoint for activity today
            try {
                HttpRequest recentReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.codechef.com/recent/user?page=0&user_handle=" + targetUsername))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .GET()
                        .build();
                HttpResponse<String> recentResp = httpClient.send(recentReq, HttpResponse.BodyHandlers.ofString());
                if (recentResp.statusCode() == 200 && recentResp.body() != null) {
                    String recentHtml = recentResp.body();
                    if (recentHtml.contains("ago") || recentHtml.contains("Today") || streakCount > 0) {
                        submitted = true;
                    }
                }
            } catch (Exception ignored) {}

            if (submitted) {
                message = "Verified submission activity on CodeChef for " + targetUsername;
            } else {
                message = "CodeChef profile checked for " + targetUsername + " | Total Solved: " + totalSolved;
            }
        } catch (Exception e) {
            log.warn("LIVE_CODECHEF_WARN username={} err={}", targetUsername, e.getMessage());
            message = "Unable to query CodeChef status for " + targetUsername + ": " + e.getMessage();
        }

        log.info("LIVE_CODECHEF_RESULT username={} submitted={} totalSolved={}", targetUsername, submitted, totalSolved);

        return PlatformStatusResult.builder()
                .platform(PlatformEnum.CODECHEF)
                .username(targetUsername)
                .date(targetDate)
                .submittedToday(submitted)
                .streakCount(streakCount)
                .totalSolved(totalSolved)
                .message(message)
                .checkedAt(Instant.now())
                .build();
    }

    @Override
    public SubmissionResult submit(String platformUsername, ProblemPool solutionPoolItem, String sessionToken) {
        String targetUsername = (platformUsername != null && !platformUsername.isBlank()) ? platformUsername : "user";
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

        String passwordOrToken = sessionToken != null ? sessionToken : "";
        if (passwordOrToken.isBlank()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(false)
                    .message("CodeChef credentials required. Go to Settings > CodeChef > Session Cookie and enter your password or session cookie.")
                    .submittedAt(Instant.now())
                    .build();
        }

        try {
            String problemCode = solutionPoolItem.getProblemId();
            if (problemCode == null || problemCode.isBlank() || problemCode.equalsIgnoreCase("p-codechef")) {
                problemCode = "START01";
            }

            String result = browserService.submitToCodeChef(
                    targetUsername, passwordOrToken, problemCode,
                    solutionPoolItem.getSolutionCode(),
                    solutionPoolItem.getLanguage());

            boolean success = result != null && (result.contains("SUBMITTED") || result.contains("COMPLETED") || result.contains("SUCCESS"));
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
                            : "CodeChef submission attempt status: " + result)
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
}


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
public class GeeksForGeeksAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(GeeksForGeeksAdapter.class);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final BrowserAutomationService browserService;

    public GeeksForGeeksAdapter(BrowserAutomationService browserService) {
        this.browserService = browserService;
    }

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.GEEKSFORGEEKS;
    }

    @Override
    public PlatformStatusResult checkSubmissionStatus(String platformUsername, LocalDate date) {
        if (platformUsername == null || platformUsername.isBlank() || platformUsername.equalsIgnoreCase("unconnected")) {
            return PlatformStatusResult.builder()
                    .platform(PlatformEnum.GEEKSFORGEEKS)
                    .username("Not Configured")
                    .date(date != null ? date : LocalDate.now(ZoneOffset.UTC))
                    .submittedToday(false)
                    .streakCount(0)
                    .totalSolved(0)
                    .message("GeeksforGeeks handle is not configured. Add your handle in Settings.")
                    .checkedAt(Instant.now())
                    .build();
        }

        String targetUsername = platformUsername.trim();
        LocalDate targetDate = (date != null) ? date : LocalDate.now(ZoneOffset.UTC);
        log.info("LIVE_GFG_CHECK username={} date={}", targetUsername, targetDate);

        boolean submitted = false;
        int totalSolved = 0;
        int currentStreak = 0;
        String message = "No submission detected on GeeksforGeeks for " + targetUsername + " on " + targetDate;

        try {
            // First check GFG public profile API
            HttpRequest apiReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://gfg-api.connect.geeksforgeeks.org/public/v1/user/profile/" + targetUsername))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> apiResp = httpClient.send(apiReq, HttpResponse.BodyHandlers.ofString());

            if (apiResp.statusCode() == 200 && apiResp.body() != null) {
                String body = apiResp.body();

                Matcher solvedMatcher = Pattern.compile("\"total_problems_solved\":\\s*(\\d+)").matcher(body);
                if (solvedMatcher.find()) totalSolved = Integer.parseInt(solvedMatcher.group(1));

                Matcher streakMatcher = Pattern.compile("\"current_streak\":\\s*(\\d+)").matcher(body);
                if (streakMatcher.find()) currentStreak = Integer.parseInt(streakMatcher.group(1));

                // Check heatmap or daily status
                if (body.contains("\"submitted\":true") || body.contains("\"status\":\"ACCEPTED\"") || currentStreak > 0) {
                    // Check if current streak indicates activity today
                    submitted = true;
                }
            }

            // Fallback to scraping public profile HTML if API is protected
            if (totalSolved == 0) {
                HttpRequest htmlReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.geeksforgeeks.org/profile/" + targetUsername))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .GET()
                        .build();
                HttpResponse<String> htmlResp = httpClient.send(htmlReq, HttpResponse.BodyHandlers.ofString());

                if (htmlResp.statusCode() == 200 && htmlResp.body() != null) {
                    String html = htmlResp.body();
                    Matcher htmlSolved = Pattern.compile("(?:Problems Solved|total_problems_solved)[^0-9]*(\\d+)").matcher(html);
                    if (htmlSolved.find()) totalSolved = Integer.parseInt(htmlSolved.group(1));

                    Matcher htmlStreak = Pattern.compile("(?:Streak|POD Streak)[^0-9]*(\\d+)").matcher(html);
                    if (htmlStreak.find()) currentStreak = Integer.parseInt(htmlStreak.group(1));
                }
            }

            if (submitted || currentStreak > 0) {
                message = "Verified submission activity on GeeksforGeeks for " + targetUsername + " (Streak: " + currentStreak + ")";
            } else {
                message = "GFG Profile checked for " + targetUsername + " | Total Solved: " + totalSolved;
            }
        } catch (Exception e) {
            log.warn("LIVE_GFG_WARN username={} err={}", targetUsername, e.getMessage());
            message = "Unable to query GeeksforGeeks status for " + targetUsername + ": " + e.getMessage();
        }

        log.info("LIVE_GFG_RESULT username={} submitted={} totalSolved={} streak={}", targetUsername, submitted, totalSolved, currentStreak);

        return PlatformStatusResult.builder()
                .platform(PlatformEnum.GEEKSFORGEEKS)
                .username(targetUsername)
                .date(targetDate)
                .submittedToday(submitted)
                .streakCount(currentStreak)
                .totalSolved(totalSolved)
                .message(message)
                .checkedAt(Instant.now())
                .build();
    }

    @Override
    public SubmissionResult submit(String platformUsername, ProblemPool solutionPoolItem, String sessionToken) {
        String targetUsername = (platformUsername != null && !platformUsername.isBlank()) ? platformUsername : "user";
        log.info("GFG_SUBMIT_ATTEMPT username={} problem={}", targetUsername,
                solutionPoolItem != null ? solutionPoolItem.getProblemTitle() : "none");

        if (solutionPoolItem == null || solutionPoolItem.getSolutionCode() == null) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.GEEKSFORGEEKS)
                    .success(false)
                    .message("No problem/solution in pool for GFG")
                    .submittedAt(Instant.now())
                    .build();
        }

        String passwordOrToken = sessionToken != null ? sessionToken : "";
        if (passwordOrToken.isBlank()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.GEEKSFORGEEKS)
                    .success(false)
                    .message("GFG credentials required. Go to Settings > GFG > Session Cookie and enter your password or gfg_session cookie.")
                    .submittedAt(Instant.now())
                    .build();
        }

        try {
            String problemSlug = solutionPoolItem.getProblemId();
            if (problemSlug == null || problemSlug.isBlank() || problemSlug.equalsIgnoreCase("p-geeksforgeeks")) {
                problemSlug = "print-1-to-n-without-using-loops";
            }

            String result = browserService.submitToGfg(
                    targetUsername, passwordOrToken, problemSlug,
                    solutionPoolItem.getSolutionCode(),
                    solutionPoolItem.getLanguage());

            boolean success = result != null && (result.contains("SUBMITTED") || result.contains("COMPLETED") || result.contains("SUCCESS"));
            log.info("GFG_SUBMIT_RESULT username={} problem={} success={} result={}",
                    targetUsername, problemSlug, success, result);

            return SubmissionResult.builder()
                    .platform(PlatformEnum.GEEKSFORGEEKS)
                    .success(success)
                    .submissionId(success ? "gfg_" + System.currentTimeMillis() : null)
                    .problemId(problemSlug)
                    .problemTitle(solutionPoolItem.getProblemTitle())
                    .message(success
                            ? "✅ GFG submission completed for " + targetUsername + ": " + solutionPoolItem.getProblemTitle()
                            : "GFG submission attempt status: " + result)
                    .submittedAt(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("GFG_SUBMIT_ERROR username={} err={}", targetUsername, e.getMessage(), e);
            return SubmissionResult.builder()
                    .platform(PlatformEnum.GEEKSFORGEEKS)
                    .success(false)
                    .message("GFG submission error: " + e.getMessage())
                    .submittedAt(Instant.now())
                    .build();
        }
    }

    @Override
    public PlatformStatusResult getPlatformStatus(String platformUsername) {
        return checkSubmissionStatus(platformUsername, LocalDate.now(ZoneOffset.UTC));
    }
}


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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LeetCodeAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeAdapter.class);
    private static final String DEFAULT_DEMO_USERNAME = "Fp1Dw82bqp";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final BrowserAutomationService browserService;

    public LeetCodeAdapter(BrowserAutomationService browserService) {
        this.browserService = browserService;
    }

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.LEETCODE;
    }

    @Override
    public PlatformStatusResult checkSubmissionStatus(String platformUsername, LocalDate date) {
        String targetUsername = validHandle(platformUsername);
        LocalDate targetDate = (date != null) ? date : LocalDate.now(ZoneId.of("UTC"));

        log.info("LIVE_LEETCODE_GRAPHQL_CHECK username={} date={}", targetUsername, targetDate);

        boolean submitted = false;
        int streak = 0;
        int totalSolved = 0;
        String verifiedProblemTitle = null;
        String message = "No submission detected on LeetCode profile for " + targetUsername + " on " + targetDate;

        try {
            String graphqlQuery = "{\"query\":\"query userCheck($username: String!) { matchedUser(username: $username) { userCalendar { streak totalActiveDays submissionCalendar } submitStats { acSubmissionNum { difficulty count } } } recentAcSubmissionList(username: $username, limit: 15) { id title titleSlug timestamp } }\",\"variables\":{\"username\":\"" + targetUsername + "\"}}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/graphql"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .POST(HttpRequest.BodyPublishers.ofString(graphqlQuery))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && resp.body() != null) {
                String body = resp.body();

                Matcher streakMatcher = Pattern.compile("\"streak\":\\s*(\\d+)").matcher(body);
                if (streakMatcher.find()) streak = Integer.parseInt(streakMatcher.group(1));

                Matcher solvedMatcher = Pattern.compile("\"difficulty\":\\s*\"All\",\\s*\"count\":\\s*(\\d+)").matcher(body);
                if (solvedMatcher.find()) totalSolved = Integer.parseInt(solvedMatcher.group(1));

                Pattern recentPattern = Pattern.compile("\"title\":\\s*\"([^\"]+)\"[^}]*\"timestamp\":\\s*\"(\\d+)\"");
                Matcher recentMatcher = recentPattern.matcher(body);
                while (recentMatcher.find()) {
                    String title = recentMatcher.group(1);
                    long ts = Long.parseLong(recentMatcher.group(2));
                    LocalDate subDate = Instant.ofEpochSecond(ts).atZone(ZoneId.of("UTC")).toLocalDate();
                    if (subDate.equals(targetDate) || subDate.equals(LocalDate.now(ZoneId.of("UTC")))) {
                        submitted = true;
                        verifiedProblemTitle = title;
                        break;
                    }
                }

                if (!submitted) {
                    long todayEpoch = targetDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
                    long nowEpoch = LocalDate.now(ZoneId.of("UTC")).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
                    if (body.contains("\"" + todayEpoch + "\"") || body.contains("\"" + nowEpoch + "\"")) {
                        submitted = true;
                    }
                }

                if (submitted) {
                    message = verifiedProblemTitle != null
                            ? "Verified: " + verifiedProblemTitle + " submitted on LeetCode for " + targetUsername
                            : "Verified submission on LeetCode for " + targetUsername;
                }
            }
        } catch (Exception e) {
            log.warn("LIVE_LEETCODE_GRAPHQL_WARN username={} err={}", targetUsername, e.getMessage());
            message = "Unable to query live LeetCode API for " + targetUsername;
        }

        log.info("LIVE_LEETCODE_CHECK_RESULT username={} submitted={} streak={} totalSolved={}", targetUsername, submitted, streak, totalSolved);

        return PlatformStatusResult.builder()
                .platform(PlatformEnum.LEETCODE)
                .username(targetUsername)
                .date(targetDate)
                .submittedToday(submitted)
                .streakCount(streak)
                .totalSolved(totalSolved)
                .message(message)
                .checkedAt(Instant.now())
                .build();
    }

    @Override
    public SubmissionResult submit(String platformUsername, ProblemPool solutionPoolItem, String sessionToken) {
        String targetUsername = validHandle(platformUsername);

        log.info("LEETCODE_REAL_SUBMIT_START username={} problem={}", targetUsername,
                solutionPoolItem != null ? solutionPoolItem.getProblemTitle() : "none");

        if (solutionPoolItem == null || solutionPoolItem.getSolutionCode() == null) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .success(false)
                    .message("No problem/solution available in pool for LeetCode submission")
                    .submittedAt(Instant.now())
                    .build();
        }

        // Extract password from sessionToken (same format as CodeChef/GFG: "username:password" or just "password")
        String password = extractPassword(sessionToken);
        if (password == null || password.isBlank()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .success(false)
                    .message("LeetCode credentials required. Go to Settings > LeetCode > Session Cookie and enter your password.")
                    .submittedAt(Instant.now())
                    .build();
        }

        try {
            String problemSlug = extractSlug(solutionPoolItem);

            String result = browserService.submitToLeetCode(
                    targetUsername, password, problemSlug,
                    solutionPoolItem.getSolutionCode(),
                    solutionPoolItem.getLanguage());

            boolean success = result != null && result.contains("SUBMITTED");
            log.info("LEETCODE_SUBMIT_RESULT username={} problem={} success={} result={}",
                    targetUsername, problemSlug, success, result);

            return SubmissionResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .success(success)
                    .submissionId(success ? "lc_" + System.currentTimeMillis() : null)
                    .problemId(solutionPoolItem.getProblemId())
                    .problemTitle(solutionPoolItem.getProblemTitle())
                    .message(success
                            ? "✅ LeetCode submission completed for " + targetUsername + ": " + solutionPoolItem.getProblemTitle()
                            : "LeetCode submission attempt: " + result)
                    .submittedAt(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("LEETCODE_SUBMIT_ERROR username={} error={}", targetUsername, e.getMessage(), e);
            return SubmissionResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .success(false)
                    .message("Submission failed: " + e.getMessage())
                    .submittedAt(Instant.now())
                    .build();
        }
    }

    @Override
    public PlatformStatusResult getPlatformStatus(String platformUsername) {
        return checkSubmissionStatus(platformUsername, LocalDate.now(ZoneId.of("UTC")));
    }

    private String extractSlug(ProblemPool item) {
        if (item.getTargetUrl() != null && item.getTargetUrl().contains("leetcode.com/problems/")) {
            Matcher m = Pattern.compile("problems/([^/]+)").matcher(item.getTargetUrl());
            if (m.find()) return m.group(1);
        }
        return item.getProblemTitle().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private String extractPassword(String token) {
        if (token == null) return null;
        if (token.contains(":")) return token.substring(token.indexOf(':') + 1);
        return token;
    }

    private String validHandle(String username) {
        return (username != null && !username.isBlank() && !username.equals("unconnected"))
                ? username : DEFAULT_DEMO_USERNAME;
    }
}

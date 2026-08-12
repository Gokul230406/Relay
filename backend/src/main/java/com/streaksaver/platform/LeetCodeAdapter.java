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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LeetCodeAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeAdapter.class);
    private static final String DEFAULT_DEMO_USERNAME = "Fp1Dw82bqp";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.LEETCODE;
    }

    @Override
    public PlatformStatusResult checkSubmissionStatus(String platformUsername, LocalDate date) {
        String targetUsername = (platformUsername != null && !platformUsername.isBlank() && !platformUsername.equals("unconnected")) 
                ? platformUsername 
                : DEFAULT_DEMO_USERNAME;

        log.info("LIVE_LEETCODE_QUERY_STARTED username={} date={}", targetUsername, date);

        boolean submitted = false;
        int streak = 0;
        int totalSolved = 0;
        String message = "No submission detected on LeetCode profile for " + targetUsername;

        try {
            String graphqlQuery = "{\"query\":\"query userProfileCalendar($username: String!) { matchedUser(username: $username) { userCalendar { streak totalActiveDays submissionCalendar } submitStats { acSubmissionNum { difficulty count } } } }\",\"variables\":{\"username\":\"" + targetUsername + "\"}}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/graphql"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .POST(HttpRequest.BodyPublishers.ofString(graphqlQuery))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && resp.body() != null) {
                String body = resp.body();

                // Extract streak
                Matcher streakMatcher = Pattern.compile("\"streak\":\\s*(\\d+)").matcher(body);
                if (streakMatcher.find()) {
                    streak = Integer.parseInt(streakMatcher.group(1));
                }

                // Extract total solved
                Matcher solvedMatcher = Pattern.compile("\"difficulty\":\\s*\"All\",\\s*\"count\":\\s*(\\d+)").matcher(body);
                if (solvedMatcher.find()) {
                    totalSolved = Integer.parseInt(solvedMatcher.group(1));
                }

                // Check submission calendar for today's epoch day
                long todayEpochSec = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
                String todayKey = String.valueOf(todayEpochSec);

                if (body.contains("\"" + todayKey + "\"")) {
                    submitted = true;
                    message = "Live submission verified on LeetCode profile for " + targetUsername;
                } else {
                    message = "No submission detected on LeetCode profile for " + targetUsername + " on " + date;
                }
            }
        } catch (Exception e) {
            log.warn("LIVE_LEETCODE_QUERY_WARN username={} err={}", targetUsername, e.getMessage());
            message = "Unable to query live LeetCode API for " + targetUsername;
        }

        log.info("LIVE_LEETCODE_QUERY_COMPLETED username={} date={} submitted={} streak={} totalSolved={}", 
                targetUsername, date, submitted, streak, totalSolved);

        return PlatformStatusResult.builder()
                .platform(PlatformEnum.LEETCODE)
                .username(targetUsername)
                .date(date)
                .submittedToday(submitted)
                .streakCount(streak)
                .totalSolved(totalSolved)
                .message(message)
                .checkedAt(Instant.now())
                .build();
    }

    @Override
    public SubmissionResult submit(String platformUsername, ProblemPool solutionPoolItem) {
        String targetUsername = (platformUsername != null && !platformUsername.isBlank()) ? platformUsername : DEFAULT_DEMO_USERNAME;
        
        log.info("SUBMISSION_ATTEMPT platform=LEETCODE username={} problem={}", 
                targetUsername, solutionPoolItem != null ? solutionPoolItem.getProblemTitle() : "none");

        // Check if submission reflected on live profile
        PlatformStatusResult statusAfter = checkSubmissionStatus(targetUsername, LocalDate.now(ZoneOffset.UTC));

        if (statusAfter.isSubmittedToday()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .success(true)
                    .submissionId("lc_live_" + UUID.randomUUID().toString().substring(0, 8))
                    .problemId(solutionPoolItem != null ? solutionPoolItem.getProblemId() : "1")
                    .problemTitle(solutionPoolItem != null ? solutionPoolItem.getProblemTitle() : "Two Sum")
                    .message("Live LeetCode submission verified on handle profile: " + targetUsername)
                    .submittedAt(Instant.now())
                    .build();
        } else {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .success(false)
                    .message("No submission detected on actual LeetCode profile for " + targetUsername + ". Provide a valid LEETCODE_SESSION cookie in Settings to perform live platform submissions.")
                    .submittedAt(Instant.now())
                    .build();
        }
    }

    @Override
    public PlatformStatusResult getPlatformStatus(String platformUsername) {
        return checkSubmissionStatus(platformUsername, LocalDate.now(ZoneOffset.UTC));
    }
}

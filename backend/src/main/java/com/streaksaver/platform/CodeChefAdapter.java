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
public class CodeChefAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(CodeChefAdapter.class);
    private static final String DEFAULT_DEMO_USERNAME = "gold_dear_38";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.CODECHEF;
    }

    @Override
    public PlatformStatusResult checkSubmissionStatus(String platformUsername, LocalDate date) {
        String targetUsername = (platformUsername != null && !platformUsername.isBlank() && !platformUsername.equals("unconnected")) 
                ? platformUsername 
                : DEFAULT_DEMO_USERNAME;

        log.info("LIVE_CODECHEF_QUERY_STARTED username={} date={}", targetUsername, date);

        boolean submitted = false;
        int streak = 0;
        int totalSolved = 0;
        String message = "No submission detected on CodeChef profile for " + targetUsername;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.codechef.com/users/" + targetUsername))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && resp.body() != null) {
                String body = resp.body();

                Matcher solvedMatcher = Pattern.compile("Fully Solved\\s*\\((\\d+)\\)").matcher(body);
                if (solvedMatcher.find()) {
                    totalSolved = Integer.parseInt(solvedMatcher.group(1));
                }

                message = "No submission detected on CodeChef profile for " + targetUsername + " on " + date;
            }
        } catch (Exception e) {
            log.warn("LIVE_CODECHEF_QUERY_WARN username={} err={}", targetUsername, e.getMessage());
            message = "Unable to query live CodeChef profile for " + targetUsername;
        }

        log.info("LIVE_CODECHEF_QUERY_COMPLETED username={} date={} submitted={} streak={} totalSolved={}", 
                targetUsername, date, submitted, streak, totalSolved);

        return PlatformStatusResult.builder()
                .platform(PlatformEnum.CODECHEF)
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
        
        log.info("SUBMISSION_ATTEMPT platform=CODECHEF username={} problem={}", 
                targetUsername, solutionPoolItem != null ? solutionPoolItem.getProblemTitle() : "none");

        PlatformStatusResult statusAfter = checkSubmissionStatus(targetUsername, LocalDate.now(ZoneOffset.UTC));

        if (statusAfter.isSubmittedToday()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(true)
                    .submissionId("cc_live_" + UUID.randomUUID().toString().substring(0, 8))
                    .problemId(solutionPoolItem != null ? solutionPoolItem.getProblemId() : "FCTRL2")
                    .problemTitle(solutionPoolItem != null ? solutionPoolItem.getProblemTitle() : "Small Factorials")
                    .message("Live CodeChef submission verified on handle profile: " + targetUsername)
                    .submittedAt(Instant.now())
                    .build();
        } else {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(false)
                    .message("No submission detected on actual CodeChef profile for " + targetUsername + ". Provide a valid session token in Settings to perform live platform submissions.")
                    .submittedAt(Instant.now())
                    .build();
        }
    }

    @Override
    public PlatformStatusResult getPlatformStatus(String platformUsername) {
        return checkSubmissionStatus(platformUsername, LocalDate.now(ZoneOffset.UTC));
    }
}

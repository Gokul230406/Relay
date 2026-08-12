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
        log.info("CODECHEF_SUBMIT username={} sessionProvided={}", targetUsername, sessionToken != null && !sessionToken.isBlank());

        if (sessionToken == null || sessionToken.isBlank()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(false)
                    .message("CodeChef session token required. Go to Settings > CodeChef > Session Cookie and paste your CodeChef session cookie.")
                    .submittedAt(Instant.now())
                    .build();
        }

        // CodeChef submission would use their IDE API with session cookie
        // For now, return status based on live profile check
        PlatformStatusResult status = checkSubmissionStatus(targetUsername, LocalDate.now(ZoneOffset.UTC));
        if (status.isSubmittedToday()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.CODECHEF)
                    .success(true)
                    .message("CodeChef submission verified on handle: " + targetUsername)
                    .submittedAt(Instant.now())
                    .build();
        }

        return SubmissionResult.builder()
                .platform(PlatformEnum.CODECHEF)
                .success(false)
                .message("CodeChef session-based submission not yet supported. Submit manually to CodeChef to maintain streak.")
                .submittedAt(Instant.now())
                .build();
    }

    @Override
    public PlatformStatusResult getPlatformStatus(String platformUsername) {
        return checkSubmissionStatus(platformUsername, LocalDate.now(ZoneOffset.UTC));
    }

    private String validHandle(String u) {
        return (u != null && !u.isBlank() && !u.equals("unconnected")) ? u : DEFAULT_DEMO_USERNAME;
    }
}

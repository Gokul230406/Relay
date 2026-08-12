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

@Component
public class GeeksForGeeksAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(GeeksForGeeksAdapter.class);
    private static final String DEFAULT_DEMO_USERNAME = "gokul9ac3";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.GEEKSFORGEEKS;
    }

    @Override
    public PlatformStatusResult checkSubmissionStatus(String platformUsername, LocalDate date) {
        String targetUsername = validHandle(platformUsername);
        log.info("LIVE_GFG_CHECK username={} date={}", targetUsername, date);

        boolean submitted = false;
        String message = "No submission detected on GeeksforGeeks for " + targetUsername;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.geeksforgeeks.org/profile/" + targetUsername))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                message = "GeeksforGeeks profile queried for " + targetUsername;
            }
        } catch (Exception e) {
            log.warn("LIVE_GFG_WARN username={} err={}", targetUsername, e.getMessage());
        }

        log.info("LIVE_GFG_RESULT username={} submitted={}", targetUsername, submitted);

        return PlatformStatusResult.builder()
                .platform(PlatformEnum.GEEKSFORGEEKS)
                .username(targetUsername)
                .date(date)
                .submittedToday(submitted)
                .streakCount(0)
                .totalSolved(0)
                .message(message)
                .checkedAt(Instant.now())
                .build();
    }

    @Override
    public SubmissionResult submit(String platformUsername, ProblemPool solutionPoolItem, String sessionToken) {
        String targetUsername = validHandle(platformUsername);
        log.info("GFG_SUBMIT username={} sessionProvided={}", targetUsername, sessionToken != null && !sessionToken.isBlank());

        if (sessionToken == null || sessionToken.isBlank()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.GEEKSFORGEEKS)
                    .success(false)
                    .message("GeeksforGeeks session token required. Go to Settings > GFG > Session Cookie.")
                    .submittedAt(Instant.now())
                    .build();
        }

        PlatformStatusResult status = checkSubmissionStatus(targetUsername, LocalDate.now(ZoneOffset.UTC));
        if (status.isSubmittedToday()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.GEEKSFORGEEKS)
                    .success(true)
                    .message("GFG submission verified on handle: " + targetUsername)
                    .submittedAt(Instant.now())
                    .build();
        }

        return SubmissionResult.builder()
                .platform(PlatformEnum.GEEKSFORGEEKS)
                .success(false)
                .message("GFG session-based submission not yet supported. Submit manually to GFG to maintain streak.")
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

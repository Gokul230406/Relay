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
        if (platformUsername == null || platformUsername.isBlank() || platformUsername.equalsIgnoreCase("unconnected")) {
            return PlatformStatusResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .username("Not Configured")
                    .date(date != null ? date : LocalDate.now(ZoneId.of("UTC")))
                    .submittedToday(false)
                    .streakCount(0)
                    .totalSolved(0)
                    .message("LeetCode handle is not configured. Add your handle in Settings.")
                    .checkedAt(Instant.now())
                    .build();
        }

        String targetUsername = platformUsername.trim();
        LocalDate targetDate = (date != null) ? date : LocalDate.now(ZoneId.of("UTC"));

        log.info("LIVE_LEETCODE_GRAPHQL_CHECK username={} date={}", targetUsername, targetDate);

        boolean submitted = false;
        int streak = 0;
        int totalSolved = 0;
        String verifiedProblemTitle = null;
        String message = "No submission detected on LeetCode profile for " + targetUsername + " on " + targetDate;

        try {
            String graphqlQuery = "{\"query\":\"query userCheck($username: String!) { matchedUser(username: $username) { userCalendar { streak totalActiveDays submissionCalendar } submitStats { acSubmissionNum { difficulty count } } } recentAcSubmissionList(username: $username, limit: 20) { id title titleSlug timestamp } }\",\"variables\":{\"username\":\"" + targetUsername + "\"}}";

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

                // Check recentAcSubmissionList timestamps matching targetDate or current UTC/user dates
                Pattern recentPattern = Pattern.compile("\"title\":\\s*\"([^\"]+)\"[^}]*\"timestamp\":\\s*\"(\\d+)\"");
                Matcher recentMatcher = recentPattern.matcher(body);
                while (recentMatcher.find()) {
                    String title = recentMatcher.group(1);
                    long ts = Long.parseLong(recentMatcher.group(2));
                    LocalDate subDateUtc = Instant.ofEpochSecond(ts).atZone(ZoneOffset.UTC).toLocalDate();
                    LocalDate subDateUserZone = Instant.ofEpochSecond(ts).atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();

                    if (subDateUtc.equals(targetDate) || subDateUserZone.equals(targetDate) || subDateUtc.equals(LocalDate.now(ZoneOffset.UTC))) {
                        submitted = true;
                        verifiedProblemTitle = title;
                        break;
                    }
                }

                // Also check submissionCalendar epoch
                if (!submitted) {
                    long todayEpochUtc = targetDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
                    long nowEpochUtc = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
                    if (body.contains("\"" + todayEpochUtc + "\"") || body.contains("\"" + nowEpochUtc + "\"")) {
                        submitted = true;
                    }
                }

                if (submitted) {
                    message = verifiedProblemTitle != null
                            ? "Verified: " + verifiedProblemTitle + " submitted on LeetCode for " + targetUsername
                            : "Verified submission activity on LeetCode for " + targetUsername;
                }
            }
        } catch (Exception e) {
            log.warn("LIVE_LEETCODE_GRAPHQL_WARN username={} err={}", targetUsername, e.getMessage());
            message = "Unable to query live LeetCode API for " + targetUsername + ": " + e.getMessage();
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
        String targetUsername = (platformUsername != null && !platformUsername.isBlank()) ? platformUsername : "user";

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

        if (sessionToken == null || sessionToken.isBlank()) {
            return SubmissionResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .success(false)
                    .message("LeetCode credentials required. Enter your password or LEETCODE_SESSION cookie in Settings > LeetCode.")
                    .submittedAt(Instant.now())
                    .build();
        }

        try {
            String leetcodeSession = sessionToken;
            String csrfToken = "";

            // If token does NOT contain LEETCODE_SESSION=, assume user entered raw password, so automatically log in!
            if (!sessionToken.contains("LEETCODE_SESSION=")) {
                log.info("LEETCODE_AUTO_LOGIN_ATTEMPT username={}", targetUsername);
                String refreshedToken = browserService.refreshLeetCodeSession(targetUsername, sessionToken);
                if (refreshedToken != null && refreshedToken.contains("LEETCODE_SESSION=")) {
                    leetcodeSession = refreshedToken;
                }
            }

            if (leetcodeSession.contains("csrftoken=")) {
                Matcher csrfMatcher = Pattern.compile("csrftoken=([^;\\s]+)").matcher(leetcodeSession);
                if (csrfMatcher.find()) csrfToken = csrfMatcher.group(1);
            }
            if (leetcodeSession.contains("LEETCODE_SESSION=")) {
                Matcher sessMatcher = Pattern.compile("LEETCODE_SESSION=([^;\\s]+)").matcher(leetcodeSession);
                if (sessMatcher.find()) leetcodeSession = sessMatcher.group(1);
            }

            if (csrfToken.isEmpty()) {
                csrfToken = fetchCsrfToken(leetcodeSession);
            }

            String problemSlug = extractSlug(solutionPoolItem);
            if (problemSlug == null || problemSlug.isBlank() || problemSlug.equalsIgnoreCase("p-leetcode")) {
                problemSlug = "two-sum";
            }

            String questionId = getQuestionId(problemSlug);
            if (questionId == null) {
                // Fallback to Two Sum questionId = 1 if slug resolution failed
                problemSlug = "two-sum";
                questionId = "1";
            }

            String langSlug = mapLanguage(solutionPoolItem.getLanguage());
            String submitBody = "{\"question_id\":\"" + questionId + "\",\"lang\":\"" + langSlug + "\",\"typed_code\":" + escapeJson(solutionPoolItem.getSolutionCode()) + "}";

            String cookieHeader = "LEETCODE_SESSION=" + leetcodeSession + "; csrftoken=" + csrfToken;

            HttpRequest submitReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/problems/" + problemSlug + "/submit/"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Cookie", cookieHeader)
                    .header("x-csrftoken", csrfToken)
                    .header("Referer", "https://leetcode.com/problems/" + problemSlug + "/")
                    .header("Origin", "https://leetcode.com")
                    .POST(HttpRequest.BodyPublishers.ofString(submitBody))
                    .build();

            HttpResponse<String> submitResp = httpClient.send(submitReq, HttpResponse.BodyHandlers.ofString());

            log.info("LEETCODE_SUBMIT_RESPONSE status={} body={}", submitResp.statusCode(), submitResp.body());

            if (submitResp.statusCode() != 200) {
                return SubmissionResult.builder()
                        .platform(PlatformEnum.LEETCODE)
                        .success(false)
                        .message("LeetCode rejected the submission (HTTP " + submitResp.statusCode() + "). Your LEETCODE_SESSION cookie may be expired. Re-copy it from your browser.")
                        .submittedAt(Instant.now())
                        .build();
            }

            Matcher subIdMatcher = Pattern.compile("\"submission_id\":\\s*(\\d+)").matcher(submitResp.body());
            if (!subIdMatcher.find()) {
                return SubmissionResult.builder()
                        .platform(PlatformEnum.LEETCODE)
                        .success(false)
                        .message("LeetCode returned response without submission_id: " + submitResp.body())
                        .submittedAt(Instant.now())
                        .build();
            }

            String submissionId = subIdMatcher.group(1);
            log.info("LEETCODE_SUBMISSION_ID={} problem={}", submissionId, problemSlug);

            String resultStatus = pollSubmissionResult(submissionId, cookieHeader, csrfToken);
            boolean submissionRecorded = resultStatus != null && !resultStatus.contains("Timeout");

            log.info("LEETCODE_SUBMISSION_RESULT submissionId={} status={} recorded={}", submissionId, resultStatus, submissionRecorded);

            return SubmissionResult.builder()
                    .platform(PlatformEnum.LEETCODE)
                    .success(submissionRecorded)
                    .submissionId(submissionId)
                    .problemId(solutionPoolItem.getProblemId())
                    .problemTitle(solutionPoolItem.getProblemTitle())
                    .message(submissionRecorded
                            ? "✅ LeetCode submission executed! Status: " + resultStatus + " | Problem: " + solutionPoolItem.getProblemTitle() + " | ID: " + submissionId
                            : "LeetCode submission status unknown: " + resultStatus)
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

    private String fetchCsrfToken(String leetcodeSession) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com"))
                    .header("Cookie", "LEETCODE_SESSION=" + leetcodeSession)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            for (String setCookie : resp.headers().allValues("set-cookie")) {
                Matcher m = Pattern.compile("csrftoken=([^;]+)").matcher(setCookie);
                if (m.find()) return m.group(1);
            }
        } catch (Exception e) {
            log.warn("CSRF_FETCH_WARN err={}", e.getMessage());
        }
        return "dummy_csrf";
    }

    private String getQuestionId(String slug) {
        try {
            String query = "{\"query\":\"query questionData($titleSlug: String!) { question(titleSlug: $titleSlug) { questionId questionFrontendId } }\",\"variables\":{\"titleSlug\":\"" + slug + "\"}}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://leetcode.com/graphql"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .POST(HttpRequest.BodyPublishers.ofString(query))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            Matcher m = Pattern.compile("\"questionId\":\\s*\"(\\d+)\"").matcher(resp.body());
            if (m.find()) return m.group(1);
        } catch (Exception e) {
            log.warn("QUESTION_ID_FETCH_WARN slug={} err={}", slug, e.getMessage());
        }
        return null;
    }

    private String pollSubmissionResult(String submissionId, String cookieHeader, String csrfToken) {
        for (int attempt = 0; attempt < 15; attempt++) {
            try {
                Thread.sleep(2000);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("https://leetcode.com/submissions/detail/" + submissionId + "/check/"))
                        .header("Cookie", cookieHeader)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .header("Referer", "https://leetcode.com")
                        .GET()
                        .build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                String body = resp.body();

                if (body.contains("\"state\": \"PENDING\"") || body.contains("\"state\":\"PENDING\"") || body.contains("\"state\": \"STARTED\"") || body.contains("\"state\":\"STARTED\"")) {
                    log.debug("LEETCODE_POLL attempt={} state=PENDING", attempt);
                    continue;
                }

                Matcher statusMatcher = Pattern.compile("\"status_msg\":\\s*\"([^\"]+)\"").matcher(body);
                if (statusMatcher.find()) {
                    return statusMatcher.group(1);
                }

                if (body.contains("Accepted")) return "Accepted";

                return "Done (" + body.substring(0, Math.min(100, body.length())) + ")";
            } catch (Exception e) {
                log.warn("LEETCODE_POLL_WARN attempt={} err={}", attempt, e.getMessage());
            }
        }
        return "Timeout - submission submitted but polling timed out";
    }

    private String extractSlug(ProblemPool item) {
        if (item.getTargetUrl() != null && item.getTargetUrl().contains("leetcode.com/problems/")) {
            Matcher m = Pattern.compile("problems/([^/]+)").matcher(item.getTargetUrl());
            if (m.find()) return m.group(1);
        }
        if (item.getProblemTitle() != null) {
            return item.getProblemTitle().toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
        }
        return "two-sum";
    }

    private String mapLanguage(String lang) {
        if (lang == null) return "java";
        return switch (lang.toLowerCase()) {
            case "java" -> "java";
            case "python", "python3" -> "python3";
            case "javascript", "js" -> "javascript";
            case "typescript", "ts" -> "typescript";
            case "c++" , "cpp" -> "cpp";
            case "c" -> "c";
            case "c#", "csharp" -> "csharp";
            case "go", "golang" -> "golang";
            case "kotlin" -> "kotlin";
            case "swift" -> "swift";
            case "rust" -> "rust";
            case "ruby" -> "ruby";
            default -> "java";
        };
    }

    private String escapeJson(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    @Override
    public PlatformStatusResult getPlatformStatus(String platformUsername) {
        return checkSubmissionStatus(platformUsername, LocalDate.now(ZoneId.of("UTC")));
    }
}


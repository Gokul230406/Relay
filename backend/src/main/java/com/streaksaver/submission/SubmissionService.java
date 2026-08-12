package com.streaksaver.submission;

import com.streaksaver.model.*;
import com.streaksaver.notification.NotificationService;
import com.streaksaver.platform.CodingPlatformAdapter;
import com.streaksaver.platform.PlatformAdapterFactory;
import com.streaksaver.platform.PlatformStatusResult;
import com.streaksaver.platform.SubmissionResult;
import com.streaksaver.repository.*;
import com.streaksaver.streak.StreakCheckerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final StreakCheckerService streakCheckerService;
    private final PlatformAdapterFactory adapterFactory;
    private final PlatformConfigurationRepository platformConfigurationRepository;
    private final ProblemPoolRepository problemPoolRepository;
    private final SubmissionHistoryRepository submissionHistoryRepository;
    private final PlatformConnectionRepository platformConnectionRepository;
    private final DailyPlatformStatusRepository dailyPlatformStatusRepository;
    private final NotificationService notificationService;

    public SubmissionService(SubmissionGuardService guardService,
                             StreakCheckerService streakCheckerService,
                             PlatformAdapterFactory adapterFactory,
                             PlatformConfigurationRepository platformConfigurationRepository,
                             ProblemPoolRepository problemPoolRepository,
                             SubmissionHistoryRepository submissionHistoryRepository,
                             PlatformConnectionRepository platformConnectionRepository,
                             DailyPlatformStatusRepository dailyPlatformStatusRepository,
                             NotificationService notificationService) {
        this.streakCheckerService = streakCheckerService;
        this.adapterFactory = adapterFactory;
        this.platformConfigurationRepository = platformConfigurationRepository;
        this.problemPoolRepository = problemPoolRepository;
        this.submissionHistoryRepository = submissionHistoryRepository;
        this.platformConnectionRepository = platformConnectionRepository;
        this.dailyPlatformStatusRepository = dailyPlatformStatusRepository;
        this.notificationService = notificationService;
    }

    public SubmissionExecutionResponse executeEmergencySubmission(String userId, String userTimezone) {
        ZoneId zone = (userTimezone != null && !userTimezone.isBlank()) ? ZoneId.of(userTimezone) : ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);

        log.info("MULTI_PLATFORM_SUBMISSION_STARTED userId={} date={}", userId, today);

        Map<PlatformEnum, PlatformStatusResult> statuses = streakCheckerService.checkAllPlatforms(userId, today);

        PlatformConfiguration config = platformConfigurationRepository.findByUserId(userId)
                .orElseGet(() -> PlatformConfiguration.builder().userId(userId).build());

        List<PlatformEnum> enabledPlatforms = (config.getEnabledPlatforms() != null && !config.getEnabledPlatforms().isEmpty())
                ? config.getEnabledPlatforms()
                : List.of(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS);

        List<String> submittedPlatforms = new ArrayList<>();
        List<String> submissionDetails = new ArrayList<>();

        for (PlatformEnum platform : enabledPlatforms) {
            PlatformStatusResult status = statuses.get(platform);
            boolean alreadySubmitted = status != null && status.isSubmittedToday();

            if (!alreadySubmitted) {
                List<ProblemPool> poolItems = problemPoolRepository.findByUserIdAndPlatformAndActiveTrue(userId, platform);
                ProblemPool poolItem = (poolItems != null && !poolItems.isEmpty()) ? poolItems.get(0) : null;

                if (poolItem == null) {
                    poolItem = ProblemPool.builder()
                            .userId(userId)
                            .platform(platform)
                            .problemId("P-" + platform.name().toLowerCase())
                            .problemTitle("Standard Challenge (" + platform.getDisplayName() + ")")
                            .language("java")
                            .solutionCode("class Solution { public static void main(String[] args){} }")
                            .build();
                }

                Optional<PlatformConnection> conn = platformConnectionRepository.findByUserIdAndPlatform(userId, platform);
                String handle = conn.map(PlatformConnection::getPlatformUsername).orElse("user_" + userId);

                CodingPlatformAdapter adapter = adapterFactory.getAdapter(platform);
                SubmissionResult result = adapter.submit(handle, poolItem);

                if (result.isSuccess()) {
                    DailyPlatformStatus statusDoc = dailyPlatformStatusRepository
                            .findByUserIdAndDateAndPlatform(userId, today, platform)
                            .orElseGet(() -> DailyPlatformStatus.builder()
                                    .userId(userId)
                                    .date(today)
                                    .platform(platform)
                                    .build());

                    statusDoc.setSubmitted(true);
                    statusDoc.setStreakCount(Math.max(1, statusDoc.getStreakCount() + 1));
                    statusDoc.setCheckedAt(Instant.now());
                    dailyPlatformStatusRepository.save(statusDoc);

                    submittedPlatforms.add(platform.getDisplayName());
                    submissionDetails.add(platform.getDisplayName() + ": " + result.getProblemTitle() + " (" + result.getSubmissionId() + ")");

                    notificationService.sendSubmissionSuccessNotification(userId, platform.getDisplayName());
                }
            } else {
                submittedPlatforms.add(platform.getDisplayName() + " (Already Submitted)");
            }
        }

        Map<PlatformEnum, PlatformStatusResult> updatedStatuses = streakCheckerService.checkAllPlatforms(userId, today);

        recordHistory(userId, today, updatedStatuses, PlatformEnum.LEETCODE, GuardStatusEnum.SUCCESS, 
                "Relay Multi-Platform Submit", String.join(" | ", submissionDetails));

        String message = submittedPlatforms.isEmpty()
                ? "All platforms already have valid submissions today!"
                : "Successfully checked & submitted to platforms: " + String.join(", ", submittedPlatforms);

        return SubmissionExecutionResponse.builder()
                .executed(true)
                .date(today)
                .selectedPlatform(PlatformEnum.LEETCODE)
                .status(GuardStatusEnum.SUCCESS)
                .submissionId("relay_sub_" + System.currentTimeMillis())
                .problemTitle(String.join(", ", submittedPlatforms))
                .message(message)
                .dailyLimitReached(false)
                .build();
    }

    private void recordHistory(String userId, LocalDate date, Map<PlatformEnum, PlatformStatusResult> statuses,
                               PlatformEnum selectedPlatform, GuardStatusEnum status, String actionText, String details) {
        try {
            boolean lc = statuses != null && statuses.get(PlatformEnum.LEETCODE) != null && statuses.get(PlatformEnum.LEETCODE).isSubmittedToday();
            boolean cc = statuses != null && statuses.get(PlatformEnum.CODECHEF) != null && statuses.get(PlatformEnum.CODECHEF).isSubmittedToday();
            boolean gfg = statuses != null && statuses.get(PlatformEnum.GEEKSFORGEEKS) != null && statuses.get(PlatformEnum.GEEKSFORGEEKS).isSubmittedToday();

            SubmissionHistory history = SubmissionHistory.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .date(date)
                    .leetCodeSubmitted(lc)
                    .codeChefSubmitted(cc)
                    .gfgSubmitted(gfg)
                    .botAction(actionText)
                    .selectedPlatform(selectedPlatform)
                    .submissionStatus(status)
                    .details(details)
                    .timestamp(Instant.now())
                    .build();

            submissionHistoryRepository.save(history);
            log.info("HISTORY_RECORD_SUCCESS userId={} action={} timestamp={}", userId, actionText, history.getTimestamp());
        } catch (Exception e) {
            log.warn("HISTORY_RECORD_WARN Could not save history log item: {}", e.getMessage());
        }
    }
}

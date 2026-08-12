package com.streaksaver.streak;

import com.streaksaver.model.PlatformConfiguration;
import com.streaksaver.model.PlatformEnum;
import com.streaksaver.platform.PlatformStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SubmissionDecisionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionDecisionService.class);

    private static final List<PlatformEnum> DEFAULT_PRIORITY = Arrays.asList(
            PlatformEnum.LEETCODE,
            PlatformEnum.CODECHEF,
            PlatformEnum.GEEKSFORGEEKS
    );

    public DecisionResult evaluateSubmissionRequirement(
            Map<PlatformEnum, PlatformStatusResult> platformStatuses,
            PlatformConfiguration config) {

        List<PlatformEnum> priorityOrder = (config != null && config.getPriorityOrder() != null && !config.getPriorityOrder().isEmpty())
                ? config.getPriorityOrder()
                : DEFAULT_PRIORITY;

        List<PlatformEnum> enabledPlatforms = (config != null && config.getEnabledPlatforms() != null && !config.getEnabledPlatforms().isEmpty())
                ? config.getEnabledPlatforms()
                : DEFAULT_PRIORITY;

        boolean allSubmitted = true;

        for (PlatformEnum platform : enabledPlatforms) {
            PlatformStatusResult status = platformStatuses.get(platform);
            if (status == null || !status.isSubmittedToday()) {
                allSubmitted = false;
                break;
            }
        }

        if (allSubmitted) {
            log.info("SUBMISSION_NOT_REQUIRED reason='All enabled platforms have valid submissions today'");
            return DecisionResult.builder()
                    .submissionRequired(false)
                    .selectedPlatform(null)
                    .reason("All enabled platforms already have submissions today.")
                    .build();
        }

        for (PlatformEnum platform : priorityOrder) {
            if (!enabledPlatforms.contains(platform)) {
                continue;
            }

            PlatformStatusResult status = platformStatuses.get(platform);
            if (status == null || !status.isSubmittedToday()) {
                log.info("PLATFORM_SELECTED platform={} reason='Highest priority platform without a submission today'", platform);
                return DecisionResult.builder()
                        .submissionRequired(true)
                        .selectedPlatform(platform)
                        .reason("Selected " + platform.getDisplayName() + " as highest-priority missing platform.")
                        .build();
            }
        }

        return DecisionResult.builder()
                .submissionRequired(false)
                .selectedPlatform(null)
                .reason("No available platform selected.")
                .build();
    }
}

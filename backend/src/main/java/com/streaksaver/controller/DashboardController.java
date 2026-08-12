package com.streaksaver.controller;

import com.streaksaver.dto.DashboardDTO;
import com.streaksaver.model.*;
import com.streaksaver.platform.PlatformStatusResult;
import com.streaksaver.repository.*;
import com.streaksaver.streak.StreakCheckerService;
import com.streaksaver.submission.SubmissionGuardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StreakCheckerService streakCheckerService;
    private final SubmissionGuardService guardService;
    private final PlatformConfigurationRepository platformConfigRepository;
    private final SchedulerConfigurationRepository schedulerConfigRepository;
    private final UserRepository userRepository;

    public DashboardController(StreakCheckerService streakCheckerService,
                               SubmissionGuardService guardService,
                               PlatformConfigurationRepository platformConfigRepository,
                               SchedulerConfigurationRepository schedulerConfigRepository,
                               UserRepository userRepository) {
        this.streakCheckerService = streakCheckerService;
        this.guardService = guardService;
        this.platformConfigRepository = platformConfigRepository;
        this.schedulerConfigRepository = schedulerConfigRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        ZoneId userZone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        LocalDate today = LocalDate.now(userZone);

        Map<PlatformEnum, PlatformStatusResult> platformStatuses = streakCheckerService.checkAllPlatforms(userId, today);

        Map<PlatformEnum, Integer> streaks = new EnumMap<>(PlatformEnum.class);
        for (Map.Entry<PlatformEnum, PlatformStatusResult> entry : platformStatuses.entrySet()) {
            streaks.put(entry.getKey(), entry.getValue().getStreakCount());
        }

        PlatformConfiguration config = platformConfigRepository.findByUserId(userId)
                .orElseGet(() -> PlatformConfiguration.builder().userId(userId).build());

        SchedulerConfiguration schedulerConfig = schedulerConfigRepository.findByUserId(userId)
                .orElseGet(() -> SchedulerConfiguration.builder().userId(userId).build());

        Optional<DailySubmissionGuard> guardOpt = guardService.getTodayGuardRecord(userId, today);

        String botStatus = "Ready for trial submission. Scheduled trigger set for " + schedulerConfig.getEmergencyTime() + " (" + schedulerConfig.getTimezone() + ")";
        boolean dailyLimitReached = false;

        String lastSubmissionPlatform = guardOpt.flatMap(g -> Optional.ofNullable(g.getSelectedPlatform())).map(PlatformEnum::getDisplayName).orElse("None");
        String lastSubmissionTime = guardOpt.flatMap(g -> Optional.ofNullable(g.getCompletedAt())).map(Object::toString).orElse("N/A");

        DashboardDTO dto = DashboardDTO.builder()
                .userId(userId)
                .date(today.toString())
                .platformStatuses(platformStatuses)
                .streaks(streaks)
                .priorityOrder(config.getPriorityOrder())
                .emergencyTime(schedulerConfig.getEmergencyTime())
                .timezone(schedulerConfig.getTimezone())
                .botStatus(botStatus)
                .dailyLimitReached(dailyLimitReached)
                .lastSubmissionPlatform(lastSubmissionPlatform)
                .lastSubmissionTime(lastSubmissionTime)
                .build();

        return ResponseEntity.ok(dto);
    }
}

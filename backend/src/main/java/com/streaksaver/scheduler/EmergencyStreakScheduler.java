package com.streaksaver.scheduler;

import com.streaksaver.model.SchedulerConfiguration;
import com.streaksaver.notification.NotificationService;
import com.streaksaver.repository.SchedulerConfigurationRepository;
import com.streaksaver.repository.UserRepository;
import com.streaksaver.submission.SubmissionGuardService;
import com.streaksaver.submission.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

@Component
@EnableScheduling
public class EmergencyStreakScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmergencyStreakScheduler.class);

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("[HH:mm][H:mm][H:m][HH:m]")
            .toFormatter();

    private final SchedulerConfigurationRepository schedulerConfigRepository;
    private final UserRepository userRepository;
    private final SubmissionService submissionService;
    private final SubmissionGuardService guardService;
    private final NotificationService notificationService;

    public EmergencyStreakScheduler(SchedulerConfigurationRepository schedulerConfigRepository,
                                   UserRepository userRepository,
                                   SubmissionService submissionService,
                                   SubmissionGuardService guardService,
                                   NotificationService notificationService) {
        this.schedulerConfigRepository = schedulerConfigRepository;
        this.userRepository = userRepository;
        this.submissionService = submissionService;
        this.guardService = guardService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void processEmergencySubmissions() {
        List<SchedulerConfiguration> activeConfigs = schedulerConfigRepository.findByEnabledTrue();

        for (SchedulerConfiguration config : activeConfigs) {
            try {
                ZoneId userZone = ZoneId.of(config.getTimezone() != null ? config.getTimezone() : "Asia/Kolkata");
                LocalTime nowTime = LocalTime.now(userZone);
                LocalDate today = LocalDate.now(userZone);

                LocalTime emergencyTime = LocalTime.parse(config.getEmergencyTime().trim(), TIME_FORMATTER);

                if (nowTime.getHour() == emergencyTime.getHour() && nowTime.getMinute() == emergencyTime.getMinute()) {
                    log.info("SCHEDULER_EMERGENCY_TIME_TRIGGERED userId={} emergencyTime={} userTimezone={}", 
                            config.getUserId(), config.getEmergencyTime(), config.getTimezone());

                    submissionService.executeEmergencySubmission(config.getUserId(), config.getTimezone());
                }
            } catch (Exception e) {
                log.error("SCHEDULER_ERROR userId={} error={}", config.getUserId(), e.getMessage(), e);
            }
        }
    }
}

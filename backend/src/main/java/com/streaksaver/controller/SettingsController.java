package com.streaksaver.controller;

import com.streaksaver.dto.SettingsDTO;
import com.streaksaver.model.PlatformConfiguration;
import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.SchedulerConfiguration;
import com.streaksaver.model.User;
import com.streaksaver.repository.PlatformConfigurationRepository;
import com.streaksaver.repository.SchedulerConfigurationRepository;
import com.streaksaver.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final PlatformConfigurationRepository platformConfigRepository;
    private final SchedulerConfigurationRepository schedulerConfigRepository;
    private final UserRepository userRepository;

    public SettingsController(PlatformConfigurationRepository platformConfigRepository,
                              SchedulerConfigurationRepository schedulerConfigRepository,
                              UserRepository userRepository) {
        this.platformConfigRepository = platformConfigRepository;
        this.schedulerConfigRepository = schedulerConfigRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<SettingsDTO> getSettings(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        
        PlatformConfiguration config = platformConfigRepository.findByUserId(userId)
                .orElseGet(() -> PlatformConfiguration.builder()
                        .userId(userId)
                        .priorityOrder(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                        .enabledPlatforms(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                        .autoSubmitEnabled(true)
                        .notificationsEnabled(true)
                        .build());

        SchedulerConfiguration scheduler = schedulerConfigRepository.findByUserId(userId)
                .orElseGet(() -> SchedulerConfiguration.builder()
                        .userId(userId)
                        .emergencyTime("23:30")
                        .timezone("Asia/Kolkata")
                        .build());

        SettingsDTO dto = SettingsDTO.builder()
                .priorityOrder(config.getPriorityOrder())
                .enabledPlatforms(config.getEnabledPlatforms())
                .emergencyTime(scheduler.getEmergencyTime())
                .timezone(scheduler.getTimezone())
                .autoSubmitEnabled(config.isAutoSubmitEnabled())
                .notificationsEnabled(config.isNotificationsEnabled())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<SettingsDTO> updateSettings(@RequestBody SettingsDTO dto, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();

        PlatformConfiguration config = platformConfigRepository.findByUserId(userId)
                .orElseGet(() -> PlatformConfiguration.builder().userId(userId).build());

        if (dto.getPriorityOrder() != null && !dto.getPriorityOrder().isEmpty()) {
            config.setPriorityOrder(dto.getPriorityOrder());
        }
        if (dto.getEnabledPlatforms() != null) {
            config.setEnabledPlatforms(dto.getEnabledPlatforms());
        }
        config.setAutoSubmitEnabled(dto.isAutoSubmitEnabled());
        config.setNotificationsEnabled(dto.isNotificationsEnabled());
        config.setUpdatedAt(Instant.now());
        platformConfigRepository.save(config);

        SchedulerConfiguration scheduler = schedulerConfigRepository.findByUserId(userId)
                .orElseGet(() -> SchedulerConfiguration.builder().userId(userId).build());

        if (dto.getEmergencyTime() != null && !dto.getEmergencyTime().isBlank()) {
            scheduler.setEmergencyTime(dto.getEmergencyTime());
        }
        if (dto.getTimezone() != null && !dto.getTimezone().isBlank()) {
            scheduler.setTimezone(dto.getTimezone());
        }
        scheduler.setUpdatedAt(Instant.now());
        schedulerConfigRepository.save(scheduler);

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && dto.getTimezone() != null) {
            user.setTimezone(dto.getTimezone());
            userRepository.save(user);
        }

        return ResponseEntity.ok(dto);
    }
}

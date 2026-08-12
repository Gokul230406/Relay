package com.streaksaver.controller;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.User;
import com.streaksaver.platform.PlatformStatusResult;
import com.streaksaver.repository.UserRepository;
import com.streaksaver.streak.StreakCheckerService;
import com.streaksaver.submission.SubmissionExecutionResponse;
import com.streaksaver.submission.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/api/streak")
public class StreakController {

    private final StreakCheckerService streakCheckerService;
    private final SubmissionService submissionService;
    private final UserRepository userRepository;

    public StreakController(StreakCheckerService streakCheckerService,
                            SubmissionService submissionService,
                            UserRepository userRepository) {
        this.streakCheckerService = streakCheckerService;
        this.submissionService = submissionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/check")
    public ResponseEntity<Map<PlatformEnum, PlatformStatusResult>> checkStatus(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        ZoneId userZone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        LocalDate today = LocalDate.now(userZone);

        Map<PlatformEnum, PlatformStatusResult> results = streakCheckerService.checkAllPlatforms(userId, today);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/emergency-submit")
    public ResponseEntity<SubmissionExecutionResponse> triggerEmergencySubmit(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        SubmissionExecutionResponse response = submissionService.executeEmergencySubmission(userId, user.getTimezone());
        return ResponseEntity.ok(response);
    }
}

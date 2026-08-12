package com.streaksaver.submission;

import com.streaksaver.model.DailySubmissionGuard;
import com.streaksaver.model.GuardStatusEnum;
import com.streaksaver.model.PlatformEnum;

import com.streaksaver.notification.NotificationService;
import com.streaksaver.platform.CodingPlatformAdapter;
import com.streaksaver.platform.PlatformAdapterFactory;
import com.streaksaver.platform.PlatformStatusResult;
import com.streaksaver.platform.SubmissionResult;
import com.streaksaver.repository.*;
import com.streaksaver.streak.StreakCheckerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StreakSaverConcurrencyTest {

    private SubmissionService submissionService;
    private SubmissionGuardService guardService;
    private DailySubmissionGuardRepository guardRepository;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private StreakCheckerService streakCheckerService;
    private PlatformAdapterFactory adapterFactory;
    private PlatformConfigurationRepository platformConfigRepository;
    private ProblemPoolRepository problemPoolRepository;
    private SubmissionHistoryRepository submissionHistoryRepository;
    private PlatformConnectionRepository platformConnectionRepository;
    private DailyPlatformStatusRepository dailyPlatformStatusRepository;
    private NotificationService notificationService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        guardRepository = mock(DailySubmissionGuardRepository.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        guardService = new SubmissionGuardService(guardRepository, redisTemplate);

        streakCheckerService = mock(StreakCheckerService.class);
        adapterFactory = mock(PlatformAdapterFactory.class);
        platformConfigRepository = mock(PlatformConfigurationRepository.class);
        problemPoolRepository = mock(ProblemPoolRepository.class);
        submissionHistoryRepository = mock(SubmissionHistoryRepository.class);
        platformConnectionRepository = mock(PlatformConnectionRepository.class);
        dailyPlatformStatusRepository = mock(DailyPlatformStatusRepository.class);
        notificationService = mock(NotificationService.class);

        submissionService = new SubmissionService(
                guardService,
                streakCheckerService,
                adapterFactory,
                platformConfigRepository,
                problemPoolRepository,
                submissionHistoryRepository,
                platformConnectionRepository,
                dailyPlatformStatusRepository,
                notificationService
        );
    }

    @Test
    @DisplayName("MULTI PLATFORM TEST: Execute emergency submission")
    void testMultiPlatformSubmission() throws InterruptedException, ExecutionException {
        String userId = "test_user_concurrent";
        LocalDate today = LocalDate.now();

        Map<PlatformEnum, PlatformStatusResult> statuses = new EnumMap<>(PlatformEnum.class);
        statuses.put(PlatformEnum.LEETCODE, PlatformStatusResult.builder().platform(PlatformEnum.LEETCODE).date(today).submittedToday(false).build());
        statuses.put(PlatformEnum.CODECHEF, PlatformStatusResult.builder().platform(PlatformEnum.CODECHEF).date(today).submittedToday(false).build());
        statuses.put(PlatformEnum.GEEKSFORGEEKS, PlatformStatusResult.builder().platform(PlatformEnum.GEEKSFORGEEKS).date(today).submittedToday(false).build());

        when(streakCheckerService.checkAllPlatforms(eq(userId), any())).thenReturn(statuses);

        CodingPlatformAdapter adapter = mock(CodingPlatformAdapter.class);
        when(adapter.submit(anyString(), any(), any())).thenReturn(SubmissionResult.builder()
                .platform(PlatformEnum.LEETCODE)
                .success(true)
                .submissionId("sub_multi_100")
                .problemId("P-1")
                .problemTitle("Two Sum")
                .message("Submitted successfully")
                .build());

        when(adapterFactory.getAdapter(any())).thenReturn(adapter);

        SubmissionExecutionResponse resp = submissionService.executeEmergencySubmission(userId, "Asia/Kolkata");
        assertTrue(resp.isExecuted());
        assertEquals(GuardStatusEnum.SUCCESS, resp.getStatus());
    }
}

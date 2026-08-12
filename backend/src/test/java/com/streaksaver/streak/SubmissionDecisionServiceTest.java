package com.streaksaver.streak;

import com.streaksaver.model.PlatformConfiguration;
import com.streaksaver.model.PlatformEnum;
import com.streaksaver.platform.PlatformStatusResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionDecisionServiceTest {

    private SubmissionDecisionService decisionService;

    @BeforeEach
    void setUp() {
        decisionService = new SubmissionDecisionService();
    }

    @Test
    @DisplayName("When all platforms submitted -> return NO_ACTION required")
    void testAllPlatformsSubmitted() {
        LocalDate today = LocalDate.now();
        Map<PlatformEnum, PlatformStatusResult> statuses = new EnumMap<>(PlatformEnum.class);
        
        statuses.put(PlatformEnum.LEETCODE, PlatformStatusResult.builder().platform(PlatformEnum.LEETCODE).date(today).submittedToday(true).build());
        statuses.put(PlatformEnum.CODECHEF, PlatformStatusResult.builder().platform(PlatformEnum.CODECHEF).date(today).submittedToday(true).build());
        statuses.put(PlatformEnum.GEEKSFORGEEKS, PlatformStatusResult.builder().platform(PlatformEnum.GEEKSFORGEEKS).date(today).submittedToday(true).build());

        PlatformConfiguration config = PlatformConfiguration.builder()
                .priorityOrder(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                .enabledPlatforms(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                .build();

        DecisionResult result = decisionService.evaluateSubmissionRequirement(statuses, config);

        assertFalse(result.isSubmissionRequired());
        assertNull(result.getSelectedPlatform());
        assertTrue(result.getReason().contains("already have submissions"));
    }

    @Test
    @DisplayName("When LeetCode is submitted but CodeChef and GFG missing -> Select highest priority missing platform (CodeChef)")
    void testPrioritySelectionWithLeetCodeSubmitted() {
        LocalDate today = LocalDate.now();
        Map<PlatformEnum, PlatformStatusResult> statuses = new EnumMap<>(PlatformEnum.class);

        statuses.put(PlatformEnum.LEETCODE, PlatformStatusResult.builder().platform(PlatformEnum.LEETCODE).date(today).submittedToday(true).build());
        statuses.put(PlatformEnum.CODECHEF, PlatformStatusResult.builder().platform(PlatformEnum.CODECHEF).date(today).submittedToday(false).build());
        statuses.put(PlatformEnum.GEEKSFORGEEKS, PlatformStatusResult.builder().platform(PlatformEnum.GEEKSFORGEEKS).date(today).submittedToday(false).build());

        PlatformConfiguration config = PlatformConfiguration.builder()
                .priorityOrder(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                .enabledPlatforms(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                .build();

        DecisionResult result = decisionService.evaluateSubmissionRequirement(statuses, config);

        assertTrue(result.isSubmissionRequired());
        assertEquals(PlatformEnum.CODECHEF, result.getSelectedPlatform());
    }

    @Test
    @DisplayName("When all platforms missing -> Select highest priority platform (LeetCode)")
    void testAllPlatformsMissingSelectsHighestPriority() {
        LocalDate today = LocalDate.now();
        Map<PlatformEnum, PlatformStatusResult> statuses = new EnumMap<>(PlatformEnum.class);

        statuses.put(PlatformEnum.LEETCODE, PlatformStatusResult.builder().platform(PlatformEnum.LEETCODE).date(today).submittedToday(false).build());
        statuses.put(PlatformEnum.CODECHEF, PlatformStatusResult.builder().platform(PlatformEnum.CODECHEF).date(today).submittedToday(false).build());
        statuses.put(PlatformEnum.GEEKSFORGEEKS, PlatformStatusResult.builder().platform(PlatformEnum.GEEKSFORGEEKS).date(today).submittedToday(false).build());

        PlatformConfiguration config = PlatformConfiguration.builder()
                .priorityOrder(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                .enabledPlatforms(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                .build();

        DecisionResult result = decisionService.evaluateSubmissionRequirement(statuses, config);

        assertTrue(result.isSubmissionRequired());
        assertEquals(PlatformEnum.LEETCODE, result.getSelectedPlatform());
    }

    @Test
    @DisplayName("When user reorders priority (GFG > CodeChef > LeetCode) and all missing -> Select GFG")
    void testCustomUserPrioritySelection() {
        LocalDate today = LocalDate.now();
        Map<PlatformEnum, PlatformStatusResult> statuses = new EnumMap<>(PlatformEnum.class);

        statuses.put(PlatformEnum.LEETCODE, PlatformStatusResult.builder().platform(PlatformEnum.LEETCODE).date(today).submittedToday(false).build());
        statuses.put(PlatformEnum.CODECHEF, PlatformStatusResult.builder().platform(PlatformEnum.CODECHEF).date(today).submittedToday(false).build());
        statuses.put(PlatformEnum.GEEKSFORGEEKS, PlatformStatusResult.builder().platform(PlatformEnum.GEEKSFORGEEKS).date(today).submittedToday(false).build());

        PlatformConfiguration config = PlatformConfiguration.builder()
                .priorityOrder(Arrays.asList(PlatformEnum.GEEKSFORGEEKS, PlatformEnum.CODECHEF, PlatformEnum.LEETCODE))
                .enabledPlatforms(Arrays.asList(PlatformEnum.LEETCODE, PlatformEnum.CODECHEF, PlatformEnum.GEEKSFORGEEKS))
                .build();

        DecisionResult result = decisionService.evaluateSubmissionRequirement(statuses, config);

        assertTrue(result.isSubmissionRequired());
        assertEquals(PlatformEnum.GEEKSFORGEEKS, result.getSelectedPlatform());
    }
}

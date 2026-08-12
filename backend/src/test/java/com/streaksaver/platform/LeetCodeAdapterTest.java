package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LeetCodeAdapterTest {

    private LeetCodeAdapter leetCodeAdapter;
    private BrowserAutomationService browserService;

    @BeforeEach
    void setUp() {
        browserService = Mockito.mock(BrowserAutomationService.class);
        leetCodeAdapter = new LeetCodeAdapter(browserService);
    }

    @Test
    @DisplayName("Should return LEETCODE platform enum")
    void testGetPlatform() {
        assertEquals(PlatformEnum.LEETCODE, leetCodeAdapter.getPlatform());
    }

    @Test
    @DisplayName("Should reject submission when solution pool item is null")
    void testSubmitNullSolution() {
        SubmissionResult result = leetCodeAdapter.submit("testuser", null, "session123");
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(PlatformEnum.LEETCODE, result.getPlatform());
        assertTrue(result.getMessage().contains("No problem/solution"));
    }

    @Test
    @DisplayName("Should reject submission when solution code is null")
    void testSubmitNullCode() {
        ProblemPool pool = new ProblemPool();
        pool.setProblemTitle("Two Sum");
        pool.setSolutionCode(null);

        SubmissionResult result = leetCodeAdapter.submit("testuser", pool, "session123");
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No problem/solution"));
    }

    @Test
    @DisplayName("Should reject submission when session token is missing")
    void testSubmitMissingSessionToken() {
        ProblemPool pool = new ProblemPool();
        pool.setProblemTitle("Two Sum");
        pool.setSolutionCode("class Solution {}");

        SubmissionResult result = leetCodeAdapter.submit("testuser", pool, "");
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("LeetCode credentials required"));
    }

    @Test
    @DisplayName("Should return valid platform status when handle is blank in checkSubmissionStatus")
    void testCheckSubmissionStatusWithBlankHandle() {
        PlatformStatusResult status = leetCodeAdapter.checkSubmissionStatus("", LocalDate.now());
        assertNotNull(status);
        assertEquals(PlatformEnum.LEETCODE, status.getPlatform());
        assertNotNull(status.getUsername());
    }
}


package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GeeksForGeeksAdapterTest {

    private GeeksForGeeksAdapter gfgAdapter;
    private BrowserAutomationService browserService;

    @BeforeEach
    void setUp() {
        browserService = Mockito.mock(BrowserAutomationService.class);
        gfgAdapter = new GeeksForGeeksAdapter(browserService);
    }

    @Test
    @DisplayName("Should return GEEKSFORGEEKS platform enum")
    void testGetPlatform() {
        assertEquals(PlatformEnum.GEEKSFORGEEKS, gfgAdapter.getPlatform());
    }

    @Test
    @DisplayName("Should reject submission when solution pool item is null")
    void testSubmitNullSolution() {
        SubmissionResult result = gfgAdapter.submit("testuser", null, "pass123");
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(PlatformEnum.GEEKSFORGEEKS, result.getPlatform());
        assertTrue(result.getMessage().contains("No problem/solution"));
    }

    @Test
    @DisplayName("Should reject submission when password is missing")
    void testSubmitMissingPassword() {
        ProblemPool pool = new ProblemPool();
        pool.setProblemTitle("Missing Array Number");
        pool.setSolutionCode("int missingNumber() { return 0; }");

        SubmissionResult result = gfgAdapter.submit("testuser", pool, "");
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("GFG credentials required"));
    }

    @Test
    @DisplayName("Should return valid platform status result on checkSubmissionStatus")
    void testCheckSubmissionStatus() {
        PlatformStatusResult status = gfgAdapter.checkSubmissionStatus("gokul9ac3", LocalDate.now());
        assertNotNull(status);
        assertEquals(PlatformEnum.GEEKSFORGEEKS, status.getPlatform());
        assertEquals("gokul9ac3", status.getUsername());
    }
}

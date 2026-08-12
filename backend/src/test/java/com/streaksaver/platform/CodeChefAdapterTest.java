package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CodeChefAdapterTest {

    private CodeChefAdapter codeChefAdapter;
    private BrowserAutomationService browserService;

    @BeforeEach
    void setUp() {
        browserService = Mockito.mock(BrowserAutomationService.class);
        codeChefAdapter = new CodeChefAdapter(browserService);
    }

    @Test
    @DisplayName("Should return CODECHEF platform enum")
    void testGetPlatform() {
        assertEquals(PlatformEnum.CODECHEF, codeChefAdapter.getPlatform());
    }

    @Test
    @DisplayName("Should reject submission when solution pool item is null")
    void testSubmitNullSolution() {
        SubmissionResult result = codeChefAdapter.submit("testuser", null, "pass123");
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(PlatformEnum.CODECHEF, result.getPlatform());
        assertTrue(result.getMessage().contains("No problem/solution"));
    }

    @Test
    @DisplayName("Should reject submission when password credential is missing")
    void testSubmitMissingPassword() {
        ProblemPool pool = new ProblemPool();
        pool.setProblemTitle("Chef and Brain Speed");
        pool.setSolutionCode("print('YES')");

        SubmissionResult result = codeChefAdapter.submit("testuser", pool, "");
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("CodeChef credentials required"));
    }

    @Test
    @DisplayName("Should return valid platform status structure on checkSubmissionStatus")
    void testCheckSubmissionStatus() {
        PlatformStatusResult status = codeChefAdapter.checkSubmissionStatus("gold_dear_38", LocalDate.now());
        assertNotNull(status);
        assertEquals(PlatformEnum.CODECHEF, status.getPlatform());
        assertEquals("gold_dear_38", status.getUsername());
    }
}

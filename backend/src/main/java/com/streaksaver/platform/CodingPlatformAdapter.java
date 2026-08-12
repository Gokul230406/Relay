package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;

import java.time.LocalDate;

public interface CodingPlatformAdapter {
    PlatformEnum getPlatform();

    PlatformStatusResult checkSubmissionStatus(String platformUsername, LocalDate date);

    /**
     * Submit code to the platform using the user's session token.
     * @param platformUsername the user's handle on the platform
     * @param solutionPoolItem the problem + solution code to submit
     * @param sessionToken the user's authenticated session cookie/token (e.g. LEETCODE_SESSION)
     * @return result of the submission attempt
     */
    SubmissionResult submit(String platformUsername, ProblemPool solutionPoolItem, String sessionToken);

    PlatformStatusResult getPlatformStatus(String platformUsername);
}

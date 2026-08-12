package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;

import java.time.LocalDate;

public interface CodingPlatformAdapter {
    PlatformEnum getPlatform();

    PlatformStatusResult checkSubmissionStatus(String platformUsername, LocalDate date);

    SubmissionResult submit(String platformUsername, ProblemPool solutionPoolItem);

    PlatformStatusResult getPlatformStatus(String platformUsername);
}

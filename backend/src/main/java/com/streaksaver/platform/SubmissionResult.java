package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import java.time.Instant;

public class SubmissionResult {
    private PlatformEnum platform;
    private boolean success;
    private String submissionId;
    private String problemId;
    private String problemTitle;
    private String message;
    private String executionTime;
    private Instant submittedAt = Instant.now();

    public SubmissionResult() {}

    public SubmissionResult(PlatformEnum platform, boolean success, String submissionId, String problemId, String problemTitle, String message, String executionTime, Instant submittedAt) {
        this.platform = platform;
        this.success = success;
        this.submissionId = submissionId;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.message = message;
        this.executionTime = executionTime;
        this.submittedAt = submittedAt != null ? submittedAt : Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private PlatformEnum platform;
        private boolean success;
        private String submissionId;
        private String problemId;
        private String problemTitle;
        private String message;
        private String executionTime;
        private Instant submittedAt = Instant.now();

        public Builder platform(PlatformEnum platform) { this.platform = platform; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder submissionId(String submissionId) { this.submissionId = submissionId; return this; }
        public Builder problemId(String problemId) { this.problemId = problemId; return this; }
        public Builder problemTitle(String problemTitle) { this.problemTitle = problemTitle; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder executionTime(String executionTime) { this.executionTime = executionTime; return this; }
        public Builder submittedAt(Instant submittedAt) { this.submittedAt = submittedAt; return this; }

        public SubmissionResult build() {
            return new SubmissionResult(platform, success, submissionId, problemId, problemTitle, message, executionTime, submittedAt);
        }
    }

    public PlatformEnum getPlatform() { return platform; }
    public boolean isSuccess() { return success; }
    public String getSubmissionId() { return submissionId; }
    public String getProblemId() { return problemId; }
    public String getProblemTitle() { return problemTitle; }
    public String getMessage() { return message; }
    public String getExecutionTime() { return executionTime; }
    public Instant getSubmittedAt() { return submittedAt; }
}

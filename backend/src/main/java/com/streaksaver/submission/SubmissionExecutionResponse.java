package com.streaksaver.submission;

import com.streaksaver.model.GuardStatusEnum;
import com.streaksaver.model.PlatformEnum;
import java.time.Instant;
import java.time.LocalDate;

public class SubmissionExecutionResponse {
    private boolean executed;
    private LocalDate date;
    private PlatformEnum selectedPlatform;
    private GuardStatusEnum status;
    private String submissionId;
    private String problemTitle;
    private String message;
    private boolean dailyLimitReached;
    private Instant timestamp = Instant.now();

    public SubmissionExecutionResponse() {}

    public SubmissionExecutionResponse(boolean executed, LocalDate date, PlatformEnum selectedPlatform, GuardStatusEnum status, String submissionId, String problemTitle, String message, boolean dailyLimitReached, Instant timestamp) {
        this.executed = executed;
        this.date = date;
        this.selectedPlatform = selectedPlatform;
        this.status = status;
        this.submissionId = submissionId;
        this.problemTitle = problemTitle;
        this.message = message;
        this.dailyLimitReached = dailyLimitReached;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean executed;
        private LocalDate date;
        private PlatformEnum selectedPlatform;
        private GuardStatusEnum status;
        private String submissionId;
        private String problemTitle;
        private String message;
        private boolean dailyLimitReached;
        private Instant timestamp = Instant.now();

        public Builder executed(boolean executed) { this.executed = executed; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder selectedPlatform(PlatformEnum selectedPlatform) { this.selectedPlatform = selectedPlatform; return this; }
        public Builder status(GuardStatusEnum status) { this.status = status; return this; }
        public Builder submissionId(String submissionId) { this.submissionId = submissionId; return this; }
        public Builder problemTitle(String problemTitle) { this.problemTitle = problemTitle; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder dailyLimitReached(boolean dailyLimitReached) { this.dailyLimitReached = dailyLimitReached; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public SubmissionExecutionResponse build() {
            return new SubmissionExecutionResponse(executed, date, selectedPlatform, status, submissionId, problemTitle, message, dailyLimitReached, timestamp);
        }
    }

    public boolean isExecuted() { return executed; }
    public LocalDate getDate() { return date; }
    public PlatformEnum getSelectedPlatform() { return selectedPlatform; }
    public GuardStatusEnum getStatus() { return status; }
    public String getSubmissionId() { return submissionId; }
    public String getProblemTitle() { return problemTitle; }
    public String getMessage() { return message; }
    public boolean isDailyLimitReached() { return dailyLimitReached; }
    public Instant getTimestamp() { return timestamp; }
}

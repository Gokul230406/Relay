package com.streaksaver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "daily_submission_guards")
@CompoundIndex(name = "user_date_unique_idx", def = "{'userId': 1, 'date': 1}", unique = true)
public class DailySubmissionGuard {
    @Id
    private String id;

    private String userId;

    private LocalDate date;

    private PlatformEnum selectedPlatform;

    private GuardStatusEnum status;

    private String submissionId;

    private String problemId;

    private String problemTitle;

    private Instant attemptStartedAt;

    private Instant completedAt;

    private String errorMessage;

    private Instant createdAt = Instant.now();

    public DailySubmissionGuard() {}

    public DailySubmissionGuard(String id, String userId, LocalDate date, PlatformEnum selectedPlatform, GuardStatusEnum status, String submissionId, String problemId, String problemTitle, Instant attemptStartedAt, Instant completedAt, String errorMessage, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.selectedPlatform = selectedPlatform;
        this.status = status;
        this.submissionId = submissionId;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.attemptStartedAt = attemptStartedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private LocalDate date;
        private PlatformEnum selectedPlatform;
        private GuardStatusEnum status;
        private String submissionId;
        private String problemId;
        private String problemTitle;
        private Instant attemptStartedAt;
        private Instant completedAt;
        private String errorMessage;
        private Instant createdAt = Instant.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder selectedPlatform(PlatformEnum selectedPlatform) { this.selectedPlatform = selectedPlatform; return this; }
        public Builder status(GuardStatusEnum status) { this.status = status; return this; }
        public Builder submissionId(String submissionId) { this.submissionId = submissionId; return this; }
        public Builder problemId(String problemId) { this.problemId = problemId; return this; }
        public Builder problemTitle(String problemTitle) { this.problemTitle = problemTitle; return this; }
        public Builder attemptStartedAt(Instant attemptStartedAt) { this.attemptStartedAt = attemptStartedAt; return this; }
        public Builder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public DailySubmissionGuard build() {
            return new DailySubmissionGuard(id, userId, date, selectedPlatform, status, submissionId, problemId, problemTitle, attemptStartedAt, completedAt, errorMessage, createdAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public PlatformEnum getSelectedPlatform() { return selectedPlatform; }
    public void setSelectedPlatform(PlatformEnum selectedPlatform) { this.selectedPlatform = selectedPlatform; }

    public GuardStatusEnum getStatus() { return status; }
    public void setStatus(GuardStatusEnum status) { this.status = status; }

    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }

    public String getProblemId() { return problemId; }
    public void setProblemId(String problemId) { this.problemId = problemId; }

    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }

    public Instant getAttemptStartedAt() { return attemptStartedAt; }
    public void setAttemptStartedAt(Instant attemptStartedAt) { this.attemptStartedAt = attemptStartedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

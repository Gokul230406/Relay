package com.streaksaver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "submission_histories")
@CompoundIndex(name = "relay_history_non_unique_v3", def = "{'userId': 1, 'timestamp': -1}")
public class SubmissionHistory {
    @Id
    private String id;

    private String userId;

    private LocalDate date;

    private boolean leetCodeSubmitted;

    private boolean codeChefSubmitted;

    private boolean gfgSubmitted;

    private String botAction;

    private PlatformEnum selectedPlatform;

    private GuardStatusEnum submissionStatus;

    private String problemTitle;

    private String details;

    private Instant timestamp = Instant.now();

    public SubmissionHistory() {}

    public SubmissionHistory(String id, String userId, LocalDate date, boolean leetCodeSubmitted, boolean codeChefSubmitted, boolean gfgSubmitted, String botAction, PlatformEnum selectedPlatform, GuardStatusEnum submissionStatus, String problemTitle, String details, Instant timestamp) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.leetCodeSubmitted = leetCodeSubmitted;
        this.codeChefSubmitted = codeChefSubmitted;
        this.gfgSubmitted = gfgSubmitted;
        this.botAction = botAction;
        this.selectedPlatform = selectedPlatform;
        this.submissionStatus = submissionStatus;
        this.problemTitle = problemTitle;
        this.details = details;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String userId;
        private LocalDate date;
        private boolean leetCodeSubmitted;
        private boolean codeChefSubmitted;
        private boolean gfgSubmitted;
        private String botAction;
        private PlatformEnum selectedPlatform;
        private GuardStatusEnum submissionStatus;
        private String problemTitle;
        private String details;
        private Instant timestamp = Instant.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder leetCodeSubmitted(boolean leetCodeSubmitted) { this.leetCodeSubmitted = leetCodeSubmitted; return this; }
        public Builder codeChefSubmitted(boolean codeChefSubmitted) { this.codeChefSubmitted = codeChefSubmitted; return this; }
        public Builder gfgSubmitted(boolean gfgSubmitted) { this.gfgSubmitted = gfgSubmitted; return this; }
        public Builder botAction(String botAction) { this.botAction = botAction; return this; }
        public Builder selectedPlatform(PlatformEnum selectedPlatform) { this.selectedPlatform = selectedPlatform; return this; }
        public Builder submissionStatus(GuardStatusEnum submissionStatus) { this.submissionStatus = submissionStatus; return this; }
        public Builder problemTitle(String problemTitle) { this.problemTitle = problemTitle; return this; }
        public Builder details(String details) { this.details = details; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public SubmissionHistory build() {
            return new SubmissionHistory(id, userId, date, leetCodeSubmitted, codeChefSubmitted, gfgSubmitted, botAction, selectedPlatform, submissionStatus, problemTitle, details, timestamp);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isLeetCodeSubmitted() { return leetCodeSubmitted; }
    public void setLeetCodeSubmitted(boolean leetCodeSubmitted) { this.leetCodeSubmitted = leetCodeSubmitted; }

    public boolean isCodeChefSubmitted() { return codeChefSubmitted; }
    public void setCodeChefSubmitted(boolean codeChefSubmitted) { this.codeChefSubmitted = codeChefSubmitted; }

    public boolean isGfgSubmitted() { return gfgSubmitted; }
    public void setGfgSubmitted(boolean gfgSubmitted) { this.gfgSubmitted = gfgSubmitted; }

    public String getBotAction() { return botAction; }
    public void setBotAction(String botAction) { this.botAction = botAction; }

    public PlatformEnum getSelectedPlatform() { return selectedPlatform; }
    public void setSelectedPlatform(PlatformEnum selectedPlatform) { this.selectedPlatform = selectedPlatform; }

    public GuardStatusEnum getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(GuardStatusEnum submissionStatus) { this.submissionStatus = submissionStatus; }

    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

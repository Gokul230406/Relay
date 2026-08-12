package com.streaksaver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "problem_pools")
@CompoundIndex(name = "user_platform_problem_idx", def = "{'userId': 1, 'platform': 1, 'problemId': 1}", unique = true)
public class ProblemPool {
    @Id
    private String id;

    private String userId;

    private PlatformEnum platform;

    private String problemId;

    private String problemTitle;

    private String language;

    private String solutionCode;

    private String targetUrl;

    private boolean active = true;

    private Instant createdAt = Instant.now();

    public ProblemPool() {}

    public ProblemPool(String id, String userId, PlatformEnum platform, String problemId, String problemTitle, String language, String solutionCode, String targetUrl, boolean active, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.platform = platform;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.language = language;
        this.solutionCode = solutionCode;
        this.targetUrl = targetUrl;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String userId;
        private PlatformEnum platform;
        private String problemId;
        private String problemTitle;
        private String language;
        private String solutionCode;
        private String targetUrl;
        private boolean active = true;
        private Instant createdAt = Instant.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder platform(PlatformEnum platform) { this.platform = platform; return this; }
        public Builder problemId(String problemId) { this.problemId = problemId; return this; }
        public Builder problemTitle(String problemTitle) { this.problemTitle = problemTitle; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder solutionCode(String solutionCode) { this.solutionCode = solutionCode; return this; }
        public Builder targetUrl(String targetUrl) { this.targetUrl = targetUrl; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ProblemPool build() {
            return new ProblemPool(id, userId, platform, problemId, problemTitle, language, solutionCode, targetUrl, active, createdAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public PlatformEnum getPlatform() { return platform; }
    public void setPlatform(PlatformEnum platform) { this.platform = platform; }

    public String getProblemId() { return problemId; }
    public void setProblemId(String problemId) { this.problemId = problemId; }

    public String getProblemTitle() { return problemTitle; }
    public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSolutionCode() { return solutionCode; }
    public void setSolutionCode(String solutionCode) { this.solutionCode = solutionCode; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

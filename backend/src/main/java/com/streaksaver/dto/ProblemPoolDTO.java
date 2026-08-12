package com.streaksaver.dto;

import com.streaksaver.model.PlatformEnum;

public class ProblemPoolDTO {
    private String id;
    private PlatformEnum platform;
    private String problemId;
    private String problemTitle;
    private String language;
    private String solutionCode;
    private String targetUrl;
    private boolean active;

    public ProblemPoolDTO() {}

    public ProblemPoolDTO(String id, PlatformEnum platform, String problemId, String problemTitle, String language, String solutionCode, String targetUrl, boolean active) {
        this.id = id;
        this.platform = platform;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.language = language;
        this.solutionCode = solutionCode;
        this.targetUrl = targetUrl;
        this.active = active;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private PlatformEnum platform;
        private String problemId;
        private String problemTitle;
        private String language;
        private String solutionCode;
        private String targetUrl;
        private boolean active;

        public Builder id(String id) { this.id = id; return this; }
        public Builder platform(PlatformEnum platform) { this.platform = platform; return this; }
        public Builder problemId(String problemId) { this.problemId = problemId; return this; }
        public Builder problemTitle(String problemTitle) { this.problemTitle = problemTitle; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder solutionCode(String solutionCode) { this.solutionCode = solutionCode; return this; }
        public Builder targetUrl(String targetUrl) { this.targetUrl = targetUrl; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public ProblemPoolDTO build() {
            return new ProblemPoolDTO(id, platform, problemId, problemTitle, language, solutionCode, targetUrl, active);
        }
    }

    public String getId() { return id; }
    public PlatformEnum getPlatform() { return platform; }
    public String getProblemId() { return problemId; }
    public String getProblemTitle() { return problemTitle; }
    public String getLanguage() { return language; }
    public String getSolutionCode() { return solutionCode; }
    public String getTargetUrl() { return targetUrl; }
    public boolean isActive() { return active; }
}

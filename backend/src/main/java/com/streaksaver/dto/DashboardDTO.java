package com.streaksaver.dto;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.platform.PlatformStatusResult;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private String userId;
    private String date;
    private Map<PlatformEnum, PlatformStatusResult> platformStatuses;
    private Map<PlatformEnum, Integer> streaks;
    private List<PlatformEnum> priorityOrder;
    private String emergencyTime;
    private String timezone;
    private String botStatus;
    private boolean dailyLimitReached;
    private String lastSubmissionPlatform;
    private String lastSubmissionTime;

    public DashboardDTO() {}

    public DashboardDTO(String userId, String date, Map<PlatformEnum, PlatformStatusResult> platformStatuses, Map<PlatformEnum, Integer> streaks, List<PlatformEnum> priorityOrder, String emergencyTime, String timezone, String botStatus, boolean dailyLimitReached, String lastSubmissionPlatform, String lastSubmissionTime) {
        this.userId = userId;
        this.date = date;
        this.platformStatuses = platformStatuses;
        this.streaks = streaks;
        this.priorityOrder = priorityOrder;
        this.emergencyTime = emergencyTime;
        this.timezone = timezone;
        this.botStatus = botStatus;
        this.dailyLimitReached = dailyLimitReached;
        this.lastSubmissionPlatform = lastSubmissionPlatform;
        this.lastSubmissionTime = lastSubmissionTime;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userId;
        private String date;
        private Map<PlatformEnum, PlatformStatusResult> platformStatuses;
        private Map<PlatformEnum, Integer> streaks;
        private List<PlatformEnum> priorityOrder;
        private String emergencyTime;
        private String timezone;
        private String botStatus;
        private boolean dailyLimitReached;
        private String lastSubmissionPlatform;
        private String lastSubmissionTime;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder date(String date) { this.date = date; return this; }
        public Builder platformStatuses(Map<PlatformEnum, PlatformStatusResult> platformStatuses) { this.platformStatuses = platformStatuses; return this; }
        public Builder streaks(Map<PlatformEnum, Integer> streaks) { this.streaks = streaks; return this; }
        public Builder priorityOrder(List<PlatformEnum> priorityOrder) { this.priorityOrder = priorityOrder; return this; }
        public Builder emergencyTime(String emergencyTime) { this.emergencyTime = emergencyTime; return this; }
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }
        public Builder botStatus(String botStatus) { this.botStatus = botStatus; return this; }
        public Builder dailyLimitReached(boolean dailyLimitReached) { this.dailyLimitReached = dailyLimitReached; return this; }
        public Builder lastSubmissionPlatform(String lastSubmissionPlatform) { this.lastSubmissionPlatform = lastSubmissionPlatform; return this; }
        public Builder lastSubmissionTime(String lastSubmissionTime) { this.lastSubmissionTime = lastSubmissionTime; return this; }

        public DashboardDTO build() {
            return new DashboardDTO(userId, date, platformStatuses, streaks, priorityOrder, emergencyTime, timezone, botStatus, dailyLimitReached, lastSubmissionPlatform, lastSubmissionTime);
        }
    }

    public String getUserId() { return userId; }
    public String getDate() { return date; }
    public Map<PlatformEnum, PlatformStatusResult> getPlatformStatuses() { return platformStatuses; }
    public Map<PlatformEnum, Integer> getStreaks() { return streaks; }
    public List<PlatformEnum> getPriorityOrder() { return priorityOrder; }
    public String getEmergencyTime() { return emergencyTime; }
    public String getTimezone() { return timezone; }
    public String getBotStatus() { return botStatus; }
    public boolean isDailyLimitReached() { return dailyLimitReached; }
    public String getLastSubmissionPlatform() { return lastSubmissionPlatform; }
    public String getLastSubmissionTime() { return lastSubmissionTime; }
}

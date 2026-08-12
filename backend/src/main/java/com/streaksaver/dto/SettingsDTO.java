package com.streaksaver.dto;

import com.streaksaver.model.PlatformEnum;
import java.util.List;

public class SettingsDTO {
    private List<PlatformEnum> priorityOrder;
    private List<PlatformEnum> enabledPlatforms;
    private String emergencyTime;
    private String timezone;
    private boolean autoSubmitEnabled;
    private boolean notificationsEnabled;

    public SettingsDTO() {}

    public SettingsDTO(List<PlatformEnum> priorityOrder, List<PlatformEnum> enabledPlatforms, String emergencyTime, String timezone, boolean autoSubmitEnabled, boolean notificationsEnabled) {
        this.priorityOrder = priorityOrder;
        this.enabledPlatforms = enabledPlatforms;
        this.emergencyTime = emergencyTime;
        this.timezone = timezone;
        this.autoSubmitEnabled = autoSubmitEnabled;
        this.notificationsEnabled = notificationsEnabled;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<PlatformEnum> priorityOrder;
        private List<PlatformEnum> enabledPlatforms;
        private String emergencyTime;
        private String timezone;
        private boolean autoSubmitEnabled;
        private boolean notificationsEnabled;

        public Builder priorityOrder(List<PlatformEnum> priorityOrder) { this.priorityOrder = priorityOrder; return this; }
        public Builder enabledPlatforms(List<PlatformEnum> enabledPlatforms) { this.enabledPlatforms = enabledPlatforms; return this; }
        public Builder emergencyTime(String emergencyTime) { this.emergencyTime = emergencyTime; return this; }
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }
        public Builder autoSubmitEnabled(boolean autoSubmitEnabled) { this.autoSubmitEnabled = autoSubmitEnabled; return this; }
        public Builder notificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; return this; }

        public SettingsDTO build() {
            return new SettingsDTO(priorityOrder, enabledPlatforms, emergencyTime, timezone, autoSubmitEnabled, notificationsEnabled);
        }
    }

    public List<PlatformEnum> getPriorityOrder() { return priorityOrder; }
    public List<PlatformEnum> getEnabledPlatforms() { return enabledPlatforms; }
    public String getEmergencyTime() { return emergencyTime; }
    public String getTimezone() { return timezone; }
    public boolean isAutoSubmitEnabled() { return autoSubmitEnabled; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
}

package com.streaksaver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "platform_configurations")
public class PlatformConfiguration {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private List<PlatformEnum> priorityOrder;

    private List<PlatformEnum> enabledPlatforms;

    private boolean autoSubmitEnabled = true;

    private boolean notificationsEnabled = true;

    private Instant updatedAt = Instant.now();

    public PlatformConfiguration() {}

    public PlatformConfiguration(String id, String userId, List<PlatformEnum> priorityOrder, List<PlatformEnum> enabledPlatforms, boolean autoSubmitEnabled, boolean notificationsEnabled, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.priorityOrder = priorityOrder;
        this.enabledPlatforms = enabledPlatforms;
        this.autoSubmitEnabled = autoSubmitEnabled;
        this.notificationsEnabled = notificationsEnabled;
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String userId;
        private List<PlatformEnum> priorityOrder;
        private List<PlatformEnum> enabledPlatforms;
        private boolean autoSubmitEnabled = true;
        private boolean notificationsEnabled = true;
        private Instant updatedAt = Instant.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder priorityOrder(List<PlatformEnum> priorityOrder) { this.priorityOrder = priorityOrder; return this; }
        public Builder enabledPlatforms(List<PlatformEnum> enabledPlatforms) { this.enabledPlatforms = enabledPlatforms; return this; }
        public Builder autoSubmitEnabled(boolean autoSubmitEnabled) { this.autoSubmitEnabled = autoSubmitEnabled; return this; }
        public Builder notificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public PlatformConfiguration build() {
            return new PlatformConfiguration(id, userId, priorityOrder, enabledPlatforms, autoSubmitEnabled, notificationsEnabled, updatedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<PlatformEnum> getPriorityOrder() { return priorityOrder; }
    public void setPriorityOrder(List<PlatformEnum> priorityOrder) { this.priorityOrder = priorityOrder; }

    public List<PlatformEnum> getEnabledPlatforms() { return enabledPlatforms; }
    public void setEnabledPlatforms(List<PlatformEnum> enabledPlatforms) { this.enabledPlatforms = enabledPlatforms; }

    public boolean isAutoSubmitEnabled() { return autoSubmitEnabled; }
    public void setAutoSubmitEnabled(boolean autoSubmitEnabled) { this.autoSubmitEnabled = autoSubmitEnabled; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

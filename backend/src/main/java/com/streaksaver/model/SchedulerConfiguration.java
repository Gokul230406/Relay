package com.streaksaver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "scheduler_configurations")
public class SchedulerConfiguration {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String emergencyTime = "23:30";

    private String timezone = "Asia/Kolkata";

    private boolean enabled = true;

    private Instant updatedAt = Instant.now();

    public SchedulerConfiguration() {}

    public SchedulerConfiguration(String id, String userId, String emergencyTime, String timezone, boolean enabled, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.emergencyTime = emergencyTime != null ? emergencyTime : "23:30";
        this.timezone = timezone != null ? timezone : "Asia/Kolkata";
        this.enabled = enabled;
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String userId;
        private String emergencyTime = "23:30";
        private String timezone = "Asia/Kolkata";
        private boolean enabled = true;
        private Instant updatedAt = Instant.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder emergencyTime(String emergencyTime) { this.emergencyTime = emergencyTime; return this; }
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public SchedulerConfiguration build() {
            return new SchedulerConfiguration(id, userId, emergencyTime, timezone, enabled, updatedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmergencyTime() { return emergencyTime; }
    public void setEmergencyTime(String emergencyTime) { this.emergencyTime = emergencyTime; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

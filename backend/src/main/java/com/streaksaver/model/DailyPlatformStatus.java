package com.streaksaver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "daily_platform_statuses")
@CompoundIndex(name = "user_date_platform_idx", def = "{'userId': 1, 'date': 1, 'platform': 1}", unique = true)
public class DailyPlatformStatus {
    @Id
    private String id;

    private String userId;

    private LocalDate date;

    private PlatformEnum platform;

    private boolean submitted;

    private int streakCount;

    private Instant checkedAt;

    public DailyPlatformStatus() {}

    public DailyPlatformStatus(String id, String userId, LocalDate date, PlatformEnum platform, boolean submitted, int streakCount, Instant checkedAt) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.platform = platform;
        this.submitted = submitted;
        this.streakCount = streakCount;
        this.checkedAt = checkedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String userId;
        private LocalDate date;
        private PlatformEnum platform;
        private boolean submitted;
        private int streakCount;
        private Instant checkedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder platform(PlatformEnum platform) { this.platform = platform; return this; }
        public Builder submitted(boolean submitted) { this.submitted = submitted; return this; }
        public Builder streakCount(int streakCount) { this.streakCount = streakCount; return this; }
        public Builder checkedAt(Instant checkedAt) { this.checkedAt = checkedAt; return this; }

        public DailyPlatformStatus build() {
            return new DailyPlatformStatus(id, userId, date, platform, submitted, streakCount, checkedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public PlatformEnum getPlatform() { return platform; }
    public void setPlatform(PlatformEnum platform) { this.platform = platform; }

    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}

package com.streaksaver.platform;

import com.streaksaver.model.PlatformEnum;
import java.time.Instant;
import java.time.LocalDate;

public class PlatformStatusResult {
    private PlatformEnum platform;
    private String username;
    private LocalDate date;
    private boolean submittedToday;
    private int streakCount;
    private int totalSolved;
    private String message;
    private Instant checkedAt = Instant.now();

    public PlatformStatusResult() {}

    public PlatformStatusResult(PlatformEnum platform, String username, LocalDate date, boolean submittedToday, int streakCount, int totalSolved, String message, Instant checkedAt) {
        this.platform = platform;
        this.username = username;
        this.date = date;
        this.submittedToday = submittedToday;
        this.streakCount = streakCount;
        this.totalSolved = totalSolved;
        this.message = message;
        this.checkedAt = checkedAt != null ? checkedAt : Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private PlatformEnum platform;
        private String username;
        private LocalDate date;
        private boolean submittedToday;
        private int streakCount;
        private int totalSolved;
        private String message;
        private Instant checkedAt = Instant.now();

        public Builder platform(PlatformEnum platform) { this.platform = platform; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder submittedToday(boolean submittedToday) { this.submittedToday = submittedToday; return this; }
        public Builder streakCount(int streakCount) { this.streakCount = streakCount; return this; }
        public Builder totalSolved(int totalSolved) { this.totalSolved = totalSolved; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder checkedAt(Instant checkedAt) { this.checkedAt = checkedAt; return this; }

        public PlatformStatusResult build() {
            return new PlatformStatusResult(platform, username, date, submittedToday, streakCount, totalSolved, message, checkedAt);
        }
    }

    public PlatformEnum getPlatform() { return platform; }
    public String getUsername() { return username; }
    public LocalDate getDate() { return date; }
    public boolean isSubmittedToday() { return submittedToday; }
    public int getStreakCount() { return streakCount; }
    public int getTotalSolved() { return totalSolved; }
    public String getMessage() { return message; }
    public Instant getCheckedAt() { return checkedAt; }
}

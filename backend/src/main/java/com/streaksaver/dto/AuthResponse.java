package com.streaksaver.dto;

public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String fullName;
    private String timezone;

    public AuthResponse() {}

    public AuthResponse(String token, String userId, String email, String fullName, String timezone) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.timezone = timezone;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String userId;
        private String email;
        private String fullName;
        private String timezone;

        public Builder token(String token) { this.token = token; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, userId, email, fullName, timezone);
        }
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getTimezone() { return timezone; }
}

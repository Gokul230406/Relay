package com.streaksaver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "platform_connections")
@CompoundIndex(name = "user_platform_unique_idx", def = "{'userId': 1, 'platform': 1}", unique = true)
public class PlatformConnection {
    @Id
    private String id;

    private String userId;

    private PlatformEnum platform;

    private String platformUsername;

    private String encryptedAuthToken;

    private boolean connected;

    private String connectionMessage;

    private Instant lastVerifiedAt = Instant.now();

    private Instant createdAt = Instant.now();

    public PlatformConnection() {}

    public PlatformConnection(String id, String userId, PlatformEnum platform, String platformUsername, String encryptedAuthToken, boolean connected, String connectionMessage, Instant lastVerifiedAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.platform = platform;
        this.platformUsername = platformUsername;
        this.encryptedAuthToken = encryptedAuthToken;
        this.connected = connected;
        this.connectionMessage = connectionMessage;
        this.lastVerifiedAt = lastVerifiedAt != null ? lastVerifiedAt : Instant.now();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String userId;
        private PlatformEnum platform;
        private String platformUsername;
        private String encryptedAuthToken;
        private boolean connected;
        private String connectionMessage;
        private Instant lastVerifiedAt = Instant.now();
        private Instant createdAt = Instant.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder platform(PlatformEnum platform) { this.platform = platform; return this; }
        public Builder platformUsername(String platformUsername) { this.platformUsername = platformUsername; return this; }
        public Builder encryptedAuthToken(String encryptedAuthToken) { this.encryptedAuthToken = encryptedAuthToken; return this; }
        public Builder connected(boolean connected) { this.connected = connected; return this; }
        public Builder connectionMessage(String connectionMessage) { this.connectionMessage = connectionMessage; return this; }
        public Builder lastVerifiedAt(Instant lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public PlatformConnection build() {
            return new PlatformConnection(id, userId, platform, platformUsername, encryptedAuthToken, connected, connectionMessage, lastVerifiedAt, createdAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public PlatformEnum getPlatform() { return platform; }
    public void setPlatform(PlatformEnum platform) { this.platform = platform; }

    public String getPlatformUsername() { return platformUsername; }
    public void setPlatformUsername(String platformUsername) { this.platformUsername = platformUsername; }

    public String getEncryptedAuthToken() { return encryptedAuthToken; }
    public void setEncryptedAuthToken(String encryptedAuthToken) { this.encryptedAuthToken = encryptedAuthToken; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public String getConnectionMessage() { return connectionMessage; }
    public void setConnectionMessage(String connectionMessage) { this.connectionMessage = connectionMessage; }

    public Instant getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(Instant lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

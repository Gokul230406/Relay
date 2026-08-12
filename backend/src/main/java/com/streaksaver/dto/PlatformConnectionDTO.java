package com.streaksaver.dto;

import com.streaksaver.model.PlatformEnum;

public class PlatformConnectionDTO {
    private PlatformEnum platform;
    private String platformUsername;
    private boolean connected;
    private String connectionMessage;

    public PlatformConnectionDTO() {}

    public PlatformConnectionDTO(PlatformEnum platform, String platformUsername, boolean connected, String connectionMessage) {
        this.platform = platform;
        this.platformUsername = platformUsername;
        this.connected = connected;
        this.connectionMessage = connectionMessage;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private PlatformEnum platform;
        private String platformUsername;
        private boolean connected;
        private String connectionMessage;

        public Builder platform(PlatformEnum platform) { this.platform = platform; return this; }
        public Builder platformUsername(String platformUsername) { this.platformUsername = platformUsername; return this; }
        public Builder connected(boolean connected) { this.connected = connected; return this; }
        public Builder connectionMessage(String connectionMessage) { this.connectionMessage = connectionMessage; return this; }

        public PlatformConnectionDTO build() {
            return new PlatformConnectionDTO(platform, platformUsername, connected, connectionMessage);
        }
    }

    public PlatformEnum getPlatform() { return platform; }
    public String getPlatformUsername() { return platformUsername; }
    public boolean isConnected() { return connected; }
    public String getConnectionMessage() { return connectionMessage; }
}

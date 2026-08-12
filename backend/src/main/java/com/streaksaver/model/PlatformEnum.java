package com.streaksaver.model;

public enum PlatformEnum {
    LEETCODE("LeetCode"),
    CODECHEF("CodeChef"),
    GEEKSFORGEEKS("GeeksforGeeks");

    private final String displayName;

    PlatformEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

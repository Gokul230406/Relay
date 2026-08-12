package com.streaksaver.streak;

import com.streaksaver.model.PlatformEnum;

public class DecisionResult {
    private boolean submissionRequired;
    private PlatformEnum selectedPlatform;
    private String reason;

    public DecisionResult() {}

    public DecisionResult(boolean submissionRequired, PlatformEnum selectedPlatform, String reason) {
        this.submissionRequired = submissionRequired;
        this.selectedPlatform = selectedPlatform;
        this.reason = reason;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean submissionRequired;
        private PlatformEnum selectedPlatform;
        private String reason;

        public Builder submissionRequired(boolean submissionRequired) { this.submissionRequired = submissionRequired; return this; }
        public Builder selectedPlatform(PlatformEnum selectedPlatform) { this.selectedPlatform = selectedPlatform; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }

        public DecisionResult build() {
            return new DecisionResult(submissionRequired, selectedPlatform, reason);
        }
    }

    public boolean isSubmissionRequired() { return submissionRequired; }
    public PlatformEnum getSelectedPlatform() { return selectedPlatform; }
    public String getReason() { return reason; }
}

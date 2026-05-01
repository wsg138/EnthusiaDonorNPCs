package com.enthusiasmpvp.donornpcs;

import java.time.Instant;

public final class UpdateStatus {
    private LeaderboardEntry entry;
    private String lastPlaceholderValue = "";
    private String lastDesiredSkinName = "";
    private String lastAppliedSkinName = "";
    private boolean lastSuccessful;
    private String lastMessage = "Not updated yet";
    private Instant lastAttemptAt;
    private Instant lastSuccessAt;

    public UpdateStatus(LeaderboardEntry entry) {
        this.entry = entry;
    }

    public LeaderboardEntry entry() {
        return entry;
    }

    public void setEntry(LeaderboardEntry entry) {
        this.entry = entry;
    }

    public String lastPlaceholderValue() {
        return lastPlaceholderValue;
    }

    public String lastDesiredSkinName() {
        return lastDesiredSkinName;
    }

    public String lastAppliedSkinName() {
        return lastAppliedSkinName;
    }

    public boolean lastSuccessful() {
        return lastSuccessful;
    }

    public String lastMessage() {
        return lastMessage;
    }

    public Instant lastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant lastSuccessAt() {
        return lastSuccessAt;
    }

    public void markSkipped(String placeholderValue, String desiredSkinName, String message) {
        this.lastPlaceholderValue = safe(placeholderValue);
        this.lastDesiredSkinName = safe(desiredSkinName);
        this.lastSuccessful = true;
        this.lastMessage = message;
        this.lastAttemptAt = Instant.now();
    }

    public void markSuccess(String placeholderValue, String desiredSkinName, String message) {
        this.lastPlaceholderValue = safe(placeholderValue);
        this.lastDesiredSkinName = safe(desiredSkinName);
        this.lastAppliedSkinName = safe(desiredSkinName);
        this.lastSuccessful = true;
        this.lastMessage = message;
        this.lastAttemptAt = Instant.now();
        this.lastSuccessAt = this.lastAttemptAt;
    }

    public void markFailure(String placeholderValue, String desiredSkinName, String message) {
        this.lastPlaceholderValue = safe(placeholderValue);
        this.lastDesiredSkinName = safe(desiredSkinName);
        this.lastSuccessful = false;
        this.lastMessage = message;
        this.lastAttemptAt = Instant.now();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

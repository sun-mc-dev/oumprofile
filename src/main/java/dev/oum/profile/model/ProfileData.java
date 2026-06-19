package dev.oum.profile.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ProfileData {

    private final String name;
    private final long createdAt;
    private long lastUsed;
    private PlayerState state;
    private double balance;
    private String primaryGroup;
    private String groupsJson;

    public ProfileData(@NonNull String name, long createdAt, long lastUsed, @NonNull PlayerState state,
                       double balance, @Nullable String primaryGroup, @Nullable String groupsJson) {
        this.name = name;
        this.createdAt = createdAt;
        this.lastUsed = lastUsed;
        this.state = state;
        this.balance = balance;
        this.primaryGroup = primaryGroup;
        this.groupsJson = groupsJson;
    }

    public static @NonNull ProfileData fresh(@NonNull String name) {
        long now = System.currentTimeMillis();
        return new ProfileData(name, now, now, PlayerState.fresh(), 0.0, "default", "[\"default\"]");
    }

    public @NonNull String name() {
        return name;
    }

    public long createdAt() {
        return createdAt;
    }

    public long lastUsed() {
        return lastUsed;
    }

    public @NonNull PlayerState state() {
        return state;
    }

    public double balance() {
        return balance;
    }

    public @Nullable String primaryGroup() {
        return primaryGroup;
    }

    public @Nullable String groupsJson() {
        return groupsJson;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public void setState(@NonNull PlayerState state) {
        this.state = state;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setPrimaryGroup(@Nullable String primaryGroup) {
        this.primaryGroup = primaryGroup;
    }

    public void setGroupsJson(@Nullable String groupsJson) {
        this.groupsJson = groupsJson;
    }
}
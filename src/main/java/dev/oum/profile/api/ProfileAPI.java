package dev.oum.profile.api;

import dev.oum.oumlib.scheduler.Promise;
import dev.oum.profile.model.ProfileData;
import dev.oum.profile.profile.ProfileManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@ApiStatus.Experimental
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class ProfileAPI {

    private static ProfileManager manager;

    private ProfileAPI() {
    }

    public static void init(@NonNull ProfileManager m) {
        manager = m;
    }

    private static @NonNull ProfileManager manager() {
        if (manager == null) throw new IllegalStateException("OumProfile is not initialized yet.");
        return manager;
    }

    /**
     * Gets all loaded profiles for a player UUID.
     *
     * @param uuid The player UUID.
     * @return Map of profile names to ProfileData.
     */
    public static @NonNull Map<String, ProfileData> getProfiles(@NonNull UUID uuid) {
        return manager().getProfiles(uuid);
    }

    /**
     * Gets the name of the player's active profile.
     *
     * @param uuid The player UUID.
     * @return The active profile name, or null if not loaded.
     */
    public static @Nullable String getActiveProfileName(@NonNull UUID uuid) {
        return manager().getActiveProfileName(uuid);
    }

    /**
     * Gets the full ProfileData of the player's currently active profile.
     *
     * @param uuid The player UUID.
     * @return The active ProfileData, or null if not loaded.
     */
    public static @Nullable ProfileData getActiveProfile(@NonNull UUID uuid) {
        String activeName = getActiveProfileName(uuid);
        if (activeName == null) return null;
        return getProfiles(uuid).get(activeName);
    }

    /**
     * Checks if a player has a profile with the specified name.
     *
     * @param uuid The player UUID.
     * @param name The profile name.
     * @return True if they have the profile, false otherwise.
     */
    public static boolean hasProfile(@NonNull UUID uuid, @NonNull String name) {
        return manager().hasProfile(uuid, name);
    }

    /**
     * Checks asynchronously if a profile with the specified name exists in the database.
     * Useful for querying offline player profiles.
     *
     * @param uuid The player UUID.
     * @param name The profile name.
     * @return A Promise completing with true if the profile exists, false otherwise.
     */
    public static @NonNull Promise<Boolean> existsInDatabase(@NonNull UUID uuid, @NonNull String name) {
        return manager().existsInDatabase(uuid, name);
    }

    /**
     * Creates a new profile for a player.
     *
     * @param player The player.
     * @param name   The profile name to create.
     * @return True if creation succeeded, false otherwise.
     */
    public static boolean createProfile(@NonNull Player player, @NonNull String name) {
        return manager().createProfile(player, name);
    }

    /**
     * Deletes a profile for a player.
     *
     * @param player The player.
     * @param name   The profile name to delete.
     * @return True if deletion succeeded, false otherwise.
     */
    public static boolean deleteProfile(@NonNull Player player, @NonNull String name) {
        return manager().deleteProfile(player, name);
    }

    /**
     * Triggers a profile switch request for a player, applying warmups, combat tags, and cooldowns.
     *
     * @param player The player.
     * @param target The target profile name.
     */
    public static void switchProfile(@NonNull Player player, @NonNull String target) {
        manager().requestSwitch(player, target);
    }

    /**
     * Checks if a player currently has an active warmup switch countdown running.
     *
     * @param uuid The player UUID.
     * @return True if they have a pending warmup, false otherwise.
     */
    public static boolean hasPendingWarmup(@NonNull UUID uuid) {
        return manager().hasPendingWarmup(uuid);
    }

    /**
     * Force-cancels any active profile switch warmup countdown for a player.
     *
     * @param uuid The player UUID.
     */
    public static void cancelWarmup(@NonNull UUID uuid) {
        manager().cancelWarmup(uuid);
    }

    /**
     * Gets the saved economy balance on a specific profile.
     *
     * @param uuid The player UUID.
     * @param name The profile name.
     * @return The balance amount, or 0.0 if not found.
     */
    public static double getProfileBalance(@NonNull UUID uuid, @NonNull String name) {
        ProfileData data = getProfiles(uuid).get(name);
        return data != null ? data.balance() : 0.0;
    }

    /**
     * Sets the saved economy balance on a specific profile.
     * Note: This does not instantly change the active player's Vault balance if they are currently playing on that profile.
     *
     * @param uuid    The player UUID.
     * @param name    The profile name.
     * @param balance The new balance value.
     */
    public static void setProfileBalance(@NonNull UUID uuid, @NonNull String name, double balance) {
        ProfileData data = getProfiles(uuid).get(name);
        if (data != null) {
            data.setBalance(balance);
        }
    }

    /**
     * Gets the last active millisecond timestamp of a profile.
     *
     * @param uuid The player UUID.
     * @param name The profile name.
     * @return Epoch millisecond timestamp, or 0 if not found.
     */
    public static long getProfileLastUsed(@NonNull UUID uuid, @NonNull String name) {
        ProfileData data = getProfiles(uuid).get(name);
        return data != null ? data.lastUsed() : 0L;
    }

    /**
     * Gets the maximum number of profiles a player is allowed to create.
     *
     * @param player The player.
     * @return Maximum profile limit integer.
     */
    public static int getMaxProfiles(@NonNull Player player) {
        return manager().getMaxProfiles(player);
    }
}
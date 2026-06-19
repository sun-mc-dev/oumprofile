package dev.oum.profile.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.bridge.economy.EconomyBridge;
import dev.oum.oumlib.bridge.permission.PermissionBridge;
import dev.oum.oumlib.config.ConfigManager;
import dev.oum.oumlib.effect.Sounds;
import dev.oum.oumlib.scheduler.Promise;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.util.Cooldown;
import dev.oum.profile.api.event.*;
import dev.oum.profile.command.Permissions;
import dev.oum.profile.config.ProfileConfig;
import dev.oum.profile.config.ProfileStorage;
import dev.oum.profile.model.PlayerState;
import dev.oum.profile.model.ProfileData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileManager {

    private static final Gson GSON = new Gson();
    private static final Type STRING_LIST = new TypeToken<List<String>>() {
    }.getType();

    private final ConfigManager<ProfileConfig> configManager;
    private final ProfileStorage storage;
    private final Map<UUID, Map<String, ProfileData>> cache = new ConcurrentHashMap<>();
    private final Map<UUID, String> active = new ConcurrentHashMap<>();
    private final Map<UUID, TaskHandle> warmups = new ConcurrentHashMap<>();
    private final Set<UUID> mutedAlerts = ConcurrentHashMap.newKeySet();
    private Cooldown combatCooldown;
    private Cooldown switchCooldown;

    public ProfileManager(@NonNull ConfigManager<ProfileConfig> configManager, @NonNull ProfileStorage storage) {
        this.configManager = configManager;
        this.storage = storage;
        this.combatCooldown = Cooldown.of(Duration.ofSeconds(configManager.get().switching().combatTagDuration()));
        this.switchCooldown = Cooldown.of(Duration.ofSeconds(configManager.get().switching().switchCooldownSeconds()));
        configManager.onReload(newConfig -> {
            this.combatCooldown = Cooldown.of(Duration.ofSeconds(newConfig.switching().combatTagDuration()));
            this.switchCooldown = Cooldown.of(Duration.ofSeconds(newConfig.switching().switchCooldownSeconds()));
        });
    }

    public boolean toggleAlerts(@NonNull UUID uuid) {
        if (mutedAlerts.contains(uuid)) {
            mutedAlerts.remove(uuid);
            return true;
        } else {
            mutedAlerts.add(uuid);
            return false;
        }
    }

    public boolean wantsAlerts(@NonNull UUID uuid) {
        return !mutedAlerts.contains(uuid);
    }

    public void sendAlert(@NonNull String alertMessage, Object... placeholders) {
        if (!configManager.get().adminAlertsEnabled()) return;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (Permissions.ALERTS.has(online) && wantsAlerts(online.getUniqueId())) {
                Text.send(online, alertMessage, placeholders);
            }
        }
    }

    public @NonNull ConfigManager<ProfileConfig> configManager() {
        return configManager;
    }

    public @NonNull Cooldown combatCooldown() {
        return combatCooldown;
    }

    public void loadPlayer(@NonNull Player player) {
        UUID uuid = player.getUniqueId();
        OumLib.logDebug("Loading profiles for player " + player.getName() + " (" + uuid + ")");
        storage.loadAll(uuid).thenAccept(list -> Scheduler.runFor(player, () -> {
            Map<String, ProfileData> map = new ConcurrentHashMap<>();
            for (ProfileData data : list) {
                map.put(data.name(), data);
            }
            String defaultName = configManager.get().defaultProfileName();
            if (map.isEmpty()) {
                OumLib.logDebug("No profiles found for " + player.getName() + ". Creating default profile: " + defaultName);
                ProfileData def = ProfileData.fresh(defaultName);
                map.put(defaultName, def);
                storage.save(uuid, def);
            }
            cache.put(uuid, map);
            String activeName = null;
            long maxLastUsed = -1;
            for (ProfileData data : map.values()) {
                if (data.lastUsed() > maxLastUsed) {
                    maxLastUsed = data.lastUsed();
                    activeName = data.name();
                }
            }
            if (activeName == null) {
                activeName = defaultName;
            }
            active.put(uuid, activeName);
            OumLib.logDebug("Loaded " + map.size() + " profiles for " + player.getName() + ". Active profile: " + activeName);
            ProfileData toApply = map.get(activeName);
            player.closeInventory();
            toApply.state().apply(player, configManager.get().switching().saveLocation());

            Bukkit.getPluginManager().callEvent(new ProfileLoadEvent(player, activeName));
        }));
    }

    public void unloadPlayer(@NonNull Player player) {
        UUID uuid = player.getUniqueId();
        OumLib.logDebug("Unloading profiles for player " + player.getName() + " (" + uuid + ")");
        cancelWarmup(uuid);
        Map<String, ProfileData> map = cache.get(uuid);
        String activeName = active.get(uuid);
        if (map == null || activeName == null) return;

        ProfileData data = map.get(activeName);
        if (data == null) return;

        player.closeInventory();
        captureLiveState(player, data, configManager.get().switching().saveLocation());
        storage.save(uuid, data);
        OumLib.logDebug("Saved active profile '" + activeName + "' data for player " + player.getName());

        cache.remove(uuid);
        active.remove(uuid);
        mutedAlerts.remove(uuid);
    }

    public void shutdown() {
        OumLib.logDebug("Shutting down ProfileManager. Saving all online players' profile states.");
        for (UUID uuid : warmups.keySet()) {
            cancelWarmup(uuid);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Map<String, ProfileData> map = cache.get(uuid);
            String activeName = active.get(uuid);
            if (map != null && activeName != null) {
                ProfileData data = map.get(activeName);
                if (data != null) {
                    try {
                        captureLiveState(player, data, configManager.get().switching().saveLocation());
                        storage.save(uuid, data).toCompletableFuture().join();
                        OumLib.logDebug("Saved active profile '" + activeName + "' for player " + player.getName() + " on shutdown.");
                    } catch (Exception e) {
                        OumLib.logError("Failed to save active profile for player " + player.getName() + " on shutdown", e);
                    }
                }
            }
        }
        cache.clear();
        active.clear();
        mutedAlerts.clear();
    }

    private void captureLiveState(@NonNull Player player, @NonNull ProfileData data, boolean saveLocation) {
        data.setState(PlayerState.capture(player, saveLocation));
        data.setBalance(EconomyBridge.balance(player));
        data.setLastUsed(System.currentTimeMillis());

        if (PermissionBridge.isAvailable()) {
            String primary = PermissionBridge.getPrimaryGroup(player.getUniqueId());
            data.setPrimaryGroup(primary);
            List<String> groups = PermissionBridge.getGroups(player.getUniqueId());
            if (groups != null) {
                data.setGroupsJson(GSON.toJson(groups));
            }
        }
        OumLib.logDebug("Captured state for player " + player.getName() + " on profile " + data.name() + " (Balance: " + data.balance() + ", Group: " + data.primaryGroup() + ")");
    }

    public @NonNull Map<String, ProfileData> getProfiles(@NonNull UUID uuid) {
        return cache.getOrDefault(uuid, Map.of());
    }

    public @Nullable String getActiveProfileName(@NonNull UUID uuid) {
        return active.get(uuid);
    }

    public boolean hasProfile(@NonNull UUID uuid, @NonNull String name) {
        return getProfiles(uuid).containsKey(name);
    }

    public @NonNull Promise<Boolean> existsInDatabase(@NonNull UUID uuid, @NonNull String name) {
        return storage.exists(uuid, name);
    }

    public int getMaxProfiles(@NonNull Player player) {
        if (player.hasPermission(Permissions.MAX_UNLIMITED)) return Integer.MAX_VALUE;
        int max = 1;
        List<Integer> tiers = configManager.get().limitTiers();
        if (tiers != null) {
            for (int tier : tiers) {
                if (player.hasPermission(Permissions.MAX_TIER_PREFIX + tier)) max = Math.max(max, tier);
            }
        }
        return max;
    }

    public boolean createProfile(@NonNull Player player, @NonNull String name) {
        UUID uuid = player.getUniqueId();
        OumLib.logDebug("Attempting to create profile '" + name + "' for player " + player.getName());
        Map<String, ProfileData> map = cache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        if (map.containsKey(name)) {
            OumLib.logDebug("Profile creation failed: Profile '" + name + "' already exists for " + player.getName());
            return false;
        }
        if (map.size() >= getMaxProfiles(player)) {
            OumLib.logDebug("Profile creation failed: Profile limit reached for " + player.getName());
            return false;
        }

        ProfileCreateEvent createEvent = new ProfileCreateEvent(player, name);
        Bukkit.getPluginManager().callEvent(createEvent);
        if (createEvent.isCancelled()) {
            OumLib.logDebug("Profile creation cancelled by API event handler.");
            return false;
        }

        ProfileData data = ProfileData.fresh(name);
        map.put(name, data);
        storage.save(uuid, data);
        OumLib.logDebug("Profile '" + name + "' created and saved for player " + player.getName());
        sendAlert(configManager.get().messages().adminAlertCreate(), "player", player.getName(), "name", name);
        return true;
    }

    public boolean deleteProfile(@NonNull Player player, @NonNull String name) {
        UUID uuid = player.getUniqueId();
        OumLib.logDebug("Attempting to delete profile '" + name + "' for player " + player.getName());
        Map<String, ProfileData> map = cache.get(uuid);
        if (map == null || !map.containsKey(name)) {
            OumLib.logDebug("Profile deletion failed: Profile '" + name + "' not found for " + player.getName());
            return false;
        }
        if (name.equals(active.get(uuid))) {
            OumLib.logDebug("Profile deletion failed: Cannot delete active profile '" + name + "' for " + player.getName());
            return false;
        }
        if (name.equalsIgnoreCase(configManager.get().defaultProfileName())) {
            OumLib.logDebug("Profile deletion failed: Cannot delete default profile '" + name + "' for " + player.getName());
            return false;
        }
        if (map.size() <= 1) {
            OumLib.logDebug("Profile deletion failed: Player " + player.getName() + " only has 1 profile remaining");
            return false;
        }

        ProfileDeleteEvent deleteEvent = new ProfileDeleteEvent(player, name);
        Bukkit.getPluginManager().callEvent(deleteEvent);
        if (deleteEvent.isCancelled()) {
            OumLib.logDebug("Profile deletion cancelled by API event handler.");
            return false;
        }

        map.remove(name);
        storage.delete(uuid, name);
        OumLib.logDebug("Profile '" + name + "' deleted successfully for player " + player.getName());
        sendAlert(configManager.get().messages().adminAlertDelete(), "player", player.getName(), "name", name);
        return true;
    }

    public void requestSwitch(@NonNull Player player, @NonNull String target) {
        UUID uuid = player.getUniqueId();
        String currentName = active.get(uuid);
        OumLib.logDebug("Player " + player.getName() + " requested profile switch from '" + currentName + "' to '" + target + "'");

        if (!hasProfile(uuid, target)) {
            Text.send(player, configManager.get().messages().profileNotFound(), "target", target);
            return;
        }
        if (target.equals(currentName)) {
            Text.send(player, configManager.get().messages().profileAlreadyActive());
            return;
        }
        if (switchCooldown.isOnCooldown(uuid) && !player.hasPermission(Permissions.BYPASS_COOLDOWN)) {
            String seconds = String.format("%.1f", switchCooldown.remainingSecondsDouble(uuid));
            Text.send(player, configManager.get().messages().switchCooldown(), "seconds", seconds);
            return;
        }
        if (configManager.get().switching().cancelInCombat() && combatCooldown.isOnCooldown(uuid) && !player.hasPermission(Permissions.BYPASS_COMBAT)) {
            Text.send(player, configManager.get().messages().combatBlock());
            return;
        }

        ProfileSwitchEvent preEvent = new ProfileSwitchEvent(player, currentName, target);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            OumLib.logDebug("Profile switch pre-event cancelled by API handler.");
            return;
        }

        boolean bypassWarmup = player.hasPermission(Permissions.BYPASS_WARMUP);
        if (!configManager.get().switching().warmupEnabled() || bypassWarmup) {
            OumLib.logDebug("Bypassing warmup for player " + player.getName() + " (Warmup config disabled: "
                    + !configManager.get().switching().warmupEnabled() + ", Permission bypass: " + bypassWarmup + ")");
            performSwitch(player, target);
            return;
        }

        cancelWarmup(uuid);
        int seconds = configManager.get().switching().warmupSeconds();
        OumLib.logDebug("Starting switch warmup of " + seconds + "s for player " + player.getName());
        Text.send(player, configManager.get().messages().warmupStart(), "target", target, "seconds", String.valueOf(seconds));

        final int[] remaining = {seconds};
        showWarmupTick(player, remaining[0], target);

        TaskHandle handle = Scheduler.runRepeating(Duration.ofSeconds(1), Duration.ofSeconds(1), () -> {
            remaining[0]--;
            if (remaining[0] <= 0) {
                OumLib.logDebug("Warmup completed for player " + player.getName() + ". Swapping profile data.");
                TaskHandle h = warmups.remove(uuid);
                if (h != null) {
                    h.cancel();
                }
                showWarmupComplete(player);
                performSwitch(player, target);
            } else {
                showWarmupTick(player, remaining[0], target);
            }
        });
        warmups.put(uuid, handle);
    }

    public void cancelWarmup(@NonNull UUID uuid) {
        cancelWarmup(uuid, null);
    }

    public void cancelWarmup(@NonNull UUID uuid, @Nullable String reasonMessage) {
        TaskHandle handle = warmups.remove(uuid);
        if (handle != null) {
            OumLib.logDebug("Warmup cancelled for player/UUID: " + uuid);
            handle.cancel();

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                showWarmupCancel(player);
                if (reasonMessage != null && !reasonMessage.isEmpty()) {
                    Text.send(player, reasonMessage);
                }
            }
        }
    }

    public boolean hasPendingWarmup(@NonNull UUID uuid) {
        return warmups.containsKey(uuid);
    }

    private void showWarmupTick(@NonNull Player player, int remaining, @NonNull String target) {
        var cfg = configManager.get().switching();
        if (cfg.warmupTitleEnabled() && cfg.warmupTitleText() != null) {
            String title = cfg.warmupTitleText()
                    .replace("<seconds>", String.valueOf(remaining))
                    .replace("<target>", target);
            String subtitle = cfg.warmupSubtitleText() != null
                    ? cfg.warmupSubtitleText().replace("<seconds>", String.valueOf(remaining)).replace("<target>", target)
                    : "";

            Text.title(player, title, subtitle, Duration.ofMillis(0), Duration.ofMillis(1100), Duration.ofMillis(100));
        }

        if (cfg.warmupSoundEnabled() && cfg.warmupSoundKey() != null && !cfg.warmupSoundKey().isEmpty()) {
            try {
                Sounds.play(player, cfg.warmupSoundKey(), 1.0f, 1.0f);
            } catch (Exception ignored) {
            }
        }
    }

    private void showWarmupComplete(@NonNull Player player) {
        var cfg = configManager.get().switching();
        if (cfg.warmupTitleEnabled()) {
            player.clearTitle();
        }
        if (cfg.warmupSoundEnabled() && cfg.warmupCompleteSoundKey() != null && !cfg.warmupCompleteSoundKey().isEmpty()) {
            try {
                Sounds.play(player, cfg.warmupCompleteSoundKey(), 1.0f, 1.0f);
            } catch (Exception ignored) {
            }
        }
    }

    private void showWarmupCancel(@NonNull Player player) {
        var cfg = configManager.get().switching();
        if (cfg.warmupTitleEnabled()) {
            player.clearTitle();
        }
        if (cfg.warmupSoundEnabled() && cfg.warmupCancelSoundKey() != null && !cfg.warmupCancelSoundKey().isEmpty()) {
            try {
                Sounds.play(player, cfg.warmupCancelSoundKey(), 1.0f, 1.0f);
            } catch (Exception ignored) {
            }
        }
    }

    public void performSwitch(@NonNull Player player, @NonNull String target) {
        UUID uuid = player.getUniqueId();
        Scheduler.runFor(player, () -> {
            Map<String, ProfileData> map = cache.get(uuid);
            if (map == null) return;

            String currentName = active.get(uuid);
            ProfileData current = map.get(currentName);
            ProfileData targetData = map.get(target);
            if (current == null || targetData == null) return;

            ProfileSwitchEvent finalCheck = new ProfileSwitchEvent(player, currentName, target);
            Bukkit.getPluginManager().callEvent(finalCheck);
            if (finalCheck.isCancelled()) {
                OumLib.logDebug("Profile switch final-check event cancelled by API handler.");
                return;
            }

            player.closeInventory();
            OumLib.logDebug("Saving current live data for player " + player.getName() + " on profile " + currentName);
            captureLiveState(player, current, configManager.get().switching().saveLocation());
            storage.save(uuid, current);

            OumLib.logDebug("Applying profile '" + target + "' data state to player " + player.getName());
            targetData.state().apply(player, configManager.get().switching().saveLocation());
            targetData.setLastUsed(System.currentTimeMillis());

            double currentBalance = EconomyBridge.balance(player);
            double targetBalance = targetData.balance();
            double difference = targetBalance - currentBalance;

            if (difference > 0) {
                EconomyBridge.deposit(player, difference);
                OumLib.logDebug("Swapped balance for player " + player.getName() + " (deposited difference=" + difference + ")");
            } else if (difference < 0) {
                EconomyBridge.withdraw(player, -difference);
                OumLib.logDebug("Swapped balance for player " + player.getName() + " (withdrew difference=" + (-difference) + ")");
            } else {
                OumLib.logDebug("Swapped balance for player " + player.getName() + " (no change, balance=" + targetBalance + ")");
            }

            if (PermissionBridge.isAvailable() && targetData.groupsJson() != null) {
                List<String> groups = GSON.fromJson(targetData.groupsJson(), STRING_LIST);
                PermissionBridge.setGroups(uuid, groups, targetData.primaryGroup());
                OumLib.logDebug("Synchronized permission groups for player " + player.getName() + ": " + groups + " (primary=" + targetData.primaryGroup() + ")");
            }

            active.put(uuid, target);
            storage.save(uuid, targetData);
            switchCooldown.set(uuid);

            Text.send(player, configManager.get().messages().switchSuccess(), "target", target);
            OumLib.logDebug("Player " + player.getName() + " successfully switched to profile: " + target);
            sendAlert(configManager.get().messages().adminAlertSwitch(), "player", player.getName(), "target", target);
            Bukkit.getPluginManager().callEvent(new ProfilePostSwitchEvent(player, currentName, target));
        });
    }
}
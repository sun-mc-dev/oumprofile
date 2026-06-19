package dev.oum.profile;

import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import dev.oum.profile.profile.ProfileManager;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public final class ProfilePlaceholders {

    public static void register(@NonNull ProfileManager manager) {
        PlaceholderRegistry registry = new PlaceholderRegistry();
        registry.forNamespace("oumprofile")
                .add("active", obj -> {
                    if (obj instanceof Player player) {
                        String active = manager.getActiveProfileName(player.getUniqueId());
                        return active != null ? active : "";
                    }
                    return "";
                })
                .add("count", obj -> {
                    if (obj instanceof Player player) {
                        return String.valueOf(manager.getProfiles(player.getUniqueId()).size());
                    }
                    return "0";
                })
                .add("max", obj -> {
                    if (obj instanceof Player player) {
                        int max = manager.getMaxProfiles(player);
                        return max == Integer.MAX_VALUE ? "Unlimited" : String.valueOf(max);
                    }
                    return "0";
                })
                .add("balance", obj -> {
                    if (obj instanceof Player player) {
                        String active = manager.getActiveProfileName(player.getUniqueId());
                        if (active != null) {
                            var profile = manager.getProfiles(player.getUniqueId()).get(active);
                            if (profile != null) {
                                return String.format(Locale.ROOT, "%.2f", profile.balance());
                            }
                        }
                    }
                    return "0.00";
                })
                .add("group", obj -> {
                    if (obj instanceof Player player) {
                        String active = manager.getActiveProfileName(player.getUniqueId());
                        if (active != null) {
                            var profile = manager.getProfiles(player.getUniqueId()).get(active);
                            if (profile != null && profile.primaryGroup() != null) {
                                return profile.primaryGroup();
                            }
                        }
                    }
                    return "default";
                })
                .register();
    }
}
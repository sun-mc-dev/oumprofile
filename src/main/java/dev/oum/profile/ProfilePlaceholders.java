package dev.oum.profile;

import dev.oum.oumlib.text.Format;
import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import dev.oum.profile.integration.SkillData;
import dev.oum.profile.model.PlayerState;
import dev.oum.profile.model.ProfileData;
import dev.oum.profile.profile.ProfileManager;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public final class ProfilePlaceholders {

    private static PlaceholderRegistry registry;

    public static void register(@NonNull ProfileManager manager) {
        registry = new PlaceholderRegistry();
        registry.forNamespace("oumprofile")
                .add("active", obj -> {
                    if (!(obj instanceof Player player)) return "";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    return active != null ? active : "";
                })
                .add("count", obj -> {
                    if (!(obj instanceof Player player)) return "0";
                    return String.valueOf(manager.getProfiles(player.getUniqueId()).size());
                })
                .add("max", obj -> {
                    if (!(obj instanceof Player player)) return "0";
                    int max = manager.getMaxProfiles(player);
                    return max == Integer.MAX_VALUE ? "Unlimited" : String.valueOf(max);
                })
                .add("balance", obj -> {
                    if (!(obj instanceof Player player)) return "0.00";
                    ProfileData profile = manager.getActiveProfile(player.getUniqueId());
                    return profile != null ? String.format(Locale.ROOT, "%.2f", profile.balance()) : "0.00";
                })
                .add("group", obj -> {
                    if (!(obj instanceof Player player)) return "default";
                    ProfileData profile = manager.getActiveProfile(player.getUniqueId());
                    return (profile != null && profile.primaryGroup() != null) ? profile.primaryGroup() : "default";
                })
                .add("playtime", obj -> {
                    if (!(obj instanceof Player player)) return "0";
                    ProfileData profile = manager.getActiveProfile(player.getUniqueId());
                    if (profile == null) return "0";
                    long elapsed = manager.getElapsedSessionSeconds(player.getUniqueId());
                    long base = profile.state().playtimeSeconds() != null ? profile.state().playtimeSeconds() : 0L;
                    return String.valueOf(base + elapsed);
                })
                .add("playtime_formatted", obj -> {
                    if (!(obj instanceof Player player)) return "0s";
                    ProfileData profile = manager.getActiveProfile(player.getUniqueId());
                    if (profile == null) return "0s";
                    long elapsed = manager.getElapsedSessionSeconds(player.getUniqueId());
                    long base = profile.state().playtimeSeconds() != null ? profile.state().playtimeSeconds() : 0L;
                    return Format.duration(Duration.ofSeconds(base + elapsed));
                });

        var config = manager.config().main();
        if (config.economy() != null && config.economy().currencies() != null) {
            for (String currency : config.economy().currencies()) {
                registerCurrencyPlaceholder(currency, manager);
            }
        }

        registry.register();
    }

    public static void registerSkillPlaceholder(String pluginName, String skillName, ProfileManager manager) {
        String baseKey = "skill_" + pluginName.toLowerCase(Locale.ROOT) + "_" + skillName.toLowerCase(Locale.ROOT);
        registerSkillLevelAndXp(baseKey, manager, (state, key) -> {
            Map<String, SkillData> skillMap = pluginName.equalsIgnoreCase("mcmmo")
                    ? state.mcmmo() : state.auraskills();
            return skillMap != null ? skillMap.get(key) : null;
        }, skillName);
    }

    public static void registerJobPlaceholder(String jobName, ProfileManager manager) {
        String baseKey = "job_" + jobName.toLowerCase(Locale.ROOT);
        registerSkillLevelAndXp(baseKey, manager, (state, key) ->
                state.jobs() != null ? state.jobs().get(key) : null, jobName);
    }

    private static void registerSkillLevelAndXp(
            String baseKey,
            ProfileManager manager,
            BiFunction<PlayerState, String, SkillData> skillExtractor,
            String skillKey
    ) {
        if (registry == null) return;
        registry.forNamespace("oumprofile")
                .add(baseKey + "_level", obj -> {
                    if (!(obj instanceof Player player)) return "0";
                    ProfileData profile = manager.getActiveProfile(player.getUniqueId());
                    if (profile == null) return "0";
                    SkillData sd = skillExtractor.apply(profile.state(), skillKey);
                    return sd != null ? String.valueOf(sd.level()) : "0";
                })
                .add(baseKey + "_xp", obj -> {
                    if (!(obj instanceof Player player)) return "0.0";
                    ProfileData profile = manager.getActiveProfile(player.getUniqueId());
                    if (profile == null) return "0.0";
                    SkillData sd = skillExtractor.apply(profile.state(), skillKey);
                    return sd != null ? String.format(Locale.ROOT, "%.1f", sd.xp()) : "0.0";
                });
    }

    public static void registerCurrencyPlaceholder(String currency, ProfileManager manager) {
        if (registry == null) return;
        String key = "currency_" + currency.toLowerCase(Locale.ROOT);
        registry.forNamespace("oumprofile")
                .add(key, obj -> {
                    if (!(obj instanceof Player player)) return "0.00";
                    ProfileData profile = manager.getActiveProfile(player.getUniqueId());
                    if (profile == null || profile.state().currencies() == null) return "0.00";
                    double bal = profile.state().currencies().getOrDefault(currency, 0.0);
                    return String.format(Locale.ROOT, "%.2f", bal);
                });
    }
}
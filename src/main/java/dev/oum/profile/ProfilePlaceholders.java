package dev.oum.profile;

import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import dev.oum.oumlib.util.Format;
import dev.oum.profile.integration.SkillData;
import dev.oum.profile.profile.ProfileManager;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

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
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "0.00";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    return profile != null ? String.format(Locale.ROOT, "%.2f", profile.balance()) : "0.00";
                })
                .add("group", obj -> {
                    if (!(obj instanceof Player player)) return "default";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "default";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    return (profile != null && profile.primaryGroup() != null) ? profile.primaryGroup() : "default";
                })
                .add("playtime", obj -> {
                    if (!(obj instanceof Player player)) return "0";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "0";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    if (profile == null) return "0";
                    long elapsed = manager.getElapsedSessionSeconds(player.getUniqueId());
                    long base = profile.state().playtimeSeconds() != null ? profile.state().playtimeSeconds() : 0L;
                    return String.valueOf(base + elapsed);
                })
                .add("playtime_formatted", obj -> {
                    if (!(obj instanceof Player player)) return "0s";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "0s";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    if (profile == null) return "0s";
                    long elapsed = manager.getElapsedSessionSeconds(player.getUniqueId());
                    long base = profile.state().playtimeSeconds() != null ? profile.state().playtimeSeconds() : 0L;
                    return Format.duration(Duration.ofSeconds(base + elapsed));
                });

        var config = manager.configManager().get();
        if (config.economy() != null && config.economy().currencies() != null) {
            for (String currency : config.economy().currencies()) {
                registerCurrencyPlaceholder(currency, manager);
            }
        }

        registry.register();
    }

    public static void registerSkillPlaceholder(String pluginName, String skillName, ProfileManager manager) {
        if (registry == null) return;
        String baseKey = "skill_" + pluginName.toLowerCase(Locale.ROOT) + "_" + skillName.toLowerCase(Locale.ROOT);
        String lvlKey = baseKey + "_level";
        String xpKey = baseKey + "_xp";

        registry.forNamespace("oumprofile")
                .add(lvlKey, obj -> {
                    if (!(obj instanceof Player player)) return "0";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "0";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    if (profile == null) return "0";
                    Map<String, SkillData> skillMap = pluginName.equalsIgnoreCase("mcmmo")
                            ? profile.state().mcmmo() : profile.state().auraskills();
                    if (skillMap == null) return "0";
                    SkillData sd = skillMap.get(skillName);
                    return sd != null ? String.valueOf(sd.level()) : "0";
                })
                .add(xpKey, obj -> {
                    if (!(obj instanceof Player player)) return "0.0";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "0.0";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    if (profile == null) return "0.0";
                    Map<String, SkillData> skillMap = pluginName.equalsIgnoreCase("mcmmo")
                            ? profile.state().mcmmo() : profile.state().auraskills();
                    if (skillMap == null) return "0.0";
                    SkillData sd = skillMap.get(skillName);
                    return sd != null ? String.format(Locale.ROOT, "%.1f", sd.xp()) : "0.0";
                });
    }

    public static void registerJobPlaceholder(String jobName, ProfileManager manager) {
        if (registry == null) return;
        String baseKey = "job_" + jobName.toLowerCase(Locale.ROOT);
        String lvlKey = baseKey + "_level";
        String xpKey = baseKey + "_xp";

        registry.forNamespace("oumprofile")
                .add(lvlKey, obj -> {
                    if (!(obj instanceof Player player)) return "0";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "0";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    if (profile == null || profile.state().jobs() == null) return "0";
                    SkillData sd = profile.state().jobs().get(jobName);
                    return sd != null ? String.valueOf(sd.level()) : "0";
                })
                .add(xpKey, obj -> {
                    if (!(obj instanceof Player player)) return "0.0";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "0.0";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    if (profile == null || profile.state().jobs() == null) return "0.0";
                    SkillData sd = profile.state().jobs().get(jobName);
                    return sd != null ? String.format(Locale.ROOT, "%.1f", sd.xp()) : "0.0";
                });
    }

    public static void registerCurrencyPlaceholder(String currency, ProfileManager manager) {
        if (registry == null) return;
        String key = "currency_" + currency.toLowerCase(Locale.ROOT);
        registry.forNamespace("oumprofile")
                .add(key, obj -> {
                    if (!(obj instanceof Player player)) return "0.00";
                    String active = manager.getActiveProfileName(player.getUniqueId());
                    if (active == null) return "0.00";
                    var profile = manager.getProfiles(player.getUniqueId()).get(active);
                    if (profile == null || profile.state().currencies() == null) return "0.00";
                    double bal = profile.state().currencies().getOrDefault(currency, 0.0);
                    return String.format(Locale.ROOT, "%.2f", bal);
                });
    }
}
package dev.oum.profile.config;

import dev.oum.oumlib.config.Comment;
import dev.oum.oumlib.config.ConfigSection;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Comment({
        "   ___                   ___            __ _ _      ",
        "  ╱___╲_   _ _ __ ___   ╱ _ ╲_ __ ___  ╱ _(_) │ ___ ",
        " ╱╱  ╱╱ │ │ │ '_ ` _ ╲ ╱ ╱_)╱ '__╱ _ ╲│ │_│ │ │╱ _ ╲",
        "╱ ╲_╱╱│ │_│ │ │ │ │ │ ╱ ___╱│ │  (_) │  _│ │ │  __╱",
        "╲___╱  ╲__,_│_│ │_│ │_╲╱    │_│  ╲___╱│_│ │_│_│╲___│",
        "",
        "OumProfile Main Configuration",
        "Configure core mechanics, database storage, profile switching, and integrations.",
        "Menus are configured in 'menus.yml' and messages in 'messages.yml'."
})
public record MainConfig(
        @Comment("Enable detailed debug logging in console")
        boolean debug,

        @Comment("Name of the default profile created on first join")
        String defaultProfileName,

        @Comment("Global date format pattern")
        String dateFormat,

        @Comment("Enable administrative alerts when players switch, create, or delete profiles")
        boolean adminAlertsEnabled,

        @Comment("Maximum character length for profile names")
        int profileNameMaxLength,

        @Comment("Regex pattern for valid profile names")
        String profileNameRegex,

        @Comment("Max profile limits based on profiles.max.<tier> permission nodes")
        List<Integer> limitTiers,

        @Comment("Periodic background auto-save settings for active player profiles")
        AutoSaveSection autoSave,

        @Comment("Database storage settings (SQLite/MySQL)")
        StorageSection storage,

        @Comment("Profile switching mechanics, warmups, combat checks, and sounds")
        SwitchSection switching,

        @Comment("LuckPerms rank synchronization")
        LuckPermsSection luckperms,

        @Comment("Multi-currency economy settings")
        EconomySection economy,

        @Comment("Skill and Job synchronization settings")
        SkillSection skills,

        @Comment("Vanilla statistics synchronization settings")
        StatisticsSection statistics
) implements ConfigSection {

    @Contract(" -> new")
    public static @NonNull MainConfig defaults() {
        return new MainConfig(
                false,
                "default",
                "yyyy-MM-dd HH:mm",
                true,
                16,
                "[a-zA-Z0-9_-]+",
                List.of(1, 3, 5, 10),
                new AutoSaveSection(true, 5),
                new StorageSection("sqlite", "localhost", 3306, "oumprofile", "root", ""),
                new SwitchSection(
                        true, 5, true, true, true, 10, 10, false,
                        true, "<color:#74c7ec>Switching Profile...</color>", "<color:#9399b2>Do not move for <color:#f9e2af><seconds>s</color></color>",
                        true, "block.note_block.hat", "entity.player.levelup", "entity.villager.no"
                ),
                new LuckPermsSection(true),
                new EconomySection(true, List.of("vault", "playerpoints")),
                new SkillSection(true, true, true),
                new StatisticsSection(true, List.of("MOB_KILLS", "DEATHS", "JUMP"))
        );
    }

    public record AutoSaveSection(
            @Comment("Enable periodic background auto-save for online players")
            boolean enabled,

            @Comment("Auto-save interval in minutes (e.g. 5 for every 5 minutes)")
            int intervalMinutes
    ) implements ConfigSection {
        @Contract(" -> new")
        public static @NonNull AutoSaveSection defaults() {
            return new AutoSaveSection(true, 5);
        }
    }

    public record StorageSection(
            @Comment("Database type: 'sqlite' or 'mysql'")
            String type,
            @Comment("MySQL database hostname")
            String host,
            @Comment("MySQL port number")
            int port,
            @Comment("Database schema name")
            String database,
            @Comment("MySQL username")
            String username,
            @Comment("MySQL password")
            String password
    ) implements ConfigSection {
    }

    public record SwitchSection(
            @Comment("Enable countdown warmup duration when switching profiles")
            boolean warmupEnabled,
            @Comment("Warmup duration in seconds")
            int warmupSeconds,
            @Comment("Cancel warmups if the player moves")
            boolean cancelOnMove,
            @Comment("Cancel warmups if the player receives damage")
            boolean cancelOnDamage,
            @Comment("Cancel warmups if the player is in combat")
            boolean cancelInCombat,
            @Comment("Combat tag duration in seconds")
            int combatTagDuration,
            @Comment("Cooldown time in seconds before switching profiles again")
            int switchCooldownSeconds,
            @Comment("Save and restore coordinates/location per profile")
            boolean saveLocation,

            @Comment("Send title countdowns during profile switching")
            boolean warmupTitleEnabled,
            @Comment("Countdown title format (MiniMessage support, placeholder <seconds>)")
            String warmupTitleText,
            @Comment("Countdown subtitle format (MiniMessage support, placeholder <seconds>)")
            String warmupSubtitleText,
            @Comment("Play sound on each tick of the warmup countdown")
            boolean warmupSoundEnabled,
            @Comment("Warmup tick sound key")
            String warmupSoundKey,
            @Comment("Warmup completion sound key")
            String warmupCompleteSoundKey,
            @Comment("Warmup cancellation sound key")
            String warmupCancelSoundKey
    ) implements ConfigSection {
    }

    public record LuckPermsSection(
            @Comment("Synchronize permission groups using LuckPerms integration")
            boolean enabled
    ) implements ConfigSection {
    }

    public record EconomySection(
            @Comment("Enable multi-currency economy storage")
            boolean enabled,
            @Comment("List of currencies to save and restore per-profile (e.g. vault, playerpoints)")
            List<String> currencies
    ) implements ConfigSection {
    }

    public record SkillSection(
            @Comment("Enable mcMMO skill level synchronization")
            boolean mcmmoEnabled,
            @Comment("Enable AuraSkills/AureliumSkills level synchronization")
            boolean auraSkillsEnabled,
            @Comment("Enable JobsReborn job level synchronization")
            boolean jobsEnabled
    ) implements ConfigSection {
    }

    public record StatisticsSection(
            @Comment("Enable vanilla statistics synchronization")
            boolean enabled,
            @Comment("List of statistic names to save and restore per-profile")
            List<String> tracked
    ) implements ConfigSection {
    }
}

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
        "Welcome to OumProfile configuration!",
        "Below you can customize how player profiles behave on your server.",
        "Made by sun-plugins",
        "",
        "==================== QUICK SETUP GUIDE ====================",
        "",
        "1. DATABASE TYPE (storage.type):",
        "   - Use 'sqlite' for single-server setups (creates a local file in your plugin folder).",
        "   - Use 'mysql' if you are syncing profiles across multiple servers (requires a shared MySQL server).",
        "",
        "2. PROFILE SLOTS & LIMITS:",
        "   - By default, players can only have 1 profile.",
        "   - Grant players 'profiles.max.<number>' (e.g., profiles.max.3) to allow more profile slots.",
        "   - You can define slots tiers (like 1, 3, 5, 10) in the 'limit-tiers' list below.",
        "   - Give players 'profiles.max.unlimited' to bypass all slot limits.",
        "",
        "3. INTEGRATIONS:",
        "   - LuckPerms (luckperms.enabled): When active, switching profiles restores the player's saved rank group.",
        "   - Vault (automatically integrated): When active, each profile keeps its own separate money balance.",
        "",
        "4. ANTI-EXPLOIT (switching.cancel-on-move, cancel-in-combat):",
        "   - Keeps players from escaping PvP. If they are in combat or move during the switch warmup,",
        "     the switch gets cancelled.",
        "",
        "===========================================================",
        "",
        "Need help or found a bug? Join our Discord: https://discord.gg/maDcwPV6KB",
        "Or report on GitHub: https://github.com/sun-mc-dev/oumprofile/issues"
})
public record ProfileConfig(
        @Comment("Enable detailed debug logging in console")
        boolean debug,

        @Comment("Profile switching settings")
        SwitchSection switching,

        @Comment("Database storage settings (SQLite/MySQL)")
        StorageSection storage,

        @Comment("LuckPerms group integration")
        LuckPermsSection luckperms,

        @Comment("Custom plugin messages (MiniMessage tags allowed)")
        MessagesSection messages,

        @Comment("GUI menus and chat input settings")
        GuiSection gui,

        @Comment("Confirmation GUI settings for deleting profiles")
        ConfirmGuiSection confirmDelete,

        @Comment("Confirmation GUI settings for creating profiles")
        ConfirmGuiSection confirmCreate,

        @Comment("Max profile limits based on oumprofile.max.<tier> permission nodes")
        List<Integer> limitTiers,

        @Comment("Name of the default profile created on first join")
        String defaultProfileName,

        @Comment("Global date format pattern")
        String dateFormat,

        @Comment("Enable administrative alerts when players switch, create, or delete profiles")
        boolean adminAlertsEnabled
) implements ConfigSection {

    @Contract(" -> new")
    public static @NonNull ProfileConfig defaults() {
        return new ProfileConfig(
                false,
                new SwitchSection(
                        true, 5, true, true, true, 10, 10, false,
                        true, "<color:#74c7ec>Switching Profile...</color>", "<color:#9399b2>Do not move for <color:#f9e2af><seconds>s</color></color>",
                        true, "block.note_block.hat", "entity.player.levelup", "entity.villager.no"
                ),
                new StorageSection("sqlite", "localhost", 3306, "oumprofile", "root", ""),
                new LuckPermsSection(true),
                new MessagesSection(
                        "<color:#f38ba8>Profile <color:#fab387>'<target>'</color> does not exist.</color>",
                        "<color:#f38ba8>You are already using that profile.</color>",
                        "<color:#f38ba8>You cannot switch profiles while in combat.</color>",
                        "<color:#74c7ec>Switching to <color:#cba6f7><target></color> in <color:#fab387><seconds>s</color>...</color>",
                        "<color:#a6e3a1>Switched to profile <color:#cba6f7><target></color>.</color>",
                        "<color:#f38ba8>You don't have permission to create a profile named <color:#fab387>'<name>'</color>.</color>",
                        "<color:#f38ba8>Could not create profile <color:#fab387>'<name>'</color> (limit reached or name exists).</color>",
                        "<color:#a6e3a1>Created profile <color:#cba6f7><name></color>.</color>",
                        "<color:#f38ba8>Could not delete profile <color:#fab387>'<name>'</color> (active, last remaining, or not found).</color>",
                        "<color:#a6e3a1>Deleted profile <color:#cba6f7><name></color>.</color>",
                        "<color:#b4befe>OumProfile <color:#585b70>»</color> <color:#9399b2>/profile <list | current | create | switch | delete | reload></color></color>",
                        "<color:#74c7ec>Your profiles (<color:#fab387><count></color>):</color>",
                        "<color:#a6e3a1>● <color:#cdd6f4><name></color> <color:#585b70>—</color> <color:#9399b2>Active</color></color>",
                        "<color:#9399b2>○ <color:#a6adc8><name></color> <color:#585b70>—</color> <color:#6c7086>Last used <date></color></color>",
                        "<color:#b4befe>Active Profile: <color:#cba6f7><name></color></color>",
                        "<color:#f38ba8>This command must be run as a player.</color>",
                        "<color:#9399b2>You have no profiles.</color>",
                        "<color:#f38ba8>No active profile found.</color>",
                        "<color:#a6e3a1>Configuration reloaded successfully.</color>",
                        "<color:#f38ba8>Please wait <color:#fab387><seconds>s</color> before switching profiles again.</color>",
                        "<color:#f38ba8>You have reached your maximum profile slot limit.</color>",
                        "<color:#f38ba8>You cannot delete your active profile.</color>",
                        "<color:#f38ba8>You cannot delete your default profile.</color>",
                        "<color:#f38ba8>Profile name must not be empty or contain spaces.</color>",
                        "<color:#f38ba8>Profile creation cancelled.</color>",
                        "<color:#f38ba8>Profile creation timed out.</color>",
                        "<color:#f38ba8>Player not found.</color>",
                        "<color:#a6e3a1>Opened profile menu for <color:#cba6f7><target></color>.</color>",
                        "<color:#74c7ec>Profiles for <color:#cba6f7><target></color> (<color:#fab387><count></color>):</color>",
                        "<color:#a6e3a1>Successfully created profile <color:#fab387><name></color> for <color:#cba6f7><target></color>.</color>",
                        "<color:#f38ba8>Failed to create profile (already exists or limit reached).</color>",
                        "<color:#a6e3a1>Forced <color:#cba6f7><target></color> to switch to profile <color:#fab387><name></color>.</color>",
                        "<color:#f38ba8>Player does not have a profile named <color:#fab387>'<name>'</color>.</color>",
                        "<color:#a6e3a1>Successfully deleted profile <color:#fab387><name></color> for <color:#cba6f7><target></color>.</color>",
                        "<color:#f38ba8>Failed to delete profile (active, last remaining, or not found).</color>",
                        "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> switched to profile <color:#cba6f7><b><target></b></color></color>",
                        "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> created profile <color:#cba6f7><b><name></b></color></color>",
                        "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> deleted profile <color:#cba6f7><b><name></b></color></color>",
                        "<color:#a6e3a1>Profile alerts enabled.</color>",
                        "<color:#f38ba8>Profile alerts disabled.</color>",
                        "<color:#f38ba8>Profile switch cancelled because you moved.</color>",
                        "<color:#f38ba8>Profile switch cancelled because you took damage.</color>",
                        "<color:#f38ba8>Profile switch cancelled.</color>",
                        "<color:#a6e3a1>Debug mode enabled.</color>",
                        "<color:#f38ba8>Debug mode disabled.</color>"
                ),
                new GuiSection(
                        "<color:#5c5f77>Select a Profile</color>",
                        3,
                        List.of(
                                "#########",
                                "  PPPPP  ",
                                "####C####"
                        ),
                        "EMERALD",
                        "BARRIER",
                        "<color:#a6e3a1><b>Create New Profile</b></color>",
                        "<color:#f38ba8><b>Profile Limit Reached</b></color>",
                        List.of(
                                "<color:#9399b2>Slots: <color:#f9e2af><slots_current></color> / <color:#9399b2><slots_max></color>",
                                "",
                                "<color:#a6e3a1>Click to start profile creation</color>"
                        ),
                        List.of(
                                "<color:#9399b2>Slots: <color:#f9e2af><slots_current></color> / <color:#9399b2><slots_max></color>",
                                "",
                                "<color:#f38ba8>Purchase more slots on our store</color>"
                        ),
                        "BOOK",
                        "WRITTEN_BOOK",
                        "LIGHT_GRAY_STAINED_GLASS_PANE",
                        "<color:#a6e3a1><b><name></b></color> <color:#9399b2>(Active)</color>",
                        "<color:#cba6f7><b><name></b></color>",
                        "<color:#585b70>Empty Slot</color>",
                        List.of(
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#9399b2>Created: <color:#cdd6f4><created></color></color>",
                                "<color:#9399b2>Last Used: <color:#cdd6f4><last_used></color></color>",
                                "<color:#9399b2>Balance: <color:#f9e2af>$<balance></color></color>",
                                "<color:#9399b2>Rank Group: <color:#b4befe><group></color></color>",
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#a6e3a1>Currently Active</color>"
                        ),
                        List.of(
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#9399b2>Created: <color:#cdd6f4><created></color></color>",
                                "<color:#9399b2>Last Used: <color:#cdd6f4><last_used></color></color>",
                                "<color:#9399b2>Balance: <color:#f9e2af>$<balance></color></color>",
                                "<color:#9399b2>Rank Group: <color:#b4befe><group></color></color>",
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#74c7ec>Left-Click to switch</color>",
                                "<color:#f38ba8>Right-Click to delete</color>"
                        ),
                        "GRAY_STAINED_GLASS_PANE",
                        " ",
                        "<color:#cba6f7><b>Profile Creation</b></color>\n<color:#9399b2>Type a name in chat for your new profile.\nType <color:#f38ba8><b>cancel</b></color> to return.</color>",
                        "cancel",
                        30,
                        "P",
                        "C",
                        true,
                        "block.chest.open",
                        true,
                        "ui.button.click",
                        true,
                        "entity.villager.no",
                        true,
                        "block.chest.close"
                ),
                new ConfirmGuiSection(
                        true,
                        "<color:#f38ba8>Confirm Deleting <profile></color>",
                        3,
                        List.of(
                                "#########",
                                "  C   D  ",
                                "#########"
                        ),
                        "C",
                        "RED_WOOL",
                        "<color:#f38ba8><b>Confirm Deletion</b></color>",
                        List.of(
                                "<color:#a6adc8>Clicking here will permanently</color>",
                                "<color:#a6adc8>delete the profile <color:#fab387><profile></color>.</color>",
                                "",
                                "<color:#f38ba8><b>WARNING: This cannot be undone!</b></color>"
                        ),
                        "D",
                        "GREEN_WOOL",
                        "<color:#a6e3a1><b>Cancel</b></color>",
                        List.of(
                                "<color:#a6adc8>Click to keep your profile</color>",
                                "<color:#a6adc8>and return to the menu.</color>"
                        ),
                        "GRAY_STAINED_GLASS_PANE",
                        " "
                ),
                new ConfirmGuiSection(
                        true,
                        "<color:#a6e3a1>Confirm Creating <name></color>",
                        3,
                        List.of(
                                "#########",
                                "  C   D  ",
                                "#########"
                        ),
                        "C",
                        "GREEN_WOOL",
                        "<color:#a6e3a1><b>Confirm Creation</b></color>",
                        List.of(
                                "<color:#a6adc8>Click here to create</color>",
                                "<color:#a6adc8>profile <color:#cba6f7><name></color>.</color>"
                        ),
                        "D",
                        "RED_WOOL",
                        "<color:#f38ba8><b>Cancel</b></color>",
                        List.of(
                                "<color:#a6adc8>Click to cancel creation</color>",
                                "<color:#a6adc8>and return to the menu.</color>"
                        ),
                        "GRAY_STAINED_GLASS_PANE",
                        " "
                ),
                List.of(1, 3, 5, 10),
                "default",
                "yyyy-MM-dd HH:mm",
                true
        );
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

    public record LuckPermsSection(
            @Comment("Synchronize permission groups using LuckPerms integration")
            boolean enabled
    ) implements ConfigSection {
    }

    public record MessagesSection(
            String profileNotFound,
            String profileAlreadyActive,
            String combatBlock,
            String warmupStart,
            String switchSuccess,
            String noPermission,
            String createFail,
            String createSuccess,
            String deleteFail,
            String deleteSuccess,
            String help,
            String listHeader,
            String listItemActive,
            String listItemInactive,
            String currentProfile,
            String playerOnly,
            String noProfiles,
            String noActiveProfile,
            String reloadSuccess,
            String switchCooldown,
            String maxProfilesReached,
            String cannotDeleteActive,
            String cannotDeleteDefault,
            String invalidProfileName,
            String profileCreationCancelled,
            String profileCreationTimedOut,
            String playerNotFound,
            String adminOpenSuccess,
            String adminListHeader,
            String adminCreateSuccess,
            String adminCreateFail,
            String adminSwitchSuccess,
            String adminSwitchFailNoProfile,
            String adminDeleteSuccess,
            String adminDeleteFail,
            String adminAlertSwitch,
            String adminAlertCreate,
            String adminAlertDelete,
            String alertsEnabled,
            String alertsDisabled,
            String warmupCancelledMove,
            String warmupCancelledDamage,
            String warmupCancelledGeneric,
            String debugEnabled,
            String debugDisabled
    ) implements ConfigSection {
    }

    public record GuiSection(
            @Comment("Title of the profile inventory GUI")
            String title,
            @Comment("Number of rows in the profile GUI")
            int rows,
            @Comment("Inventory pattern structure")
            List<String> pattern,
            @Comment("Material for the creation button")
            String createButtonMaterial,
            @Comment("Material for creation button when limits are reached")
            String createButtonMaterialLimitReached,
            @Comment("Name of the creation button")
            String createButtonName,
            @Comment("Name of creation button when limits are reached")
            String createButtonNameLimitReached,
            @Comment("Lore of the creation button")
            List<String> createButtonLore,
            @Comment("Lore of creation button when limits are reached")
            List<String> createButtonLoreLimitReached,
            @Comment("Material for the currently active profile item")
            String activeProfileMaterial,
            @Comment("Material for inactive profile items")
            String inactiveProfileMaterial,
            @Comment("Material for empty slot profile items")
            String emptySlotMaterial,
            @Comment("DisplayName pattern for the active profile")
            String activeProfileName,
            @Comment("DisplayName pattern for inactive profiles")
            String inactiveProfileName,
            @Comment("DisplayName pattern for empty slots")
            String emptySlotName,
            @Comment("Lore layout for the active profile item")
            List<String> activeProfileLore,
            @Comment("Lore layout for inactive profile items")
            List<String> inactiveProfileLore,
            @Comment("Material used for border glass items")
            String borderMaterial,
            @Comment("DisplayName of border glass items")
            String borderName,
            @Comment("Prompt printed in chat when player starts creation input")
            String promptMessage,
            @Comment("Keyword player types to abort creation")
            String cancelWord,
            @Comment("How long in seconds until chat input times out")
            int textInputTimeoutSeconds,
            @Comment("Layout character representing profile items")
            String profileSlotChar,
            @Comment("Layout character representing the creation button")
            String createButtonSlotChar,

            @Comment("Play sound when GUI is opened")
            boolean openSoundEnabled,
            @Comment("GUI open sound key")
            String openSoundKey,
            @Comment("Play sound when clicking GUI items")
            boolean clickSoundEnabled,
            @Comment("GUI click sound key")
            String clickSoundKey,
            @Comment("Play sound on errors/limits in GUI")
            boolean errorSoundEnabled,
            @Comment("GUI error sound key")
            String errorSoundKey,
            @Comment("Play sound when GUI is closed")
            boolean closeSoundEnabled,
            @Comment("GUI close sound key")
            String closeSoundKey
    ) implements ConfigSection {
    }

    public record ConfirmGuiSection(
            @Comment("Enable confirmation GUI")
            boolean enabled,
            @Comment("Title of the confirmation GUI")
            String title,
            @Comment("Number of rows in the GUI")
            int rows,
            @Comment("Inventory pattern structure")
            List<String> pattern,
            @Comment("Confirm button character in pattern")
            String confirmSlotChar,
            @Comment("Confirm button material")
            String confirmMaterial,
            @Comment("Confirm button display name")
            String confirmName,
            @Comment("Confirm button lore")
            List<String> confirmLore,
            @Comment("Deny button character in pattern")
            String denySlotChar,
            @Comment("Deny button material")
            String denyMaterial,
            @Comment("Deny button display name")
            String denyName,
            @Comment("Deny button lore")
            List<String> denyLore,
            @Comment("Material used for border glass items")
            String borderMaterial,
            @Comment("DisplayName of border glass items")
            String borderName
    ) implements ConfigSection {
    }
}
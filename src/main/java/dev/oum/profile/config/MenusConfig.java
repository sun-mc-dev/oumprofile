package dev.oum.profile.config;

import dev.oum.oumlib.config.Comment;
import dev.oum.oumlib.config.ConfigSection;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Comment({
        "OumProfile Menus Configuration",
        "Customize inventory GUI titles, dimensions, patterns, buttons, and confirmation dialogs."
})
public record MenusConfig(
        @Comment("Main profile selection GUI settings")
        GuiSection gui,

        @Comment("Confirmation GUI settings for deleting profiles")
        ConfirmGuiSection confirmDelete,

        @Comment("Confirmation GUI settings for creating profiles")
        ConfirmGuiSection confirmCreate
) implements ConfigSection {

    @Contract(" -> new")
    public static @NonNull MenusConfig defaults() {
        return new MenusConfig(
                new GuiSection(
                        "<color:#5c5f77>Select a Profile</color>",
                        3,
                        List.of(
                                "#########",
                                "  PPPPP  ",
                                "####C####"
                        ),
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19",
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE5OWI1ZWUzMjBlNzk5N2Q5MWJiNWY4NjY1ZjNkMzJhZTQ5MjBlMDNjNmIzZDliN2VlY2E2OTcxMTk5OTcifX19",
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
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdmYWFlMWQxOTgzNmJkMDc4NTQyNmU0ZmQyOGFhNjNhMzgxZTllNzE0OTU1OWVlNmIyYTUwOTk5NWJiY2ZkMiJ9fX0=",
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVjNmRjMmJiZjUxYzM2Y2ZjNzcxNDU4NWE2YTU2ODNlZjJiMTRkNDdkOGZmNzE0NjU0YTg5M2Y1ZGE2MjIifX19",
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZiYTYzMzQ0ZjQ5ZGQxYzRmNTQ4OGU5MjZiZjNkOWUyYjI5OTE2YTZjNTBkNjEwYmI0MGE1MjczZGM4YzgyIn19fQ==",
                        "<color:#a6e3a1><b><name></b></color> <color:#9399b2>(Active)</color>",
                        "<color:#cba6f7><b><name></b></color>",
                        "<color:#585b70>Empty Slot</color>",
                        List.of(
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#9399b2>Created: <color:#cdd6f4><created></color></color>",
                                "<color:#9399b2>Last Used: <color:#cdd6f4><last_used></color></color>",
                                "<color:#9399b2>Playtime: <color:#f9e2af><playtime></color></color>",
                                "<color:#9399b2>Balance: <color:#f9e2af>$<balance></color></color>",
                                "<color:#9399b2>Rank Group: <color:#b4befe><group></color></color>",
                                "<color:#9399b2>Jobs: <color:#f9e2af><jobs></color></color>",
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#a6e3a1>Currently Active</color>"
                        ),
                        List.of(
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#9399b2>Created: <color:#cdd6f4><created></color></color>",
                                "<color:#9399b2>Last Used: <color:#cdd6f4><last_used></color></color>",
                                "<color:#9399b2>Playtime: <color:#f9e2af><playtime></color></color>",
                                "<color:#9399b2>Balance: <color:#f9e2af>$<balance></color></color>",
                                "<color:#9399b2>Rank Group: <color:#b4befe><group></color></color>",
                                "<color:#9399b2>Jobs: <color:#f9e2af><jobs></color></color>",
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#74c7ec>Left-Click to switch</color>",
                                "<color:#f38ba8>Right-Click to delete</color>"
                        ),
                        List.of(
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>",
                                "<color:#9399b2>Click on the Create button</color>",
                                "<color:#9399b2>below to start a new profile.</color>",
                                "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>"
                        ),
                        "GRAY_STAINED_GLASS_PANE",
                        " ",
                        "P",
                        "C",
                        "<color:#74c7ec>Type a profile name in chat:</color>",
                        15,
                        "cancel",
                        true,
                        "block.chest.open",
                        true,
                        "ui.button.click",
                        true,
                        "block.chest.close",
                        true,
                        "entity.villager.no"
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
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==",
                        "<color:#f38ba8><b>Confirm Deletion</b></color>",
                        List.of(
                                "<color:#a6adc8>Clicking here will permanently</color>",
                                "<color:#a6adc8>delete the profile <color:#fab387><profile></color>.</color>",
                                "",
                                "<color:#f38ba8><b>WARNING: This cannot be undone!</b></color>"
                        ),
                        "D",
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0=",
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
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0=",
                        "<color:#a6e3a1><b>Confirm Creation</b></color>",
                        List.of(
                                "<color:#a6adc8>Click here to create</color>",
                                "<color:#a6adc8>profile <color:#cba6f7><name></color>.</color>"
                        ),
                        "D",
                        "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==",
                        "<color:#f38ba8><b>Cancel</b></color>",
                        List.of(
                                "<color:#a6adc8>Click to cancel creation</color>",
                                "<color:#a6adc8>and return to the menu.</color>"
                        ),
                        "GRAY_STAINED_GLASS_PANE",
                        " "
                )
        );
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
            @Comment("Display name for the create profile button")
            String createButtonName,
            @Comment("Display name for create button when limits are reached")
            String createButtonNameLimitReached,
            @Comment("Lore lines for the create profile button")
            List<String> createButtonLore,
            @Comment("Lore lines for create button when limits are reached")
            List<String> createButtonLoreLimitReached,
            @Comment("Material for the active profile button")
            String activeProfileMaterial,
            @Comment("Material for inactive profile buttons")
            String inactiveProfileMaterial,
            @Comment("Material for empty profile slots")
            String emptySlotMaterial,
            @Comment("Display name for the active profile button")
            String activeProfileName,
            @Comment("Display name for inactive profile buttons")
            String inactiveProfileName,
            @Comment("Display name for empty slots")
            String emptySlotName,
            @Comment("Lore lines for the active profile button")
            List<String> activeProfileLore,
            @Comment("Lore lines for inactive profile buttons")
            List<String> inactiveProfileLore,
            @Comment("Lore lines for empty profile slots")
            List<String> emptySlotLore,
            @Comment("Material for the GUI border filler items")
            String borderMaterial,
            @Comment("Display name for the GUI border items")
            String borderName,
            @Comment("Character key representing profile slots in the pattern")
            String profileSlotChar,
            @Comment("Character key representing create button in the pattern")
            String createButtonSlotChar,
            @Comment("Prompt message sent in chat when naming a new profile")
            String promptMessage,
            @Comment("Time limit in seconds to enter a profile name in chat")
            int textInputTimeoutSeconds,
            @Comment("Word players can type in chat to cancel profile creation")
            String cancelWord,

            @Comment("Play sound when opening profile GUI")
            boolean openSoundEnabled,
            @Comment("Sound key when opening GUI")
            String openSoundKey,
            @Comment("Play sound on profile selection click")
            boolean clickSoundEnabled,
            @Comment("Sound key for selection click")
            String clickSoundKey,
            @Comment("Play sound when closing GUI")
            boolean closeSoundEnabled,
            @Comment("Sound key when closing GUI")
            String closeSoundKey,
            @Comment("Play sound on invalid actions or errors in GUI")
            boolean errorSoundEnabled,
            @Comment("Sound key for GUI error feedback")
            String errorSoundKey
    ) implements ConfigSection {
    }

    public record ConfirmGuiSection(
            @Comment("Enable confirmation menu")
            boolean enabled,
            @Comment("Title of the confirmation menu")
            String title,
            @Comment("Number of rows in the menu")
            int rows,
            @Comment("Pattern defining layout slots")
            List<String> pattern,
            @Comment("Slot character for the Confirm button")
            String confirmSlotChar,
            @Comment("Item material/head for Confirm button")
            String confirmMaterial,
            @Comment("Display name for Confirm button")
            String confirmName,
            @Comment("Lore lines for Confirm button")
            List<String> confirmLore,
            @Comment("Slot character for the Deny/Cancel button")
            String denySlotChar,
            @Comment("Item material/head for Deny/Cancel button")
            String denyMaterial,
            @Comment("Display name for Deny/Cancel button")
            String denyName,
            @Comment("Lore lines for Deny/Cancel button")
            List<String> denyLore,
            @Comment("Material for menu border filler items")
            String borderMaterial,
            @Comment("Display name for border items")
            String borderName
    ) implements ConfigSection {
    }
}

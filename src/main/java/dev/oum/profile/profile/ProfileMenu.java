package dev.oum.profile.profile;

import dev.oum.oumlib.inventory.ChestMenu;
import dev.oum.oumlib.inventory.ClickAction;
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.inventory.Layout;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.text.TextInput;
import dev.oum.profile.command.Permissions;
import dev.oum.profile.config.ProfileConfig;
import dev.oum.profile.model.ProfileData;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("PatternValidation")
public final class ProfileMenu {

    private final ProfileManager manager;

    public ProfileMenu(@NonNull ProfileManager manager) {
        this.manager = manager;
    }

    private @NonNull DateTimeFormatter dateFormatter() {
        try {
            return DateTimeFormatter.ofPattern(manager.configManager().get().dateFormat())
                    .withZone(ZoneId.systemDefault());
        } catch (IllegalArgumentException e) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());
        }
    }

    public void open(@NonNull Player player) {
        ProfileConfig mainConfig = manager.configManager().get();
        ProfileConfig.GuiSection cfg = mainConfig.gui();
        Map<String, ProfileData> profiles = manager.getProfiles(player.getUniqueId());
        String activeProfile = manager.getActiveProfileName(player.getUniqueId());

        char pChar = cfg.profileSlotChar().isEmpty() ? 'P' : cfg.profileSlotChar().charAt(0);
        char cChar = cfg.createButtonSlotChar().isEmpty() ? 'C' : cfg.createButtonSlotChar().charAt(0);

        List<String> patternList = cfg.pattern();
        Layout tempLayout = new Layout(patternList.toArray(new String[0]));
        List<Integer> profileSlots = tempLayout.slotsFor(pChar);

        ChestMenu.Builder builder = ChestMenu.builder()
                .title(cfg.title())
                .rows(cfg.rows())
                .pattern(patternList.toArray(new String[0]));

        if (cfg.openSoundEnabled() && cfg.openSoundKey() != null && !cfg.openSoundKey().isEmpty()) {
            builder = builder.openSound(Sound.sound(Key.key(cfg.openSoundKey()), Sound.Source.MASTER, 1.0f, 1.0f));
        }
        if (cfg.clickSoundEnabled() && cfg.clickSoundKey() != null && !cfg.clickSoundKey().isEmpty()) {
            builder = builder.clickSound(Sound.sound(Key.key(cfg.clickSoundKey()), Sound.Source.MASTER, 1.0f, 1.0f));
        }
        if (cfg.closeSoundEnabled() && cfg.closeSoundKey() != null && !cfg.closeSoundKey().isEmpty()) {
            builder = builder.closeSound(Sound.sound(Key.key(cfg.closeSoundKey()), Sound.Source.MASTER, 1.0f, 1.0f));
        }

        // Bind border characters dynamically
        Material borderMat = Material.matchMaterial(cfg.borderMaterial());
        if (borderMat == null) borderMat = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack borderItem = ItemBuilder.of(borderMat).name(cfg.borderName()).build();

        for (String row : patternList) {
            for (char ch : row.toCharArray()) {
                if (ch != pChar && ch != cChar && ch != ' ') {
                    builder = builder.bind(ch, borderItem);
                }
            }
        }

        // Bind Create Profile button ('C')
        builder = builder.bind(cChar, () -> {
            int current = profiles.size();
            int max = manager.getMaxProfiles(player);
            boolean canCreate = current < max;

            String matStr = canCreate ? cfg.createButtonMaterial() : cfg.createButtonMaterialLimitReached();
            Material mat = Material.matchMaterial(matStr);
            if (mat == null) mat = canCreate ? Material.EMERALD : Material.BARRIER;

            String displayName = canCreate ? cfg.createButtonName() : cfg.createButtonNameLimitReached();

            List<String> rawLore = canCreate ? cfg.createButtonLore() : cfg.createButtonLoreLimitReached();
            List<String> formattedLore = new ArrayList<>();
            for (String line : rawLore) {
                formattedLore.add(line
                        .replace("<slots_current>", String.valueOf(current))
                        .replace("<slots_max>", max == Integer.MAX_VALUE ? "Unlimited" : String.valueOf(max))
                );
            }

            return ItemBuilder.of(mat)
                    .name(displayName)
                    .lore(formattedLore.toArray(new String[0]))
                    .build();
        }).onClick(cChar, ctx -> {
            int max = manager.getMaxProfiles(player);
            if (profiles.size() >= max) {
                Text.send(player, mainConfig.messages().maxProfilesReached());
                playErrorSound(player, cfg);
                return;
            }
            ctx.player().closeInventory();
            openCreationInput(player);
        });

        // Bind Profile Slots dynamically
        List<ProfileData> profileList = new ArrayList<>(profiles.values());
        for (int i = 0; i < profileSlots.size(); i++) {
            final int slot = profileSlots.get(i);
            final int index = i;
            builder = builder.item(slot, () -> {
                if (index >= profileList.size()) {
                    Material emptyMat = Material.matchMaterial(cfg.emptySlotMaterial());
                    if (emptyMat == null) emptyMat = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
                    return ItemBuilder.of(emptyMat)
                            .name(cfg.emptySlotName())
                            .build();
                }
                ProfileData data = profileList.get(index);
                boolean isActive = data.name().equals(activeProfile);

                Material profileMat = Material.matchMaterial(
                        isActive ? cfg.activeProfileMaterial() : cfg.inactiveProfileMaterial()
                );
                if (profileMat == null) {
                    profileMat = isActive ? Material.BOOK : Material.WRITTEN_BOOK;
                }

                String displayName = isActive ? cfg.activeProfileName() : cfg.inactiveProfileName();
                displayName = displayName.replace("<name>", data.name());

                List<String> rawLore = isActive ? cfg.activeProfileLore() : cfg.inactiveProfileLore();
                List<String> formattedLore = new ArrayList<>();
                for (String line : rawLore) {
                    formattedLore.add(line
                            .replace("<created>", dateFormatter().format(Instant.ofEpochMilli(data.createdAt())))
                            .replace("<last_used>", dateFormatter().format(Instant.ofEpochMilli(data.lastUsed())))
                            .replace("<balance>", String.format(Locale.ROOT, "%.2f", data.balance()))
                            .replace("<group>", data.primaryGroup() != null ? data.primaryGroup() : "default")
                    );
                }

                var item = ItemBuilder.of(profileMat)
                        .name(displayName)
                        .lore(formattedLore.toArray(new String[0]));

                if (isActive) item.glow();
                return item.build();
            }).onClick(slot, ctx -> {
                if (index >= profileList.size()) return;
                ProfileData data = profileList.get(index);
                boolean isActive = data.name().equals(activeProfile);

                boolean isRightClick = switch (ctx.action()) {
                    case ClickAction.RightClick ignored -> true;
                    case ClickAction.ShiftRightClick ignored -> true;
                    default -> false;
                };
                if (isRightClick) {
                    if (isActive) {
                        Text.send(player, mainConfig.messages().cannotDeleteActive());
                        playErrorSound(player, cfg);
                        return;
                    }
                    if (data.name().equalsIgnoreCase(manager.configManager().get().defaultProfileName())) {
                        Text.send(player, mainConfig.messages().cannotDeleteDefault());
                        playErrorSound(player, cfg);
                        return;
                    }
                    if (mainConfig.confirmDelete().enabled()) {
                        new ConfirmMenu(mainConfig.confirmDelete(), data.name(), () -> {
                            if (manager.deleteProfile(player, data.name())) {
                                Text.send(player, mainConfig.messages().deleteSuccess(), "name", data.name());
                                open(player);
                            } else {
                                Text.send(player, mainConfig.messages().deleteFail(), "name", data.name());
                                playErrorSound(player, cfg);
                            }
                        }, () -> open(player)).open(player);
                    } else {
                        if (manager.deleteProfile(player, data.name())) {
                            Text.send(player, mainConfig.messages().deleteSuccess(), "name", data.name());
                            open(player);
                        } else {
                            Text.send(player, mainConfig.messages().deleteFail(), "name", data.name());
                            playErrorSound(player, cfg);
                        }
                    }
                } else {
                    if (isActive) {
                        playErrorSound(player, cfg);
                        return;
                    }
                    ctx.player().closeInventory();
                    manager.requestSwitch(player, data.name());
                }
            });
        }

        builder.build().open(player);
    }

    private void playErrorSound(@NonNull Player player, ProfileConfig.@NonNull GuiSection cfg) {
        if (cfg.errorSoundEnabled() && cfg.errorSoundKey() != null && !cfg.errorSoundKey().isEmpty()) {
            player.playSound(Sound.sound(Key.key(cfg.errorSoundKey()), Sound.Source.MASTER, 1.0f, 1.0f));
        }
    }

    private void openCreationInput(@NonNull Player player) {
        ProfileConfig mainConfig = manager.configManager().get();
        ProfileConfig.GuiSection cfg = mainConfig.gui();

        TextInput.builder()
                .prompt(Text.parse(cfg.promptMessage()))
                .timeout(Duration.ofSeconds(cfg.textInputTimeoutSeconds()))
                .cancelWord(cfg.cancelWord())
                .onInput((p, text) -> {
                    String clean = text.trim();
                    if (clean.isEmpty() || clean.contains(" ")) {
                        Text.send(p, mainConfig.messages().invalidProfileName());
                        return false;
                    }
                    if (!p.hasPermission(Permissions.CREATE_PREFIX + clean) && !p.hasPermission(Permissions.CREATE_ALL)) {
                        Text.send(p, mainConfig.messages().noPermission(), "name", clean);
                        return false;
                    }
                    if (mainConfig.confirmCreate().enabled()) {
                        new ConfirmMenu(mainConfig.confirmCreate(), clean, () -> {
                            if (manager.createProfile(p, clean)) {
                                Text.send(p, mainConfig.messages().createSuccess(), "name", clean);
                                open(p);
                            } else {
                                Text.send(p, mainConfig.messages().createFail(), "name", clean);
                            }
                        }, () -> {
                            Text.send(p, mainConfig.messages().profileCreationCancelled());
                            open(p);
                        }).open(p);
                        return true;
                    }
                    if (manager.createProfile(p, clean)) {
                        Text.send(p, mainConfig.messages().createSuccess(), "name", clean);
                        open(p);
                        return true;
                    } else {
                        Text.send(p, mainConfig.messages().createFail(), "name", clean);
                        return false;
                    }
                })
                .onCancel(p -> {
                    Text.send(p, mainConfig.messages().profileCreationCancelled());
                    open(p);
                })
                .onTimeout(p -> {
                    Text.send(p, mainConfig.messages().profileCreationTimedOut());
                    open(p);
                })
                .start(player);
    }
}

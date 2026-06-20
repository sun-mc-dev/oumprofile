package dev.oum.profile.profile;

import dev.oum.oumlib.inventory.ChestMenu;
import dev.oum.oumlib.inventory.ClickAction;
import dev.oum.oumlib.bridge.item.ItemBridge;
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.bridge.economy.EconomyBridge;
import dev.oum.oumlib.bridge.permission.PermissionBridge;
import dev.oum.oumlib.inventory.Layout;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.text.TextInput;
import dev.oum.oumlib.util.Format;
import dev.oum.profile.command.Permissions;
import dev.oum.profile.config.ProfileConfig;
import dev.oum.profile.integration.IntegrationManager;
import dev.oum.profile.integration.SkillData;
import dev.oum.profile.model.ProfileData;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
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
        ItemStack borderItem = resolveItem(cfg.borderMaterial(), Material.GRAY_STAINED_GLASS_PANE)
                .name(cfg.borderName())
                .build();

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
            Material fallback = canCreate ? Material.EMERALD : Material.BARRIER;

            String displayName = canCreate ? cfg.createButtonName() : cfg.createButtonNameLimitReached();

            List<String> rawLore = canCreate ? cfg.createButtonLore() : cfg.createButtonLoreLimitReached();
            List<String> formattedLore = new ArrayList<>();
            for (String line : rawLore) {
                formattedLore.add(line
                        .replace("<slots_current>", String.valueOf(current))
                        .replace("<slots_max>", max == Integer.MAX_VALUE ? "Unlimited" : String.valueOf(max))
                );
            }

            return resolveItem(matStr, fallback)
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
        int activeSlotIdx = -1;
        ProfileData activeProfileData = null;
        for (int i = 0; i < profileSlots.size(); i++) {
            int index = i;
            if (index < profileList.size()) {
                ProfileData data = profileList.get(index);
                if (data.name().equals(activeProfile)) {
                    activeSlotIdx = profileSlots.get(i);
                    activeProfileData = data;
                    break;
                }
            }
        }
        final int finalActiveSlot = activeSlotIdx;
        final ProfileData finalActiveData = activeProfileData;

        for (int i = 0; i < profileSlots.size(); i++) {
            final int slot = profileSlots.get(i);
            final int index = i;
            builder = builder.item(slot, () -> {
                if (index >= profileList.size()) {
                    return resolveItem(cfg.emptySlotMaterial(), Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                            .name(cfg.emptySlotName())
                            .build();
                }
                ProfileData data = profileList.get(index);
                boolean isActive = data.name().equals(activeProfile);

                if (isActive) {
                    return buildActiveProfileItem(player, data, cfg);
                }

                String matStr = cfg.inactiveProfileMaterial();
                Material fallback = Material.WRITTEN_BOOK;

                String displayName = cfg.inactiveProfileName().replace("<name>", data.name());

                List<String> rawLore = cfg.inactiveProfileLore();
                List<String> formattedLore = new ArrayList<>();
                for (String line : rawLore) {
                    formattedLore.add(line
                            .replace("<created>", dateFormatter().format(Instant.ofEpochMilli(data.createdAt())))
                            .replace("<last_used>", dateFormatter().format(Instant.ofEpochMilli(data.lastUsed())))
                            .replace("<balance>", String.format(Locale.ROOT, "%.2f", data.balance()))
                            .replace("<group>", data.primaryGroup() != null ? data.primaryGroup() : "default")
                            .replace("<playtime>", data.state().playtimeSeconds() != null
                                    ? Format.duration(Duration.ofSeconds(data.state().playtimeSeconds())) : "0s")
                            .replace("<jobs>", formatSkills(data.state().jobs()))
                            .replace("<mcmmo>", formatSkills(data.state().mcmmo()))
                            .replace("<auraskills>", formatSkills(data.state().auraskills()))
                    );
                }

                var item = resolveItem(matStr, fallback)
                        .name(displayName)
                        .lore(formattedLore.toArray(new String[0]));

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

        final AtomicReference<TaskHandle> taskRef = new AtomicReference<>();
        final AtomicReference<ChestMenu> menuRef = new AtomicReference<>();

        builder = builder.onClose(p -> {
            TaskHandle task = taskRef.get();
            if (task != null) {
                task.cancel();
            }
        });

        ChestMenu menu = builder.build();
        menuRef.set(menu);
        menu.open(player);

        TaskHandle task = Scheduler.runRepeating(
                Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                () -> {
                    if (player.isOnline() && finalActiveSlot != -1 && finalActiveData != null) {
                        ChestMenu m = menuRef.get();
                        if (m != null) {
                            ItemStack updatedItem = buildActiveProfileItem(player, finalActiveData, cfg);
                            m.setItem(player, finalActiveSlot, updatedItem);
                        }
                    }
                }
        );
        taskRef.set(task);
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

    private @NonNull ItemStack buildActiveProfileItem(@NonNull Player player, @NonNull ProfileData data, ProfileConfig.@NonNull GuiSection cfg) {
        String matStr = cfg.activeProfileMaterial();
        Material fallback = Material.BOOK;

        String displayName = cfg.activeProfileName().replace("<name>", data.name());

        double balanceVal = EconomyBridge.balance(player);
        String groupVal = PermissionBridge.getPrimaryGroup(player.getUniqueId());
        if (groupVal == null) groupVal = "default";

        long elapsed = manager.getElapsedSessionSeconds(player.getUniqueId());
        long base = data.state().playtimeSeconds() != null ? data.state().playtimeSeconds() : 0L;
        long playtimeSecs = base + elapsed;

        Map<String, SkillData> jobs = IntegrationManager.jobs().capture(player);
        Map<String, SkillData> mcmmo = IntegrationManager.mcmmo().capture(player);
        Map<String, SkillData> auraskills = IntegrationManager.auraSkills().capture(player);

        List<String> rawLore = cfg.activeProfileLore();
        List<String> formattedLore = new ArrayList<>();
        for (String line : rawLore) {
            formattedLore.add(line
                    .replace("<created>", dateFormatter().format(Instant.ofEpochMilli(data.createdAt())))
                    .replace("<last_used>", dateFormatter().format(Instant.ofEpochMilli(data.lastUsed())))
                    .replace("<balance>", String.format(Locale.ROOT, "%.2f", balanceVal))
                    .replace("<group>", groupVal)
                    .replace("<playtime>", Format.duration(Duration.ofSeconds(playtimeSecs)))
                    .replace("<jobs>", formatSkills(jobs))
                    .replace("<mcmmo>", formatSkills(mcmmo))
                    .replace("<auraskills>", formatSkills(auraskills))
            );
        }

        var item = resolveItem(matStr, fallback)
                .name(displayName)
                .lore(formattedLore.toArray(new String[0]))
                .glow();

        return item.build();
    }

    private @NonNull String formatSkills(@Nullable Map<String, SkillData> map) {
        if (map == null || map.isEmpty()) {
            return "None";
        }
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, SkillData> entry : map.entrySet()) {
            list.add(entry.getKey() + " (Lv. " + entry.getValue().level() + ")");
        }
        return String.join(", ", list);
    }

    private static @NonNull ItemBuilder resolveItem(@NonNull String input, @NonNull Material fallback) {
        if (input.startsWith("head:") || input.startsWith("skull:")) {
            String texture = input.substring(input.indexOf(':') + 1);
            return ItemBuilder.of(Material.PLAYER_HEAD).skull(texture);
        }
        return ItemBuilder.of(ItemBridge.getItem(input).orElseGet(() -> new ItemStack(fallback)));
    }
}

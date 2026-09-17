package dev.oum.profile.profile;

import dev.oum.oumlib.bridge.economy.EconomyBridge;
import dev.oum.oumlib.bridge.permission.PermissionBridge;
import dev.oum.oumlib.inventory.*;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import dev.oum.oumlib.text.Format;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.text.TextInput;
import dev.oum.profile.command.Permissions;
import dev.oum.profile.config.MenusConfig;
import dev.oum.profile.config.MenusConfig.ConfirmGuiSection;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("PatternValidation")
public final class ProfileMenu {

    private final ProfileManager manager;

    public ProfileMenu(@NonNull ProfileManager manager) {
        this.manager = manager;
    }

    public void open(@NonNull Player player) {
        ProfileConfig config = manager.config();
        MenusConfig.GuiSection cfg = config.menus().gui();
        Map<String, ProfileData> profiles = manager.getProfiles(player.getUniqueId());
        String activeProfile = manager.getActiveProfileName(player.getUniqueId());

        char pChar = cfg.profileSlotChar().isEmpty() ? 'P' : cfg.profileSlotChar().charAt(0);
        char cChar = cfg.createButtonSlotChar().isEmpty() ? 'C' : cfg.createButtonSlotChar().charAt(0);

        List<String> patternList = cfg.pattern();
        String[] pattern = patternList.toArray(new String[0]);
        Layout tempLayout = new Layout(pattern);
        List<Integer> profileSlots = tempLayout.slotsFor(pChar);

        ChestMenu.Builder builder = ChestMenu.builder()
                .title(cfg.title())
                .rows(cfg.rows())
                .pattern(pattern);

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
        ItemStack borderItem = ItemBuilder.from(cfg.borderMaterial(), Material.GRAY_STAINED_GLASS_PANE)
                .name(cfg.borderName())
                .build();
        builder = builder.bindBorders(borderItem, pChar, cChar);

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

            return ItemBuilder.from(matStr, fallback)
                    .name(displayName)
                    .lore(formattedLore.toArray(new String[0]))
                    .build();
        }).onClick(cChar, ctx -> {
            int max = manager.getMaxProfiles(player);
            if (profiles.size() >= max) {
                Text.send(player, config.messages().maxProfilesReached(), "max", String.valueOf(max));
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
                    return ItemBuilder.from(cfg.emptySlotMaterial(), Material.LIGHT_GRAY_STAINED_GLASS_PANE)
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

                List<String> formattedLore = formatProfileLore(
                        cfg.inactiveProfileLore(),
                        data,
                        data.balance(),
                        data.primaryGroup() != null ? data.primaryGroup() : "default",
                        data.state().playtimeSeconds() != null ? data.state().playtimeSeconds() : 0L,
                        formatSkills(data.state().jobs()),
                        formatSkills(data.state().mcmmo()),
                        formatSkills(data.state().auraskills())
                );

                return ItemBuilder.from(matStr, fallback)
                        .name(displayName)
                        .lore(formattedLore.toArray(new String[0]))
                        .build();
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
                        Text.send(player, config.messages().cannotDeleteActive());
                        playErrorSound(player, cfg);
                        return;
                    }
                    if (data.name().equalsIgnoreCase(config.main().defaultProfileName())) {
                        Text.send(player, config.messages().cannotDeleteDefault());
                        playErrorSound(player, cfg);
                        return;
                    }
                    Runnable performDelete = () -> {
                        if (manager.deleteProfile(player, data.name())) {
                            Text.send(player, config.messages().deleteSuccess(), "name", data.name());
                            open(player);
                        } else {
                            Text.send(player, config.messages().deleteFail(), "name", data.name());
                            playErrorSound(player, cfg);
                        }
                    };
                    if (config.menus().confirmDelete().enabled()) {
                        openConfirmDialog(player, config.menus().confirmDelete(), data.name(), performDelete, () -> open(player));
                    } else {
                        performDelete.run();
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

    private void playErrorSound(@NonNull Player player, MenusConfig.@NonNull GuiSection cfg) {
        if (cfg.errorSoundEnabled() && cfg.errorSoundKey() != null && !cfg.errorSoundKey().isEmpty()) {
            player.playSound(Sound.sound(Key.key(cfg.errorSoundKey()), Sound.Source.MASTER, 1.0f, 1.0f));
        }
    }

    private void openCreationInput(@NonNull Player player) {
        ProfileConfig config = manager.config();
        MenusConfig.GuiSection cfg = config.menus().gui();

        TextInput.builder()
                .prompt(Text.parse(cfg.promptMessage()))
                .timeout(Duration.ofSeconds(cfg.textInputTimeoutSeconds()))
                .cancelWord(cfg.cancelWord())
                .onInput((p, text) -> {
                    String clean = text.trim();
                    if (!manager.checkNameValidation(p, clean)) {
                        return false;
                    }
                    if (!p.hasPermission(Permissions.CREATE_PREFIX + clean) && !p.hasPermission(Permissions.CREATE_ALL)) {
                        Text.send(p, config.messages().noPermission(), "name", clean);
                        return false;
                    }
                    Runnable performCreate = () -> {
                        if (manager.createProfile(p, clean)) {
                            Text.send(p, config.messages().createSuccess(), "name", clean);
                            open(p);
                        } else {
                            Text.send(p, config.messages().createFail(), "name", clean);
                        }
                    };
                    if (config.menus().confirmCreate().enabled()) {
                        openConfirmDialog(p, config.menus().confirmCreate(), clean, performCreate, () -> {
                            Text.send(p, config.messages().profileCreationCancelled());
                            open(p);
                        });
                    } else {
                        performCreate.run();
                    }
                    return true;
                })
                .onCancel(p -> {
                    Text.send(p, config.messages().profileCreationCancelled());
                    open(p);
                })
                .onTimeout(p -> {
                    Text.send(p, config.messages().profileCreationTimedOut());
                    open(p);
                })
                .start(player);
    }

    private @NonNull ItemStack buildActiveProfileItem(@NonNull Player player,
                                                      @NonNull ProfileData data, MenusConfig.@NonNull GuiSection cfg) {
        String matStr = cfg.activeProfileMaterial();
        Material fallback = Material.BOOK;

        String displayName = cfg.activeProfileName().replace("<name>", data.name());

        double balanceVal = EconomyBridge.balance(player);
        String groupVal = PermissionBridge.getPrimaryGroup(player.getUniqueId());
        if (groupVal == null) groupVal = "default";

        long elapsed = manager.getElapsedSessionSeconds(player.getUniqueId());
        long base = data.state().playtimeSeconds() != null ? data.state().playtimeSeconds() : 0L;
        long playtimeSecs = base + elapsed;

        List<String> formattedLore = formatProfileLore(
                cfg.activeProfileLore(),
                data,
                balanceVal,
                groupVal,
                playtimeSecs,
                formatSkills(IntegrationManager.jobs().capture(player)),
                formatSkills(IntegrationManager.mcmmo().capture(player)),
                formatSkills(IntegrationManager.auraSkills().capture(player))
        );

        return ItemBuilder.from(matStr, fallback)
                .name(displayName)
                .lore(formattedLore.toArray(new String[0]))
                .glow()
                .build();
    }

    private @NonNull List<String> formatProfileLore(
            @NonNull List<String> rawLore,
            @NonNull ProfileData data,
            double balance,
            @NonNull String group,
            long playtimeSeconds,
            @NonNull String jobs,
            @NonNull String mcmmo,
            @NonNull String auraskills
    ) {
        List<String> formatted = new ArrayList<>(rawLore.size());
        String created = manager.dateFormatter().format(Instant.ofEpochMilli(data.createdAt()));
        String lastUsed = manager.dateFormatter().format(Instant.ofEpochMilli(data.lastUsed()));
        String balanceStr = String.format(Locale.ROOT, "%.2f", balance);
        String playtimeStr = Format.duration(Duration.ofSeconds(playtimeSeconds));

        for (String line : rawLore) {
            formatted.add(line
                    .replace("<name>", data.name())
                    .replace("<created>", created)
                    .replace("<last_used>", lastUsed)
                    .replace("<balance>", balanceStr)
                    .replace("<group>", group)
                    .replace("<playtime>", playtimeStr)
                    .replace("<jobs>", jobs)
                    .replace("<mcmmo>", mcmmo)
                    .replace("<auraskills>", auraskills)
            );
        }
        return formatted;
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

    private void openConfirmDialog(@NonNull Player player, @NonNull ConfirmGuiSection section, @NonNull String targetName,
                                   @NonNull Runnable onConfirm, @NonNull Runnable onDeny) {
        String resolvedTitle = section.title()
                .replace("<profile>", targetName)
                .replace("<name>", targetName);

        char confirmChar = section.confirmSlotChar().isEmpty() ? 'C' : section.confirmSlotChar().charAt(0);
        char denyChar = section.denySlotChar().isEmpty() ? 'D' : section.denySlotChar().charAt(0);

        ItemStack borderItem = ItemBuilder.from(section.borderMaterial(), Material.GRAY_STAINED_GLASS_PANE)
                .name(section.borderName())
                .build();

        ConfirmMenu.builder()
                .title(resolvedTitle)
                .rows(section.rows())
                .pattern(section.pattern())
                .confirmSlot(confirmChar)
                .denySlot(denyChar)
                .border(borderItem)
                .confirmItem(() -> ItemBuilder.from(section.confirmMaterial(), Material.GREEN_WOOL)
                        .name(section.confirmName().replace("<profile>", targetName).replace("<name>", targetName))
                        .lore(formatLore(section.confirmLore(), targetName))
                        .build())
                .denyItem(() -> ItemBuilder.from(section.denyMaterial(), Material.RED_WOOL)
                        .name(section.denyName().replace("<profile>", targetName).replace("<name>", targetName))
                        .lore(formatLore(section.denyLore(), targetName))
                        .build())
                .onConfirm(onConfirm)
                .onDeny(onDeny)
                .open(player);
    }

    private String @NonNull [] formatLore(@NonNull List<String> raw, @NonNull String targetName) {
        List<String> formatted = new ArrayList<>(raw.size());
        for (String line : raw) {
            formatted.add(line.replace("<profile>", targetName).replace("<name>", targetName));
        }
        return formatted.toArray(new String[0]);
    }
}

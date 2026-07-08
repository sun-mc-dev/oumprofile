package dev.oum.profile.command;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.command.Argument;
import dev.oum.oumlib.command.Arguments;
import dev.oum.oumlib.command.CommandContext;
import dev.oum.oumlib.command.Commands;
import dev.oum.oumlib.text.Text;
import dev.oum.profile.model.ProfileData;
import dev.oum.profile.profile.ProfileManager;
import dev.oum.profile.profile.ProfileMenu;
import dev.oum.profile.util.ProfileIO;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ProfileCommand {

    private final ProfileManager manager;

    public ProfileCommand(@NonNull ProfileManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void register() {
        Commands.create("profile")
                .aliases("profiles", "prof")
                .permission(Permissions.USE)
                .subcommand(s -> s.label("gui").executes(this::onGui))
                .subcommand(s -> s.label("list").executes(this::onList))
                .subcommand(s -> s.label("current").executes(this::onCurrent))
                .subcommand(s -> s.label("create")
                        .argument(Arguments.word("name"))
                        .executes(this::onCreate))
                .subcommand(s -> s.label("switch")
                        .argument(Arguments.word("name").suggests(this::suggestOwnProfiles))
                        .executes(this::onSwitch))
                .subcommand(s -> s.label("delete")
                        .argument(Arguments.word("name").suggests(this::suggestOwnProfiles))
                        .executes(this::onDelete))
                .subcommand(s -> s.label("rename")
                        .argument(Arguments.word("old").suggests(this::suggestOwnProfiles))
                        .argument(Arguments.word("new"))
                        .executes(this::onRename))
                .subcommand(s -> s.label("reload")
                        .permission(Permissions.ADMIN)
                        .executes(this::onReload))
                .subcommand(s -> s.label("alerts")
                        .permission(Permissions.ALERTS)
                        .executes(this::onAlerts))
                .subcommand(s -> s.label("debug")
                        .permission(Permissions.ADMIN)
                        .executes(this::onDebug))
                .subcommand(s -> s.label("help").executes(this::onHelp))
                .subcommand(admin -> admin.label("admin")
                        .permission(Permissions.ADMIN)
                        .subcommand(s -> {
                            Argument<?> targetArg = Arguments.player("target");
                            s.label("open")
                                    .argument(targetArg)
                                    .executes(ctx -> this.onAdminOpen(ctx, targetArg));
                        })
                        .subcommand(s -> {
                            Argument<?> targetArg = Arguments.player("target");
                            s.label("list")
                                    .argument(targetArg)
                                    .executes(ctx -> this.onAdminList(ctx, targetArg));
                        })
                        .subcommand(s -> {
                            Argument<?> targetArg = Arguments.player("target");
                            s.label("create")
                                    .argument(targetArg)
                                    .argument(Arguments.word("profile"))
                                    .executes(ctx -> this.onAdminCreate(ctx, targetArg));
                        })
                        .subcommand(s -> {
                            Argument<?> targetArg = Arguments.player("target");
                            s.label("switch")
                                    .argument(targetArg)
                                    .argument(Arguments.word("profile").suggests(ctx
                                            -> this.suggestTargetProfiles(ctx, targetArg)))
                                    .executes(ctx -> this.onAdminSwitch(ctx, targetArg));
                        })
                        .subcommand(s -> {
                            Argument<?> targetArg = Arguments.player("target");
                            s.label("delete")
                                    .argument(targetArg)
                                    .argument(Arguments.word("profile").suggests(ctx
                                            -> this.suggestTargetProfiles(ctx, targetArg)))
                                    .executes(ctx -> this.onAdminDelete(ctx, targetArg));
                        })
                        .subcommand(s -> {
                            Argument<?> targetArg = Arguments.player("target");
                            s.label("rename")
                                    .argument(targetArg)
                                    .argument(Arguments.word("old").suggests(ctx
                                            -> this.suggestTargetProfiles(ctx, targetArg)))
                                    .argument(Arguments.word("new"))
                                    .executes(ctx -> this.onAdminRename(ctx, targetArg));
                        })
                        .subcommand(s -> {
                            Argument<?> targetArg = Arguments.player("target");
                            s.label("export")
                                    .argument(targetArg)
                                    .argument(Arguments.word("profile").suggests(ctx
                                            -> this.suggestTargetProfiles(ctx, targetArg)))
                                    .executes(ctx -> this.onAdminExport(ctx, targetArg));
                        })
                        .subcommand(s -> {
                            Argument<?> targetArg = Arguments.player("target");
                            s.label("import")
                                    .argument(targetArg)
                                    .argument(Arguments.word("file").suggests(ctx -> this.suggestExportFiles()))
                                    .executes(ctx -> this.onAdminImport(ctx, targetArg));
                        })
                )
                .executes(this::onGui)
                .register();
    }

    private @NonNull Collection<String> suggestOwnProfiles(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) return List.of();
        Player player = ctx.playerOrThrow();
        return manager.getProfiles(player.getUniqueId()).keySet();
    }

    private @NonNull Collection<String> suggestExportFiles() {
        File dir = ProfileIO.getExportsDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return List.of();
        List<String> names = new ArrayList<>();
        for (File f : files) {
            names.add(f.getName());
        }
        return names;
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

    private void onHelp(@NonNull CommandContext ctx) {
        Text.send(ctx.sender(), manager.configManager().get().messages().help());
    }

    private void onList(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            Text.send(ctx.sender(), manager.configManager().get().messages().playerOnly());
            return;
        }
        Player player = ctx.playerOrThrow();
        var profiles = manager.getProfiles(player.getUniqueId());
        String active = manager.getActiveProfileName(player.getUniqueId());

        if (profiles.isEmpty()) {
            Text.send(player, manager.configManager().get().messages().noProfiles());
            return;
        }

        Text.send(player, manager.configManager().get().messages().listHeader(), "count", String.valueOf(profiles.size()));
        for (ProfileData data : profiles.values()) {
            boolean isActive = data.name().equals(active);
            String format = isActive ? manager.configManager().get().messages().listItemActive()
                    : manager.configManager().get().messages().listItemInactive();
            Text.send(player, format,
                    "name", data.name(),
                    "date", dateFormatter().format(Instant.ofEpochMilli(data.lastUsed())));
        }
    }

    private void onCurrent(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            Text.send(ctx.sender(), manager.configManager().get().messages().playerOnly());
            return;
        }
        Player player = ctx.playerOrThrow();
        String active = manager.getActiveProfileName(player.getUniqueId());
        if (active == null) {
            Text.send(player, manager.configManager().get().messages().noActiveProfile());
            return;
        }
        Text.send(player, manager.configManager().get().messages().currentProfile(), "name", active);
    }

    private void onCreate(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            Text.send(ctx.sender(), manager.configManager().get().messages().playerOnly());
            return;
        }
        Player player = ctx.playerOrThrow();
        String name = ctx.args().getString("name");
        var msg = manager.configManager().get().messages();

        ProfileManager.NameValidation validation = manager.validateProfileName(name);
        switch (validation) {
            case EMPTY -> {
                Text.send(player, msg.invalidProfileName());
                return;
            }
            case TOO_LONG -> {
                Text.send(player, msg.profileNameTooLong(), "max",
                        String.valueOf(manager.configManager().get().profileNameMaxLength()));
                return;
            }
            case INVALID_CHARS -> {
                Text.send(player, msg.profileNameInvalidChars());
                return;
            }
            default -> {
            }
        }

        if (!player.hasPermission(Permissions.CREATE_PREFIX + name) && !player.hasPermission(Permissions.CREATE_ALL)) {
            Text.send(player, msg.noPermission(), "name", name);
            return;
        }

        boolean created = manager.createProfile(player, name);
        if (!created) {
            Text.send(player, msg.createFail(), "name", name);
            return;
        }

        Text.send(player, msg.createSuccess(), "name", name);
    }

    private void onSwitch(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            Text.send(ctx.sender(), manager.configManager().get().messages().playerOnly());
            return;
        }
        Player player = ctx.playerOrThrow();
        String name = ctx.args().getString("name");
        manager.requestSwitch(player, name);
    }

    private void onDelete(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            Text.send(ctx.sender(), manager.configManager().get().messages().playerOnly());
            return;
        }
        Player player = ctx.playerOrThrow();
        String name = ctx.args().getString("name");

        if (name.equalsIgnoreCase(manager.configManager().get().defaultProfileName())) {
            Text.send(player, manager.configManager().get().messages().cannotDeleteDefault());
            return;
        }

        boolean deleted = manager.deleteProfile(player, name);
        if (!deleted) {
            Text.send(player, manager.configManager().get().messages().deleteFail(), "name", name);
            return;
        }

        Text.send(player, manager.configManager().get().messages().deleteSuccess(), "name", name);
    }

    private void onRename(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            Text.send(ctx.sender(), manager.configManager().get().messages().playerOnly());
            return;
        }
        Player player = ctx.playerOrThrow();
        String oldName = ctx.args().getString("old");
        String newName = ctx.args().getString("new");
        var msg = manager.configManager().get().messages();

        if (oldName.equalsIgnoreCase(manager.configManager().get().defaultProfileName())) {
            Text.send(player, msg.cannotRenameDefault());
            return;
        }

        ProfileManager.NameValidation validation = manager.validateProfileName(newName);
        switch (validation) {
            case EMPTY -> {
                Text.send(player, msg.invalidProfileName());
                return;
            }
            case TOO_LONG -> {
                Text.send(player, msg.profileNameTooLong(), "max",
                        String.valueOf(manager.configManager().get().profileNameMaxLength()));
                return;
            }
            case INVALID_CHARS -> {
                Text.send(player, msg.profileNameInvalidChars());
                return;
            }
            default -> {
            }
        }

        boolean renamed = manager.renameProfile(player, oldName, newName);
        if (renamed) {
            Text.send(player, msg.renameSuccess(), "old", oldName, "new", newName);
        } else {
            Text.send(player, msg.renameFail(), "old", oldName);
        }
    }

    private void onReload(@NonNull CommandContext ctx) {
        manager.configManager().reload();
        Text.send(ctx.sender(), manager.configManager().get().messages().reloadSuccess());
    }

    private void onGui(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            Text.send(ctx.sender(), manager.configManager().get().messages().playerOnly());
            return;
        }
        new ProfileMenu(manager).open(ctx.playerOrThrow());
    }

    private @NonNull Collection<String> suggestTargetProfiles(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        try {
            Player target = (Player) ctx.args().get(targetArg);
            if (target != null) {
                return manager.getProfiles(target.getUniqueId()).keySet();
            }
        } catch (Exception ignored) {
        }
        return List.of();
    }

    private void onAdminOpen(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        Player target = (Player) ctx.args().get(targetArg);
        var msg = manager.configManager().get().messages();
        if (target == null) {
            Text.send(ctx.sender(), msg.playerNotFound());
            return;
        }
        new ProfileMenu(manager).open(target);
        Text.send(ctx.sender(), msg.adminOpenSuccess(), "target", target.getName());
    }

    private void onAdminList(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        Player target = (Player) ctx.args().get(targetArg);
        var msg = manager.configManager().get().messages();
        if (target == null) {
            Text.send(ctx.sender(), msg.playerNotFound());
            return;
        }
        var profiles = manager.getProfiles(target.getUniqueId());
        String active = manager.getActiveProfileName(target.getUniqueId());

        if (profiles.isEmpty()) {
            Text.send(ctx.sender(), msg.noProfiles());
            return;
        }

        Text.send(ctx.sender(), msg.adminListHeader(), "target", target.getName(), "count", String.valueOf(profiles.size()));
        for (ProfileData data : profiles.values()) {
            boolean isActive = data.name().equals(active);
            String format = isActive ? msg.listItemActive() : msg.listItemInactive();
            Text.send(ctx.sender(), format,
                    "name", data.name(),
                    "date", dateFormatter().format(Instant.ofEpochMilli(data.lastUsed())));
        }
    }

    private void onAdminCreate(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        Player target = (Player) ctx.args().get(targetArg);
        var msg = manager.configManager().get().messages();
        if (target == null) {
            Text.send(ctx.sender(), msg.playerNotFound());
            return;
        }
        String profileName = ctx.args().getString("profile");

        ProfileManager.NameValidation validation = manager.validateProfileName(profileName);
        switch (validation) {
            case EMPTY -> {
                Text.send(ctx.sender(), msg.invalidProfileName());
                return;
            }
            case TOO_LONG -> {
                Text.send(ctx.sender(), msg.profileNameTooLong(), "max",
                        String.valueOf(manager.configManager().get().profileNameMaxLength()));
                return;
            }
            case INVALID_CHARS -> {
                Text.send(ctx.sender(), msg.profileNameInvalidChars());
                return;
            }
            default -> {
            }
        }

        boolean created = manager.createProfile(target, profileName);
        if (created) {
            Text.send(ctx.sender(), msg.adminCreateSuccess(), "name", profileName, "target", target.getName());
        } else {
            Text.send(ctx.sender(), msg.adminCreateFail());
        }
    }

    private void onAdminSwitch(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        Player target = (Player) ctx.args().get(targetArg);
        var msg = manager.configManager().get().messages();
        if (target == null) {
            Text.send(ctx.sender(), msg.playerNotFound());
            return;
        }
        String profileName = ctx.args().getString("profile");
        if (!manager.hasProfile(target.getUniqueId(), profileName)) {
            Text.send(ctx.sender(), msg.adminSwitchFailNoProfile(), "name", profileName);
            return;
        }

        manager.cancelWarmup(target.getUniqueId());
        manager.performSwitch(target, profileName);
        Text.send(ctx.sender(), msg.adminSwitchSuccess(), "target", target.getName(), "name", profileName);
    }

    private void onAdminDelete(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        Player target = (Player) ctx.args().get(targetArg);
        var msg = manager.configManager().get().messages();
        if (target == null) {
            Text.send(ctx.sender(), msg.playerNotFound());
            return;
        }
        String profileName = ctx.args().getString("profile");
        boolean deleted = manager.deleteProfile(target, profileName);
        if (deleted) {
            Text.send(ctx.sender(), msg.adminDeleteSuccess(), "name", profileName, "target", target.getName());
        } else {
            Text.send(ctx.sender(), msg.adminDeleteFail());
        }
    }

    private void onAdminRename(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        Player target = (Player) ctx.args().get(targetArg);
        var msg = manager.configManager().get().messages();
        if (target == null) {
            Text.send(ctx.sender(), msg.playerNotFound());
            return;
        }
        String oldName = ctx.args().getString("old");
        String newName = ctx.args().getString("new");

        if (oldName.equalsIgnoreCase(manager.configManager().get().defaultProfileName())) {
            Text.send(ctx.sender(), msg.cannotRenameDefault());
            return;
        }

        ProfileManager.NameValidation validation = manager.validateProfileName(newName);
        switch (validation) {
            case EMPTY -> {
                Text.send(ctx.sender(), msg.invalidProfileName());
                return;
            }
            case TOO_LONG -> {
                Text.send(ctx.sender(), msg.profileNameTooLong(), "max",
                        String.valueOf(manager.configManager().get().profileNameMaxLength()));
                return;
            }
            case INVALID_CHARS -> {
                Text.send(ctx.sender(), msg.profileNameInvalidChars());
                return;
            }
            default -> {
            }
        }

        boolean renamed = manager.renameProfile(target, oldName, newName);
        if (renamed) {
            Text.send(ctx.sender(), msg.adminRenameSuccess(), "old", oldName, "new", newName, "target", target.getName());
        } else {
            Text.send(ctx.sender(), msg.adminRenameFail());
        }
    }

    private void onAdminExport(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        Player target = (Player) ctx.args().get(targetArg);
        var msg = manager.configManager().get().messages();
        if (target == null) {
            Text.send(ctx.sender(), msg.playerNotFound());
            return;
        }
        String profileName = ctx.args().getString("profile");
        var profiles = manager.getProfiles(target.getUniqueId());
        ProfileData data = profiles.get(profileName);
        if (data == null) {
            Text.send(ctx.sender(), msg.profileNotFound(), "target", profileName);
            return;
        }

        File exportFile = new File(ProfileIO.getExportsDir(),
                target.getUniqueId() + "_" + profileName + ".json");
        boolean success = ProfileIO.exportToFile(data, exportFile);
        if (success) {
            Text.send(ctx.sender(), msg.exportSuccess(), "name", profileName);
        } else {
            Text.send(ctx.sender(), msg.importFail());
        }
    }

    private void onAdminImport(@NonNull CommandContext ctx, @NonNull Argument<?> targetArg) {
        Player target = (Player) ctx.args().get(targetArg);
        var msg = manager.configManager().get().messages();
        if (target == null) {
            Text.send(ctx.sender(), msg.playerNotFound());
            return;
        }
        String fileName = ctx.args().getString("file");
        File importFile = new File(ProfileIO.getExportsDir(), fileName);
        if (!importFile.exists()) {
            Text.send(ctx.sender(), msg.importFail());
            return;
        }

        ProfileData imported = ProfileIO.importFromFile(importFile);
        if (imported == null) {
            Text.send(ctx.sender(), msg.importFail());
            return;
        }

        if (manager.hasProfile(target.getUniqueId(), imported.name())) {
            Text.send(ctx.sender(), msg.adminCreateFail());
            return;
        }

        if (manager.getProfiles(target.getUniqueId()).size() >= manager.getMaxProfiles(target)) {
            Text.send(ctx.sender(), msg.adminCreateFail());
            return;
        }

        boolean created = manager.createProfile(target, imported.name());
        if (!created) {
            Text.send(ctx.sender(), msg.importFail());
            return;
        }

        var profiles = manager.getProfiles(target.getUniqueId());
        profiles.put(imported.name(), imported);
        manager.storage().save(target.getUniqueId(), imported);
        Text.send(ctx.sender(), msg.importSuccess(), "name", imported.name(), "target", target.getName());
    }

    private void onAlerts(@NonNull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            Text.send(ctx.sender(), manager.configManager().get().messages().playerOnly());
            return;
        }
        Player player = ctx.playerOrThrow();
        boolean enabled = manager.toggleAlerts(player.getUniqueId());
        if (enabled) {
            Text.send(player, manager.configManager().get().messages().alertsEnabled());
        } else {
            Text.send(player, manager.configManager().get().messages().alertsDisabled());
        }
    }

    private void onDebug(@NonNull CommandContext ctx) {
        boolean current = OumLib.isDebug();
        OumLib.setDebug(!current);
        boolean enabled = !current;
        if (enabled) {
            Text.send(ctx.sender(), manager.configManager().get().messages().debugEnabled());
        } else {
            Text.send(ctx.sender(), manager.configManager().get().messages().debugDisabled());
        }
    }
}
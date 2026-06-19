package dev.oum.profile;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.config.ConfigManager;
import dev.oum.oumlib.text.Text;
import dev.oum.profile.api.ProfileAPI;
import dev.oum.profile.command.ProfileCommand;
import dev.oum.profile.config.ProfileConfig;
import dev.oum.profile.config.ProfileStorage;
import dev.oum.profile.profile.ProfileListener;
import dev.oum.profile.profile.ProfileManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class OumProfile extends JavaPlugin {

    private ProfileStorage storage;
    private ProfileManager manager;

    @Override
    public void onEnable() {
        OumLib.init(this);

        ConfigManager<ProfileConfig> configManager = ConfigManager.of(ProfileConfig.class,
                        "config.yml", ProfileConfig::defaults)
                .onReload(cfg -> OumLib.setDebug(cfg.debug()))
                .enableAutoReload();

        OumLib.setDebug(configManager.get().debug());

        storage = new ProfileStorage(configManager.get().storage());
        manager = new ProfileManager(configManager, storage);
        ProfileAPI.init(manager);

        ProfilePlaceholders.register(manager);

        new ProfileListener(manager, configManager);
        new ProfileCommand(manager).register();

        List.of(
                "<gradient:#33c3f0:#0088cc>   ___                   ___            __ _ _      </gradient>",
                "<gradient:#33c3f0:#0088cc>  ╱___╲_   _ _ __ ___   ╱ _ ╲_ __ ___  ╱ _(_) │ ___ </gradient>",
                "<gradient:#33c3f0:#0088cc> ╱╱  ╱╱ │ │ │ '_ ` _ ╲ ╱ ╱_)╱ '__╱ _ ╲│ │_│ │ │╱ _ ╲</gradient>",
                "<gradient:#33c3f0:#0088cc>╱ ╲_╱╱│ │_│ │ │ │ │ │ ╱ ___╱│ │ │ (_) │  _│ │ │  __╱</gradient>",
                "<gradient:#33c3f0:#0088cc>╲___╱  ╲__,_│_│ │_│ │_╲╱    │_│  ╲___╱│_│ │_│_│╲___│</gradient>",
                "",
                "<green>OumProfile has been successfully enabled! [v" + getPluginMeta().getVersion() + "]</green>",
                ""
        ).forEach(line -> OumLib.console().sendMessage(Text.parse(line)));
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.shutdown();
        if (storage != null) storage.close();
        OumLib.shutdown();
    }
}
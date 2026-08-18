package dev.oum.profile.config;

import dev.oum.oumlib.config.ConfigManager;
import org.jspecify.annotations.NonNull;

public final class ProfileConfig {

    private final ConfigManager<MainConfig> main;
    private final ConfigManager<MenusConfig> menus;
    private final ConfigManager<MessagesConfig> messages;

    public ProfileConfig(
            @NonNull ConfigManager<MainConfig> main,
            @NonNull ConfigManager<MenusConfig> menus,
            @NonNull ConfigManager<MessagesConfig> messages
    ) {
        this.main = main;
        this.menus = menus;
        this.messages = messages;
    }

    public static @NonNull ProfileConfig create(Runnable onReloadAction) {
        ConfigManager<MainConfig> main = ConfigManager.of(MainConfig.class, "config.yml", MainConfig::defaults)
                .onReload(cfg -> {
                    if (onReloadAction != null) onReloadAction.run();
                })
                .enableAutoReload();

        ConfigManager<MenusConfig> menus = ConfigManager.of(MenusConfig.class, "menus.yml", MenusConfig::defaults)
                .enableAutoReload();

        ConfigManager<MessagesConfig> messages = ConfigManager.of(MessagesConfig.class, "messages.yml", MessagesConfig::defaults)
                .enableAutoReload();

        return new ProfileConfig(main, menus, messages);
    }

    public @NonNull MainConfig main() {
        return main.get();
    }

    public @NonNull MenusConfig menus() {
        return menus.get();
    }

    public @NonNull MessagesConfig messages() {
        return messages.get();
    }

    public void reload() {
        main.reload();
        menus.reload();
        messages.reload();
    }
}
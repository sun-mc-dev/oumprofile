package dev.oum.profile.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public final class ProfileLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String activeProfile;

    public ProfileLoadEvent(@NonNull Player player, @NonNull String activeProfile) {
        this.player = player;
        this.activeProfile = activeProfile;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NonNull Player player() {
        return player;
    }

    public @NonNull String activeProfile() {
        return activeProfile;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
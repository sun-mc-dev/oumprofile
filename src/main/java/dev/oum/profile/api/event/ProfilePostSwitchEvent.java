package dev.oum.profile.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public final class ProfilePostSwitchEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String fromProfile;
    private final String toProfile;

    public ProfilePostSwitchEvent(@NonNull Player player, @NonNull String fromProfile, @NonNull String toProfile) {
        this.player = player;
        this.fromProfile = fromProfile;
        this.toProfile = toProfile;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NonNull Player player() {
        return player;
    }

    public @NonNull String fromProfile() {
        return fromProfile;
    }

    public @NonNull String toProfile() {
        return toProfile;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
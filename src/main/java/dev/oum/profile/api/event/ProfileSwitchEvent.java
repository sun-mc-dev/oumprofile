package dev.oum.profile.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public final class ProfileSwitchEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String fromProfile;
    private final String toProfile;
    private boolean cancelled;

    public ProfileSwitchEvent(@NonNull Player player, @NonNull String fromProfile, @NonNull String toProfile) {
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
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
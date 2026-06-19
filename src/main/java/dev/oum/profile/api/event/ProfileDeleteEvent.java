package dev.oum.profile.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public final class ProfileDeleteEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String profileName;
    private boolean cancelled;

    public ProfileDeleteEvent(@NonNull Player player, @NonNull String profileName) {
        this.player = player;
        this.profileName = profileName;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NonNull Player player() {
        return player;
    }

    public @NonNull String profileName() {
        return profileName;
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
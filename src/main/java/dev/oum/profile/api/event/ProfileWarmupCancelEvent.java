package dev.oum.profile.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public final class ProfileWarmupCancelEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String reason;

    public ProfileWarmupCancelEvent(@NonNull Player player, @Nullable String reason) {
        this.player = player;
        this.reason = reason;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NonNull Player player() {
        return player;
    }

    public @Nullable String reason() {
        return reason;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }
}

package dev.oum.profile.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public class ProfileRenameEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String oldName;
    private final String newName;
    private boolean cancelled;

    public ProfileRenameEvent(@NonNull Player player, @NonNull String oldName, @NonNull String newName) {
        super(player);
        this.oldName = oldName;
        this.newName = newName;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NonNull String getOldName() {
        return oldName;
    }

    public @NonNull String getNewName() {
        return newName;
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

package dev.oum.profile.profile;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.config.ConfigManager;
import dev.oum.oumlib.event.Events;
import dev.oum.profile.config.ProfileConfig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.jspecify.annotations.NonNull;

public final class ProfileListener {

    public ProfileListener(@NonNull ProfileManager manager, @NonNull ConfigManager<ProfileConfig> configManager) {
        Events.listen(PlayerJoinEvent.class)
                .handler(e -> {
                    OumLib.logDebug("PlayerJoinEvent fired for player " + e.getPlayer().getName());
                    manager.loadPlayer(e.getPlayer());
                });

        Events.listen(PlayerQuitEvent.class)
                .handler(e -> {
                    OumLib.logDebug("PlayerQuitEvent fired for player " + e.getPlayer().getName());
                    manager.unloadPlayer(e.getPlayer());
                });

        Events.listen(PlayerMoveEvent.class)
                .filter(e -> configManager.get().switching().cancelOnMove())
                .filter(e -> manager.hasPendingWarmup(e.getPlayer().getUniqueId()))
                .filter(e -> {
                    var from = e.getFrom();
                    var to = e.getTo();
                    return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
                })
                .handler(e -> {
                    OumLib.logDebug("Cancelling profile switch warmup for player " + e.getPlayer().getName() + " due to movement.");
                    manager.cancelWarmup(e.getPlayer().getUniqueId(), configManager.get().messages().warmupCancelledMove());
                });

        Events.listen(EntityDamageByEntityEvent.class)
                .filter(e -> e.getEntity() instanceof Player)
                .handler(e -> {
                    Player victim = (Player) e.getEntity();
                    Player attacker = switch (e.getDamager()) {
                        case Player p -> p;
                        case Projectile proj when proj.getShooter() instanceof Player shooter -> shooter;
                        default -> null;
                    };

                    if (attacker != null) {
                        OumLib.logDebug("Combat tag applied to victim " + victim.getName() + " and attacker " + attacker.getName());
                        manager.combatCooldown().set(victim.getUniqueId());
                        manager.combatCooldown().set(attacker.getUniqueId());
                    }

                    if (configManager.get().switching().cancelOnDamage() && manager.hasPendingWarmup(victim.getUniqueId())) {
                        OumLib.logDebug("Cancelling profile switch warmup for player " + victim.getName() + " due to combat damage.");
                        manager.cancelWarmup(victim.getUniqueId(), configManager.get().messages().warmupCancelledDamage());
                    }
                });

        Events.listen(InventoryOpenEvent.class)
                .filter(e -> e.getPlayer() instanceof Player)
                .filter(e -> manager.hasPendingWarmup(e.getPlayer().getUniqueId()))
                .handler(e -> {
                    Player p = (Player) e.getPlayer();
                    OumLib.logDebug("Cancelling profile switch warmup for player " + p.getName() + " due to opening an inventory.");
                    manager.cancelWarmup(p.getUniqueId(), configManager.get().messages().warmupCancelledGeneric());
                });

        Events.listen(PlayerDropItemEvent.class)
                .filter(e -> manager.hasPendingWarmup(e.getPlayer().getUniqueId()))
                .handler(e -> {
                    OumLib.logDebug("Cancelling profile switch warmup for player " + e.getPlayer().getName() + " due to dropping an item.");
                    manager.cancelWarmup(e.getPlayer().getUniqueId(), configManager.get().messages().warmupCancelledGeneric());
                });

        Events.listen(EntityPickupItemEvent.class)
                .filter(e -> e.getEntity() instanceof Player)
                .filter(e -> manager.hasPendingWarmup(e.getEntity().getUniqueId()))
                .handler(e -> {
                    Player p = (Player) e.getEntity();
                    OumLib.logDebug("Cancelling profile switch warmup for player " + p.getName() + " due to picking up an item.");
                    manager.cancelWarmup(p.getUniqueId(), configManager.get().messages().warmupCancelledGeneric());
                });

        Events.listen(PlayerInteractEvent.class)
                .filter(e -> manager.hasPendingWarmup(e.getPlayer().getUniqueId()))
                .handler(e -> {
                    OumLib.logDebug("Cancelling profile switch warmup for player " + e.getPlayer().getName() + " due to interaction.");
                    manager.cancelWarmup(e.getPlayer().getUniqueId(), configManager.get().messages().warmupCancelledGeneric());
                });

        Events.listen(PlayerTeleportEvent.class)
                .filter(e -> manager.hasPendingWarmup(e.getPlayer().getUniqueId()))
                .handler(e -> {
                    OumLib.logDebug("Cancelling profile switch warmup for player " + e.getPlayer().getName() + " due to teleportation.");
                    manager.cancelWarmup(e.getPlayer().getUniqueId(), configManager.get().messages().warmupCancelledGeneric());
                });
    }
}
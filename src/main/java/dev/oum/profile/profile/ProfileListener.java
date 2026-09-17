package dev.oum.profile.profile;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.bridge.combat.CombatBridge;
import dev.oum.oumlib.event.Events;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

public final class ProfileListener {

    public ProfileListener(@NonNull ProfileManager manager) {
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
                .filter(e -> manager.config().main().switching().cancelOnMove())
                .filter(e -> manager.hasPendingWarmup(e.getPlayer().getUniqueId()))
                .filter(e -> {
                    var to = e.getTo();
                    if (to == null) return false;
                    var from = e.getFrom();
                    return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
                })
                .handler(e -> cancelIfWarmup(manager, e.getPlayer(), "movement", manager.config().messages().warmupCancelledMove()));

        Events.listen(EntityDamageEvent.class)
                .filter(e -> e.getEntity() instanceof Player)
                .filter(e -> manager.config().main().switching().cancelOnDamage())
                .handler(e -> cancelIfWarmup(manager, (Player) e.getEntity(), "damage (" + e.getCause() + ")", manager.config().messages().warmupCancelledDamage()));

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
                        Duration duration = Duration.ofSeconds(manager.config().main().switching().combatTagDuration());
                        CombatBridge.tag(victim, duration);
                        CombatBridge.tag(attacker, duration);
                    }
                });

        Events.listen(PlayerDeathEvent.class)
                .handler(e -> cancelIfWarmup(manager, e.getEntity(), "death"));

        Events.listen(InventoryOpenEvent.class)
                .filter(e -> e.getPlayer() instanceof Player)
                .handler(e -> cancelIfWarmup(manager, (Player) e.getPlayer(), "opening an inventory"));

        Events.listen(InventoryClickEvent.class)
                .filter(e -> e.getWhoClicked() instanceof Player)
                .handler(e -> cancelIfWarmup(manager, (Player) e.getWhoClicked(), "inventory click"));

        Events.listen(PlayerDropItemEvent.class)
                .handler(e -> cancelIfWarmup(manager, e.getPlayer(), "dropping an item"));

        Events.listen(EntityPickupItemEvent.class)
                .filter(e -> e.getEntity() instanceof Player)
                .handler(e -> cancelIfWarmup(manager, (Player) e.getEntity(), "picking up an item"));

        Events.listen(PlayerInteractEvent.class)
                .handler(e -> cancelIfWarmup(manager, e.getPlayer(), "interaction"));

        Events.listen(PlayerTeleportEvent.class)
                .handler(e -> cancelIfWarmup(manager, e.getPlayer(), "teleportation"));

        Events.listen(PlayerChangedWorldEvent.class)
                .handler(e -> cancelIfWarmup(manager, e.getPlayer(), "world change"));

        Events.listen(VehicleEnterEvent.class)
                .filter(e -> e.getEntered() instanceof Player)
                .handler(e -> cancelIfWarmup(manager, (Player) e.getEntered(), "entering a vehicle"));
    }

    private static void cancelIfWarmup(@NonNull ProfileManager manager, @NonNull Player player, @NonNull String reason, @NonNull String message) {
        if (manager.hasPendingWarmup(player.getUniqueId())) {
            OumLib.logDebug("Cancelling profile switch warmup for player " + player.getName() + " due to " + reason + ".");
            manager.cancelWarmup(player.getUniqueId(), message);
        }
    }

    private static void cancelIfWarmup(@NonNull ProfileManager manager, @NonNull Player player, @NonNull String reason) {
        cancelIfWarmup(manager, player, reason, manager.config().messages().warmupCancelledGeneric());
    }
}
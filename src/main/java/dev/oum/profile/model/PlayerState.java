package dev.oum.profile.model;

import com.google.gson.Gson;
import dev.oum.oumlib.util.ItemSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record PlayerState(
        String inventory,
        String armor,
        String offhand,
        String enderChest,
        double health,
        double maxHealth,
        int foodLevel,
        float saturation,
        int xpLevel,
        float xpProgress,
        String gameMode,
        List<PotionEffectEntry> potionEffects,
        float fallDistance,
        int fireTicks,
        int remainingAir,
        boolean allowFlight,
        boolean isFlying,
        @Nullable LocationEntry location
) {

    private static final Gson GSON = new Gson();

    public static @NonNull PlayerState fresh() {
        return new PlayerState(
                ItemSerializer.serializeArray(new ItemStack[36]),
                ItemSerializer.serializeArray(new ItemStack[4]),
                ItemSerializer.serialize(null),
                ItemSerializer.serializeArray(new ItemStack[27]),
                20.0, 20.0, 20, 5.0f, 0, 0.0f,
                GameMode.SURVIVAL.name(),
                new ArrayList<>(),
                0.0f,
                0,
                300,
                false,
                false,
                null
        );
    }

    public static @NonNull PlayerState capture(@NonNull Player player, boolean saveLocation) {
        ItemStack[] invSlots = player.getInventory().getStorageContents();

        var maxHpAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double max = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;

        List<PotionEffectEntry> effects = new ArrayList<>();
        for (PotionEffect e : player.getActivePotionEffects()) {
            effects.add(new PotionEffectEntry(e.getType().getKey().toString(), e.getDuration(), e.getAmplifier(),
                    e.isAmbient(), e.hasParticles(), e.hasIcon()));
        }

        LocationEntry loc = null;
        if (saveLocation) {
            var location = player.getLocation();
            loc = new LocationEntry(
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch()
            );
        }

        return new PlayerState(
                ItemSerializer.serializeArray(invSlots),
                ItemSerializer.serializeArray(player.getInventory().getArmorContents()),
                ItemSerializer.serialize(player.getInventory().getItemInOffHand()),
                ItemSerializer.serializeArray(player.getEnderChest().getContents()),
                Math.min(player.getHealth(), max),
                max,
                player.getFoodLevel(),
                player.getSaturation(),
                player.getLevel(),
                player.getExp(),
                player.getGameMode().name(),
                effects,
                player.getFallDistance(),
                player.getFireTicks(),
                player.getRemainingAir(),
                player.getAllowFlight(),
                player.isFlying(),
                loc
        );
    }

    public static @NonNull PlayerState fromJson(@NonNull String json) {
        return GSON.fromJson(json, PlayerState.class);
    }

    public void apply(@NonNull Player player, boolean restoreLocation) {
        player.getInventory().setStorageContents(ItemSerializer.deserializeArray(inventory));
        player.getInventory().setArmorContents(ItemSerializer.deserializeArray(armor));
        player.getInventory().setItemInOffHand(ItemSerializer.deserialize(offhand));

        ItemStack[] chest = ItemSerializer.deserializeArray(enderChest);
        for (int i = 0; i < Math.min(chest.length, player.getEnderChest().getSize()); i++) {
            player.getEnderChest().setItem(i, chest[i]);
        }

        var maxHpAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHpAttr != null) maxHpAttr.setBaseValue(maxHealth);
        player.setHealth(Math.min(health, maxHealth));
        player.setFoodLevel(foodLevel);
        player.setSaturation(saturation);
        player.setLevel(xpLevel);
        player.setExp(xpProgress);

        try {
            player.setGameMode(GameMode.valueOf(gameMode));
        } catch (IllegalArgumentException ignored) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        for (PotionEffectEntry entry : potionEffects) {
            String typeStr = entry.type();
            NamespacedKey key = typeStr.contains(":")
                    ? NamespacedKey.fromString(typeStr)
                    : NamespacedKey.minecraft(typeStr.toLowerCase(Locale.ROOT));

            PotionEffectType type = key != null ? Registry.MOB_EFFECT.get(key) : null;
            if (type != null) {
                player.addPotionEffect(new PotionEffect(type, entry.duration(), entry.amplifier(),
                        entry.ambient(), entry.particles(), entry.icon()));
            }
        }

        player.setFallDistance(fallDistance);
        player.setFireTicks(fireTicks);
        player.setRemainingAir(remainingAir > 0 ? remainingAir : player.getMaximumAir());
        player.setAllowFlight(allowFlight);
        player.setFlying(allowFlight && isFlying);

        if (restoreLocation && location != null) {
            var world = Bukkit.getWorld(location.world());
            if (world != null) {
                player.teleport(new Location(world, location.x(), location.y(), location.z(), location.yaw(), location.pitch()));
            }
        }

        player.updateInventory();
    }

    public @NonNull String toJson() {
        return GSON.toJson(this);
    }

    public record PotionEffectEntry(String type, int duration, int amplifier, boolean ambient, boolean particles,
                                    boolean icon) {
    }

    public record LocationEntry(
            String world,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
    }
}
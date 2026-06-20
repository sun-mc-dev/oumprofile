package dev.oum.profile.model;

import com.google.gson.Gson;
import dev.oum.oumlib.bridge.economy.EconomyBridge;
import dev.oum.oumlib.util.ItemSerializer;
import dev.oum.profile.config.ProfileConfig;
import dev.oum.profile.integration.IntegrationManager;
import dev.oum.profile.integration.SkillData;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

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
        @Nullable LocationEntry location,

        @Nullable Map<String, SkillData> mcmmo,
        @Nullable Map<String, SkillData> auraskills,
        @Nullable Map<String, SkillData> jobs,
        @Nullable Map<String, Double> currencies,
        @Nullable Map<String, Integer> statistics,
        @Nullable Long playtimeSeconds
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
                null,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                0L
        );
    }

    public static @NonNull PlayerState capture(@NonNull Player player, boolean saveLocation,
                                               @NonNull ProfileConfig config, long currentPlaytimeSeconds) {
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

        Map<String, Double> currencies = new HashMap<>();
        if (config.economy() != null && config.economy().enabled() && config.economy().currencies() != null) {
            for (String currency : config.economy().currencies()) {
                try {
                    double bal = EconomyBridge.balance(currency, player);
                    currencies.put(currency, bal);
                } catch (Throwable ignored) {
                }
            }
        }

        Map<String, Integer> statistics = new HashMap<>();
        if (config.statistics() != null && config.statistics().enabled() && config.statistics().tracked() != null) {
            for (String entry : config.statistics().tracked()) {
                try {
                    String[] parts = entry.split(":", 2);
                    Statistic statistic = Statistic.valueOf(parts[0].toUpperCase(Locale.ROOT));
                    if (parts.length == 1) {
                        statistics.put(entry, player.getStatistic(statistic));
                    } else {
                        String param = parts[1].toUpperCase(Locale.ROOT);
                        if (statistic.getType() == Statistic.Type.BLOCK || statistic.getType() == Statistic.Type.ITEM) {
                            Material mat = Material.valueOf(param);
                            statistics.put(entry, player.getStatistic(statistic, mat));
                        } else if (statistic.getType() == Statistic.Type.ENTITY) {
                            EntityType entityType = EntityType.valueOf(param);
                            statistics.put(entry, player.getStatistic(statistic, entityType));
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        Map<String, SkillData> mcmmoData = null;
        if (config.skills() != null && config.skills().mcmmoEnabled()) {
            mcmmoData = IntegrationManager.mcmmo().capture(player);
        }
        Map<String, SkillData> auraskillsData = null;
        if (config.skills() != null && config.skills().auraSkillsEnabled()) {
            auraskillsData = IntegrationManager.auraSkills().capture(player);
        }
        Map<String, SkillData> jobsData = null;
        if (config.skills() != null && config.skills().jobsEnabled()) {
            jobsData = IntegrationManager.jobs().capture(player);
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
                loc,
                mcmmoData,
                auraskillsData,
                jobsData,
                currencies,
                statistics,
                currentPlaytimeSeconds
        );
    }

    public static @NonNull PlayerState fromJson(@NonNull String json) {
        return GSON.fromJson(json, PlayerState.class);
    }

    public void apply(@NonNull Player player, boolean restoreLocation, @NonNull ProfileConfig config) {
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

        if (config.statistics() != null && config.statistics().enabled() && statistics != null) {
            for (String entry : config.statistics().tracked()) {
                try {
                    String[] parts = entry.split(":", 2);
                    Statistic statistic = Statistic.valueOf(parts[0].toUpperCase(Locale.ROOT));
                    int value = statistics.getOrDefault(entry, 0);
                    if (parts.length == 1) {
                        player.setStatistic(statistic, value);
                    } else {
                        String param = parts[1].toUpperCase(Locale.ROOT);
                        if (statistic.getType() == Statistic.Type.BLOCK || statistic.getType() == Statistic.Type.ITEM) {
                            Material mat = Material.valueOf(param);
                            player.setStatistic(statistic, mat, value);
                        } else if (statistic.getType() == Statistic.Type.ENTITY) {
                            EntityType entityType = EntityType.valueOf(param);
                            player.setStatistic(statistic, entityType, value);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        if (config.skills() != null && config.skills().mcmmoEnabled() && mcmmo != null) {
            IntegrationManager.mcmmo().restore(player, mcmmo);
        }
        if (config.skills() != null && config.skills().auraSkillsEnabled() && auraskills != null) {
            IntegrationManager.auraSkills().restore(player, auraskills);
        }
        if (config.skills() != null && config.skills().jobsEnabled() && jobs != null) {
            IntegrationManager.jobs().restore(player, jobs);
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
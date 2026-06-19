package dev.oum.profile.command;

import dev.oum.oumlib.util.Permission;

public final class Permissions {

    public static final Permission USE = Permission.builder("profiles.use")
            .defaultValue(Permission.Default.TRUE)
            .build();

    public static final Permission ADMIN = Permission.builder("profiles.admin")
            .defaultValue(Permission.Default.OP)
            .build();

    public static final Permission ALERTS = Permission.builder("profiles.alerts")
            .defaultValue(Permission.Default.OP)
            .build();

    public static final String MAX_UNLIMITED = "profiles.max.unlimited";
    public static final String MAX_TIER_PREFIX = "profiles.max.";
    public static final String CREATE_PREFIX = "profiles.create.";
    public static final String CREATE_ALL = "profiles.create.*";
    public static final String BYPASS_COMBAT = "profiles.bypass.combat";
    public static final String BYPASS_WARMUP = "profiles.bypass.warmup";
    public static final String BYPASS_COOLDOWN = "profiles.bypass.cooldown";

    private Permissions() {
    }
}

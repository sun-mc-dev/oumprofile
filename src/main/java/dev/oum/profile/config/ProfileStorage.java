package dev.oum.profile.config;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.database.Database;
import dev.oum.oumlib.scheduler.Promise;
import dev.oum.profile.model.PlayerState;
import dev.oum.profile.model.ProfileData;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class ProfileStorage {

    private final Database db;
    private final boolean mysql;
    private final String saveSql;

    public ProfileStorage(MainConfig.@NonNull StorageSection cfg) {
        this.mysql = cfg.type().equalsIgnoreCase("mysql");
        if (mysql) {
            this.db = Database.mysql(cfg.host(), cfg.port(), cfg.database(), cfg.username(), cfg.password());
            db.runMigrations(ProfileStorage.class,
                    "migrations/mysql/V1__init.sql",
                    "migrations/mysql/V2__add_active_column.sql");
            this.saveSql = "INSERT INTO oum_profiles (uuid, name, created_at, last_used, state_json, balance, primary_group, groups_json, active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE last_used = VALUES(last_used), state_json = VALUES(state_json), " +
                    "balance = VALUES(balance), primary_group = VALUES(primary_group), groups_json = VALUES(groups_json), active = VALUES(active)";
        } else {
            String filename = cfg.database().endsWith(".db") ? cfg.database() : cfg.database() + ".db";
            this.db = Database.sqlite(new File(OumLib.getDataFolder(), filename));
            db.runMigrations(ProfileStorage.class,
                    "migrations/sqlite/V1__init.sql",
                    "migrations/sqlite/V2__add_active_column.sql");
            this.saveSql = "INSERT INTO oum_profiles (uuid, name, created_at, last_used, state_json, balance, primary_group, groups_json, active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (uuid, name) DO UPDATE SET " +
                    "last_used = excluded.last_used, state_json = excluded.state_json, balance = excluded.balance, " +
                    "primary_group = excluded.primary_group, groups_json = excluded.groups_json, active = excluded.active";
        }
    }

    private static @NonNull String id(@NonNull UUID uuid) {
        return uuid.toString().toLowerCase(Locale.ROOT);
    }

    public @NonNull Promise<List<ProfileData>> loadAll(@NonNull UUID uuid) {
        return db.executeQuery(
                "SELECT name, created_at, last_used, state_json, balance, primary_group, groups_json, active FROM oum_profiles WHERE uuid = ?",
                rs -> new ProfileData(
                        rs.getString("name"),
                        rs.getLong("created_at"),
                        rs.getLong("last_used"),
                        PlayerState.fromJson(rs.getString("state_json")),
                        rs.getDouble("balance"),
                        rs.getString("primary_group"),
                        rs.getString("groups_json"),
                        rs.getInt("active") == 1
                ),
                id(uuid)
        );
    }

    public @NonNull Promise<Void> save(@NonNull UUID uuid, @NonNull ProfileData data) {
        return db.executeUpdate(
                saveSql,
                id(uuid), data.name(), data.createdAt(), data.lastUsed(), data.state().toJson(),
                data.balance(), data.primaryGroup(), data.groupsJson(), data.active() ? 1 : 0
        ).map(rows -> null);
    }

    public @NonNull Promise<Void> setActive(@NonNull UUID uuid, @NonNull String name) {
        return db.executeUpdate(
                "UPDATE oum_profiles SET active = CASE WHEN name = ? THEN 1 ELSE 0 END WHERE uuid = ?",
                name, id(uuid)
        ).map(rows -> null);
    }

    public @NonNull Promise<Void> rename(@NonNull UUID uuid, @NonNull String oldName, @NonNull String newName) {
        return db.executeUpdate(
                "UPDATE oum_profiles SET name = ? WHERE uuid = ? AND name = ?",
                newName, id(uuid), oldName
        ).map(rows -> null);
    }

    @SuppressWarnings("UnusedReturnValue")
    public @NonNull Promise<Void> delete(@NonNull UUID uuid, @NonNull String name) {
        return db.executeUpdate(
                "DELETE FROM oum_profiles WHERE uuid = ? AND name = ?",
                id(uuid), name
        ).map(rows -> null);
    }

    public @NonNull Promise<Integer> pruneInactive(long cutoffMillis, @NonNull Set<UUID> excludeUuids) {
        if (excludeUuids.isEmpty()) {
            return db.executeUpdate("DELETE FROM oum_profiles WHERE last_used < ?", cutoffMillis);
        }
        StringBuilder sql = new StringBuilder("DELETE FROM oum_profiles WHERE last_used < ? AND uuid NOT IN (");
        Object[] params = new Object[1 + excludeUuids.size()];
        params[0] = cutoffMillis;
        int idx = 1;
        for (UUID u : excludeUuids) {
            if (idx > 1) sql.append(",");
            sql.append("?");
            params[idx++] = id(u);
        }
        sql.append(")");
        return db.executeUpdate(sql.toString(), params);
    }

    public @NonNull Promise<Boolean> exists(@NonNull UUID uuid, @NonNull String name) {
        return db.executeQuery(
                "SELECT 1 FROM oum_profiles WHERE uuid = ? AND name = ?",
                id(uuid), name
        ).map(rows -> !rows.isEmpty());
    }

    public void close() {
        db.close();
    }
}
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
import java.util.UUID;

public final class ProfileStorage {

    private final Database db;
    private final boolean mysql;

    public ProfileStorage(ProfileConfig.@NonNull StorageSection cfg) {
        this.mysql = cfg.type().equalsIgnoreCase("mysql");
        if (mysql) {
            this.db = Database.mysql(cfg.host(), cfg.port(), cfg.database(), cfg.username(), cfg.password());
            db.runMigrations(ProfileStorage.class, "migrations/mysql/V1__init.sql");
        } else {
            String filename = cfg.database().endsWith(".db") ? cfg.database() : cfg.database() + ".db";
            this.db = Database.sqlite(new File(OumLib.getDataFolder(), filename));
            db.runMigrations(ProfileStorage.class, "migrations/sqlite/V1__init.sql");
        }
    }

    public @NonNull Promise<List<ProfileData>> loadAll(@NonNull UUID uuid) {
        return db.executeQuery(
                "SELECT name, created_at, last_used, state_json, balance, primary_group, groups_json FROM oum_profiles WHERE uuid = ?",
                rs -> new ProfileData(
                        rs.getString("name"),
                        rs.getLong("created_at"),
                        rs.getLong("last_used"),
                        PlayerState.fromJson(rs.getString("state_json")),
                        rs.getDouble("balance"),
                        rs.getString("primary_group"),
                        rs.getString("groups_json")
                ),
                uuid.toString().toLowerCase(Locale.ROOT)
        );
    }

    public @NonNull Promise<Void> save(@NonNull UUID uuid, @NonNull ProfileData data) {
        String id = uuid.toString().toLowerCase(Locale.ROOT);
        String sql = mysql
                ? "INSERT INTO oum_profiles (uuid, name, created_at, last_used, state_json, balance, primary_group, groups_json) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                  "ON DUPLICATE KEY UPDATE last_used = VALUES(last_used), state_json = VALUES(state_json), " +
                  "balance = VALUES(balance), primary_group = VALUES(primary_group), groups_json = VALUES(groups_json)"
                : "INSERT INTO oum_profiles (uuid, name, created_at, last_used, state_json, balance, primary_group, groups_json) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                  "ON CONFLICT (uuid, name) DO UPDATE SET " +
                  "last_used = excluded.last_used, state_json = excluded.state_json, balance = excluded.balance, " +
                  "primary_group = excluded.primary_group, groups_json = excluded.groups_json";
        return db.executeUpdate(
                sql,
                id, data.name(), data.createdAt(), data.lastUsed(), data.state().toJson(),
                data.balance(), data.primaryGroup(), data.groupsJson()
        ).map(rows -> null);
    }

    @SuppressWarnings("UnusedReturnValue")
    public @NonNull Promise<Void> delete(@NonNull UUID uuid, @NonNull String name) {
        return db.executeUpdate(
                "DELETE FROM oum_profiles WHERE uuid = ? AND name = ?",
                uuid.toString().toLowerCase(Locale.ROOT), name
        ).map(rows -> null);
    }

    public @NonNull Promise<Boolean> exists(@NonNull UUID uuid, @NonNull String name) {
        return db.executeQuery(
                "SELECT 1 FROM oum_profiles WHERE uuid = ? AND name = ?",
                uuid.toString().toLowerCase(Locale.ROOT), name
        ).map(rows -> !rows.isEmpty());
    }

    public void close() {
        db.close();
    }
}
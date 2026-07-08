package dev.oum.profile.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.oum.oumlib.OumLib;
import dev.oum.profile.model.PlayerState;
import dev.oum.profile.model.ProfileData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class ProfileIO {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ProfileIO() {
    }

    public static boolean exportToFile(@NonNull ProfileData data, @NonNull File file) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            ExportWrapper wrapper = new ExportWrapper(
                    data.name(),
                    data.createdAt(),
                    data.lastUsed(),
                    data.state().toJson(),
                    data.balance(),
                    data.primaryGroup(),
                    data.groupsJson()
            );

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                GSON.toJson(wrapper, writer);
            }
            OumLib.logDebug("Exported profile '" + data.name() + "' to " + file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            OumLib.logError("Failed to export profile '" + data.name() + "' to file", e);
            return false;
        }
    }

    public static @Nullable ProfileData importFromFile(@NonNull File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            ExportWrapper wrapper = GSON.fromJson(reader, ExportWrapper.class);
            if (wrapper == null || wrapper.name == null || wrapper.stateJson == null) {
                OumLib.logError("Invalid profile export file: " + file.getName(), null);
                return null;
            }

            ProfileData data = new ProfileData(
                    wrapper.name,
                    wrapper.createdAt,
                    wrapper.lastUsed,
                    PlayerState.fromJson(wrapper.stateJson),
                    wrapper.balance,
                    wrapper.primaryGroup,
                    wrapper.groupsJson
            );
            OumLib.logDebug("Imported profile '" + data.name() + "' from " + file.getAbsolutePath());
            return data;
        } catch (Exception e) {
            OumLib.logError("Failed to import profile from file: " + file.getName(), e);
            return null;
        }
    }

    public static @NonNull File getExportsDir() {
        File dir = new File(OumLib.getDataFolder(), "exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private record ExportWrapper(
            String name,
            long createdAt,
            long lastUsed,
            String stateJson,
            double balance,
            String primaryGroup,
            String groupsJson
    ) {
    }
}

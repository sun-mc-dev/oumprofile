package dev.oum.profile.integration.mcmmo;

import dev.oum.profile.integration.SkillData;
import dev.oum.profile.integration.SkillHandler;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public interface McMMOHandler extends SkillHandler {

    McMMOHandler NOOP = new McMMOHandler() {
        @Override
        public @NonNull Map<String, SkillData> capture(@NonNull Player player) {
            return Map.of();
        }

        @Override
        public void restore(@NonNull Player player, @NonNull Map<String, SkillData> data) {
        }
    };
}

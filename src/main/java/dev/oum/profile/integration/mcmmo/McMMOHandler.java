package dev.oum.profile.integration.mcmmo;

import dev.oum.profile.integration.SkillData;
import org.bukkit.entity.Player;

import java.util.Map;

public interface McMMOHandler {
    Map<String, SkillData> capture(Player player);

    void restore(Player player, Map<String, SkillData> data);
}

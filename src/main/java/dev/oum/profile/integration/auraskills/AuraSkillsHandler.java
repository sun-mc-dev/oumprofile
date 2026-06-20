package dev.oum.profile.integration.auraskills;

import dev.oum.profile.integration.SkillData;
import org.bukkit.entity.Player;

import java.util.Map;

public interface AuraSkillsHandler {
    Map<String, SkillData> capture(Player player);

    void restore(Player player, Map<String, SkillData> data);
}

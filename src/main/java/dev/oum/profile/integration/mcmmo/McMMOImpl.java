package dev.oum.profile.integration.mcmmo;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import dev.oum.profile.integration.SkillData;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class McMMOImpl implements McMMOHandler {

    @Override
    public @NonNull Map<String, SkillData> capture(Player player) {
        Map<String, SkillData> data = new HashMap<>();
        for (PrimarySkillType skill : PrimarySkillType.values()) {
            try {
                var name = skill.name();
                if (ExperienceAPI.isValidSkillType(name)) {
                    int level = ExperienceAPI.getLevel(player, skill);
                    int xp = ExperienceAPI.getXP(player, name);
                    data.put(name, new SkillData(level, xp));
                }
            } catch (Throwable ignored) {
            }
        }
        return data;
    }

    @Override
    public void restore(Player player, Map<String, SkillData> data) {
        if (data == null) return;
        for (Map.Entry<String, SkillData> entry : data.entrySet()) {
            try {
                ExperienceAPI.setLevel(player, entry.getKey(), entry.getValue().level());
                ExperienceAPI.setXP(player, entry.getKey(), (int) entry.getValue().xp());
            } catch (Throwable ignored) {
            }
        }
    }
}

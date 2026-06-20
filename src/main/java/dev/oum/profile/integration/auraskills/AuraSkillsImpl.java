package dev.oum.profile.integration.auraskills;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.oum.profile.integration.SkillData;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class AuraSkillsImpl implements AuraSkillsHandler {

    @Override
    public @NonNull Map<String, SkillData> capture(Player player) {
        Map<String, SkillData> data = new HashMap<>();
        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
            if (user != null) {
                for (Skills skill : Skills.values()) {
                    try {
                        int level = user.getSkillLevel(skill);
                        double xp = user.getSkillXp(skill);
                        data.put(skill.name(), new SkillData(level, xp));
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return data;
    }

    @Override
    public void restore(Player player, Map<String, SkillData> data) {
        if (data == null) return;
        try {
            SkillsUser user = AuraSkillsApi.get().getUser(player.getUniqueId());
            if (user != null) {
                for (Map.Entry<String, SkillData> entry : data.entrySet()) {
                    try {
                        Skills skill = Skills.valueOf(entry.getKey().toUpperCase());
                        user.setSkillLevel(skill, entry.getValue().level());
                        user.setSkillXp(skill, entry.getValue().xp());
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }
}

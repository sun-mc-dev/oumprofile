package dev.oum.profile.integration;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public interface SkillHandler {

    @NonNull Map<String, SkillData> capture(@NonNull Player player);

    void restore(@NonNull Player player, @NonNull Map<String, SkillData> data);
}

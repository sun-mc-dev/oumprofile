package dev.oum.profile.integration;

import dev.oum.profile.integration.auraskills.AuraSkillsHandler;
import dev.oum.profile.integration.auraskills.AuraSkillsImpl;
import dev.oum.profile.integration.jobs.JobsHandler;
import dev.oum.profile.integration.jobs.JobsImpl;
import dev.oum.profile.integration.mcmmo.McMMOHandler;
import dev.oum.profile.integration.mcmmo.McMMOImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class IntegrationManager {

    private static final McMMOHandler mcmmo;
    private static final AuraSkillsHandler auraSkills;
    private static final JobsHandler jobs;

    static {
        mcmmo = Bukkit.getPluginManager().isPluginEnabled("mcMMO") ? new McMMOImpl() : new McMMOHandler() {
            @Override
            public Map<String, SkillData> capture(Player player) {
                return new HashMap<>();
            }

            @Override
            public void restore(Player player, Map<String, SkillData> data) {
            }
        };

        auraSkills = (Bukkit.getPluginManager().isPluginEnabled("AuraSkills")
                || Bukkit.getPluginManager().isPluginEnabled("AureliumSkills"))
                ? new AuraSkillsImpl() : new AuraSkillsHandler() {
            @Override
            public Map<String, SkillData> capture(Player player) {
                return new HashMap<>();
            }

            @Override
            public void restore(Player player, Map<String, SkillData> data) {
            }
        };

        jobs = Bukkit.getPluginManager().isPluginEnabled("Jobs") ? new JobsImpl() : new JobsHandler() {
            @Override
            public Map<String, SkillData> capture(Player player) {
                return new HashMap<>();
            }

            @Override
            public void restore(Player player, Map<String, SkillData> data) {
            }
        };
    }

    private IntegrationManager() {
    }

    public static McMMOHandler mcmmo() {
        return mcmmo;
    }

    public static AuraSkillsHandler auraSkills() {
        return auraSkills;
    }

    public static JobsHandler jobs() {
        return jobs;
    }
}

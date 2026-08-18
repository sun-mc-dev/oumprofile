package dev.oum.profile.integration;

import dev.oum.profile.integration.auraskills.AuraSkillsHandler;
import dev.oum.profile.integration.auraskills.AuraSkillsImpl;
import dev.oum.profile.integration.jobs.JobsHandler;
import dev.oum.profile.integration.jobs.JobsImpl;
import dev.oum.profile.integration.mcmmo.McMMOHandler;
import dev.oum.profile.integration.mcmmo.McMMOImpl;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

public final class IntegrationManager {

    private static final McMMOHandler mcmmo;
    private static final AuraSkillsHandler auraSkills;
    private static final JobsHandler jobs;

    static {
        mcmmo = Bukkit.getPluginManager().isPluginEnabled("mcMMO") ? new McMMOImpl() : McMMOHandler.NOOP;

        auraSkills = (Bukkit.getPluginManager().isPluginEnabled("AuraSkills")
                || Bukkit.getPluginManager().isPluginEnabled("AureliumSkills"))
                ? new AuraSkillsImpl() : AuraSkillsHandler.NOOP;

        jobs = Bukkit.getPluginManager().isPluginEnabled("Jobs") ? new JobsImpl() : JobsHandler.NOOP;
    }

    private IntegrationManager() {
    }

    public static @NonNull McMMOHandler mcmmo() {
        return mcmmo;
    }

    public static @NonNull AuraSkillsHandler auraSkills() {
        return auraSkills;
    }

    public static @NonNull JobsHandler jobs() {
        return jobs;
    }
}

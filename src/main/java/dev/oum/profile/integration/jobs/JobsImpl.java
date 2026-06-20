package dev.oum.profile.integration.jobs;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import dev.oum.profile.integration.SkillData;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class JobsImpl implements JobsHandler {

    @Override
    public @NonNull Map<String, SkillData> capture(Player player) {
        Map<String, SkillData> data = new HashMap<>();
        try {
            JobsPlayer jPlayer = Jobs.getPlayerManager().getJobsPlayer(player.getUniqueId());
            if (jPlayer != null) {
                for (JobProgression progression : jPlayer.getJobProgression()) {
                    try {
                        String name = progression.getJob().getName();
                        data.put(name, new SkillData(progression.getLevel(), progression.getExperience()));
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
        try {
            JobsPlayer jPlayer = Jobs.getPlayerManager().getJobsPlayer(player.getUniqueId());
            if (jPlayer != null) {
                Jobs.getPlayerManager().leaveAllJobs(jPlayer);

                if (data != null) {
                    for (Map.Entry<String, SkillData> entry : data.entrySet()) {
                        try {
                            Job job = Jobs.getJob(entry.getKey());
                            if (job != null) {
                                Jobs.getPlayerManager().joinJob(jPlayer, job);
                                JobProgression prog = jPlayer.getJobProgression(job);
                                if (prog != null) {
                                    prog.setLevel(entry.getValue().level());
                                    prog.setExperience(entry.getValue().xp());
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
                jPlayer.save();
            }
        } catch (Throwable ignored) {
        }
    }
}

package com.daqem.jobsplus.player.job;

import com.daqem.jobsplus.Constants;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.event.triggers.JobEvents;
import com.daqem.jobsplus.player.JobsPlayer;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.exp.ExpCollector;
import com.daqem.jobsplus.player.job.powerup.JobPowerupManager;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Job
{

    public static final Codec<Job> CODEC = RecordCodecBuilder.create(instance -> instance.group(ResourceLocation.CODEC.fieldOf("job_instance").forGetter(job -> job.getJobInstance().getLocation()), Codec.INT.fieldOf("level").forGetter(Job::getLevel), Codec.INT.fieldOf("experience").forGetter(Job::getExperience), Codec.DOUBLE.optionalFieldOf("experience_remainder", 0.0D).forGetter(Job::getExperienceRemainder), Codec.list(Powerup.CODEC).fieldOf("powerups").forGetter(job -> job.getPowerupManager().getAllPowerups())).apply(instance, (jobInstanceLocation, level, experience, experienceRemainder, powerups) -> new Job(null, jobInstanceLocation, level, experience, experienceRemainder, new ArrayList<>(powerups))));

    private final JobInstance jobInstance;
    private final JobPowerupManager powerupManager;
    private JobsPlayer player;
    private int level;
    private int experience;
    private double experienceRemainder;
    private final ExpCollector expCollector = new ExpCollector();

    public Job(JobsPlayer player, JobInstance jobInstance)
    {
        this(player, jobInstance, 0, 0, 0.0D, new ArrayList<>());
    }

    public Job(JobsPlayer player, JobInstance jobInstance, int level, int experience)
    {
        this(player, jobInstance, level, experience, 0.0D, new ArrayList<>());
    }

    public Job(JobsPlayer player, ResourceLocation jobInstanceLocation, int level, int experience, @NotNull List<Powerup> powerups)
    {
        this(player, JobManager.getInstance().getJobs().get(jobInstanceLocation), level, experience, 0.0D, powerups);
    }

    public Job(JobsPlayer player, JobInstance jobInstance, int level, int experience, @NotNull List<Powerup> powerups)
    {
        this(player, jobInstance, level, experience, 0.0D, powerups);
    }

    public Job(JobsPlayer player, ResourceLocation jobInstanceLocation, int level, int experience, double experienceRemainder, @NotNull List<Powerup> powerups)
    {
        this(player, JobManager.getInstance().getJobs().get(jobInstanceLocation), level, experience, experienceRemainder, powerups);
    }

    public Job(JobsPlayer player, JobInstance jobInstance, int level, int experience, double experienceRemainder, @NotNull List<Powerup> powerups)
    {
        this.player = player;
        this.jobInstance = jobInstance;
        this.powerupManager = new JobPowerupManager(new ArrayList<>(powerups));
        this.level = level;
        this.experience = experience;
        this.experienceRemainder = experienceRemainder;
    }

    public JobInstance getJobInstance()
    {
        return jobInstance;
    }

    public JobPowerupManager getPowerupManager()
    {
        return powerupManager;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getExperience()
    {
        return experience;
    }

    public double getExperienceRemainder()
    {
        return experienceRemainder;
    }

    public void setExperience(int experience, boolean triggerEvent)
    {
        int change = experience - this.experience;
        expCollector.addExp(change);
        this.experience = experience;
        checkForLevelUp();
        if (triggerEvent)
        {
            JobEvents.onJobExperience(player, this, change);
        }
    }

    public void addExperience(int experience)
    {
        addExperience((double) experience);
    }

    public void addExperience(double experience)
    {
        JobsPlus.debug("Adding {} experience to {}'s {} job.", experience, player.jobsplus$getName(), jobInstance.getName().getString());
        addAccumulatedExperience(experience);
        JobEvents.onJobExperience(player, this, experience);
    }

    public void addExperienceWithoutEvent(int experience)
    {
        addExperienceWithoutEvent((double) experience);
    }

    public void addExperienceWithoutEvent(double experience)
    {
        JobsPlus.debug("Adding {} experience to {}'s {} job without event.", experience, player.jobsplus$getName(), jobInstance.getName().getString());
        addAccumulatedExperience(experience);
    }

    private void addAccumulatedExperience(double experience)
    {
        if (experience <= 0.0D)
        {
            return;
        }

        double accumulatedExperience = this.experienceRemainder + experience;
        int wholeExperience = (int) Math.floor(accumulatedExperience + 1.0E-9D);
        this.experienceRemainder = accumulatedExperience - wholeExperience;
        if (wholeExperience > 0)
        {
            setExperience(getExperience() + wholeExperience, false);
        }
    }

    private void checkForLevelUp()
    {
        int experienceToLevelUp = getExperienceToLevelUp(level);
        if (experience >= experienceToLevelUp)
        {
            setLevel(level + 1);
            setExperience(experience - experienceToLevelUp, false);
            JobEvents.onJobLevelUp(player, this);
        }
    }

    // 다음 레벨업 총 추가 경험치
    public static int getExperienceToLevelUp(int level)
    {
        if (level == 0)
            return 0;
        return (int) (100 + level * level * 0.5791);
    }

    public void setPlayer(JobsPlayer player)
    {
        this.player = player;
    }

    public CompoundTag toNBT()
    {
        CompoundTag jobTag = new CompoundTag();

        jobTag.putString(Constants.JOB_INSTANCE_LOCATION, getJobInstance().getLocation().toString());
        jobTag.putInt(Constants.LEVEL, getLevel());
        jobTag.putInt(Constants.EXPERIENCE, getExperience());
        jobTag.putDouble(Constants.EXPERIENCE_REMAINDER, getExperienceRemainder());

        ListTag powerupsTag = new ListTag();

        for (Powerup powerup : powerupManager.getAllPowerups())
        {
            CompoundTag powerupTag = new CompoundTag();

            powerupTag.putString(Constants.POWERUP_LOCATION, powerup.getPowerupLocation().toString());
            powerupTag.putString(Constants.POWERUP_STATE, powerup.getState().name());

            powerupsTag.add(powerupTag);
        }

        jobTag.put(Constants.POWERUPS, powerupsTag);

        return jobTag;
    }

    public static Job fromNBT(JobsPlayer player, CompoundTag tag)
    {
        AtomicReference<Job> job = new AtomicReference<>();
        tag.getString(Constants.JOB_INSTANCE_LOCATION).ifPresent(jobLocation ->
        {
            tag.getInt(Constants.LEVEL).ifPresent(level ->
            {
                tag.getInt(Constants.EXPERIENCE).ifPresent(exp ->
                {
                    List<Powerup> powerups = new ArrayList<>();
                    tag.getList(Constants.POWERUPS).ifPresent(powerupsTag ->
                    {
                        for (Tag powerupTag : powerupsTag)
                        {
                            CompoundTag powerupNBT = (CompoundTag) powerupTag;
                            powerupNBT.getString(Constants.POWERUP_LOCATION).ifPresent(powerupLocation -> powerupNBT.getString(Constants.POWERUP_STATE).ifPresent(powerupState -> powerups.add(new Powerup(ResourceLocation.parse(powerupLocation), PowerupState.valueOf(powerupState)))));
                        }
                    });
                    double experienceRemainder = tag.getDouble(Constants.EXPERIENCE_REMAINDER).orElse(0.0D);
                    job.set(new Job(player, ResourceLocation.parse(jobLocation), level, exp, experienceRemainder, powerups));
                });
            });
        });
        return job.get();
    }

    public double getExperiencePercentage()
    {
        return (double) experience / (double) getExperienceToLevelUp(level) * 100;
    }

    public ExpCollector getExpCollector()
    {
        return expCollector;
    }

    public int getExperienceForNextLevel()
    {
        return getExperienceToLevelUp(level);
    }

    public static class Serializer
    {

        public static Job fromNetwork(FriendlyByteBuf friendlyByteBuf, JobsPlayer player)
        {
            ResourceLocation jobInstanceLocation = friendlyByteBuf.readResourceLocation();
            int level = friendlyByteBuf.readInt();
            int experience = friendlyByteBuf.readInt();
            double experienceRemainder = friendlyByteBuf.readDouble();
            int powerupCount = friendlyByteBuf.readVarInt();
            List<Powerup> powerups = new ArrayList<>();
            for (int i = 0; i < powerupCount; i++)
            {
                ResourceLocation powerupLocation = friendlyByteBuf.readResourceLocation();
                PowerupState state = friendlyByteBuf.readEnum(PowerupState.class);
                powerups.add(new Powerup(powerupLocation, state));
            }
            return new Job(player, jobInstanceLocation, level, experience, experienceRemainder, powerups);
        }

        public static void toNetwork(FriendlyByteBuf friendlyByteBuf, Job job)
        {
            friendlyByteBuf.writeResourceLocation(job.getJobInstance().getLocation());
            friendlyByteBuf.writeInt(job.getLevel());
            friendlyByteBuf.writeInt(job.getExperience());
            friendlyByteBuf.writeDouble(job.getExperienceRemainder());
            friendlyByteBuf.writeVarInt(job.getPowerupManager().getAllPowerups().size());
            for (Powerup powerup : job.getPowerupManager().getAllPowerups())
            {
                friendlyByteBuf.writeResourceLocation(powerup.getPowerupLocation());
                friendlyByteBuf.writeEnum(powerup.getState());
            }
        }

        public static List<Job> fromNBT(JobsServerPlayer player, CompoundTag compoundTag)
        {
            List<Job> jobs = new ArrayList<>();

            compoundTag.getList(Constants.JOBS).ifPresent(list ->
            {
                for (Tag jobTag : list)
                {
                    CompoundTag jobNBT = (CompoundTag) jobTag;
                    Job job = Job.fromNBT(player, jobNBT);
                    if (job != null)
                    {
                        jobs.add(job);
                    }
                }
            });

            return jobs;
        }

        public static ListTag toNBT(List<Job> jobs)
        {

            ListTag jobsListTag = new ListTag();
            for (Job job : jobs)
            {
                CompoundTag jobNBT = job.toNBT();
                jobsListTag.add(jobNBT);
            }

            return jobsListTag;
        }
    }
}

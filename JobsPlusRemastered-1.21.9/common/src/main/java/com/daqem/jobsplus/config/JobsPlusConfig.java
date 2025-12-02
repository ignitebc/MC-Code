package com.daqem.jobsplus.config;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.yamlconfig.api.config.ConfigExtension;
import com.daqem.yamlconfig.api.config.ConfigType;
import com.daqem.yamlconfig.api.config.IConfigBuilder;
import com.daqem.yamlconfig.api.config.entry.IConfigEntry;
import com.daqem.yamlconfig.impl.config.ConfigBuilder;

public class JobsPlusConfig
{

    public static final IConfigEntry<Boolean> enableDefaultJobs;
    public static final IConfigEntry<Integer> amountOfFreeJobs;
    public static final IConfigEntry<Integer> maxJobs;

    public static final IConfigEntry<Integer> coinsPerLevelUp;

    public static final IConfigEntry<Boolean> isDebug;

    static
    {
        IConfigBuilder config = new ConfigBuilder(JobsPlus.MOD_ID, "jobsplus-common", ConfigExtension.YAML, ConfigType.COMMON);

        config.push("jobs");
        enableDefaultJobs = config.defineBoolean("enable_default_jobs", true).withComments("true인 경우 기본 작업이 활성화됩니다. 경고: false로 설정하면 이러한 작업에 대한 모든 통계가 지워집니다.");
        amountOfFreeJobs = config.defineInteger("amount_of_free_jobs", 2, 0, Integer.MAX_VALUE).withComments("플레이어가 가질 수 있는 무료 작업의 양");

        // 직업갯수 2개로 조정 (원본)
        // maxJobs = config.defineInteger("max_jobs", Integer.MAX_VALUE, 0, Integer.MAX_VALUE).withComments("the maximum amount of jobs a player can have");
        maxJobs = config.defineInteger("max_jobs", 2, 0, Integer.MAX_VALUE).withComments("플레이어가 가질 수 있는 최대 직업 수");
        config.push("coins");

        // coinsPerLevelUp = config.defineInteger("coins_per_level_up", 1, 0, Integer.MAX_VALUE).withComments("the amount of coins a player gets when they level up a job");
        // 레벨업당 코인 얻는양 3개로 조정
        coinsPerLevelUp = config.defineInteger("coins_per_level_up", 3, 0, Integer.MAX_VALUE).withComments("플레이어가 레벨업 시 얻는 직업 코인의 양");
        config.pop();
        config.pop();

        config.push("debug");
        isDebug = config.defineBoolean("is_debug", false).withComments("if true, debug mode is enabled");
        config.pop();

        config.build();
    }

    public static void init()
    {
    }
}

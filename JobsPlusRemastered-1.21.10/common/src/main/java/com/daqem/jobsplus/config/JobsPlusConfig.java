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

        // 정책: 기본 무료 직업은 1개
        amountOfFreeJobs = config.defineInteger("amount_of_free_jobs", 1, 0, Integer.MAX_VALUE).withComments("플레이어가 가질 수 있는 무료 작업의 양");

        // 정책: 최종 최대 직업 수 상한은 7개(티켓 사용해도 초과 불가)
        maxJobs = config.defineInteger("max_jobs", 7, 0, Integer.MAX_VALUE).withComments("플레이어가 가질 수 있는 최대 직업 수");

        config.push("coins");
        coinsPerLevelUp = config.defineInteger("coins_per_level_up", 5, 0, Integer.MAX_VALUE).withComments("플레이어가 레벨업 시 얻는 직업 코인의 양");
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

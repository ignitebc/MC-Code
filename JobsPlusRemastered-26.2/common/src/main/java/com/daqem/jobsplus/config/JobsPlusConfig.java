package com.daqem.jobsplus.config;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.yamlconfig.api.config.ConfigExtension;
import com.daqem.yamlconfig.api.config.ConfigType;
import com.daqem.yamlconfig.api.config.IConfigBuilder;
import com.daqem.yamlconfig.api.config.entry.IConfigEntry;
import com.daqem.yamlconfig.impl.config.ConfigBuilder;

public class JobsPlusConfig
{

    public static final int MAX_JOB_COUNT = 8;
    public static final int COINS_PER_LEVEL_UP = 20;

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

        // 정책: 기본 무료 직업은 2개
        amountOfFreeJobs = config.defineInteger("amount_of_free_jobs", 2, 0, Integer.MAX_VALUE).withComments("플레이어가 가질 수 있는 무료 작업의 양");

        // 정책: 최종 최대 직업 수는 8개로 고정한다. 기존 설정값도 실행 시 8로 교정된다.
        maxJobs = config.defineInteger("max_jobs", MAX_JOB_COUNT, MAX_JOB_COUNT, MAX_JOB_COUNT)
                .withComments("직업선택권으로 확장할 수 있는 최대 직업 수");

        config.push("coins");
        // 정책: 레벨업당 20코인 고정. 전 직업 공통이며 직업별로 다르게 주지 않는다.
        // 직업 최대 레벨은 Job.MAX_JOB_LEVEL(200)이며, 그 이후에는 레벨업과 코인 지급이 없다.
        // Lv95 까지 94회 레벨업으로 1,880코인이 모여 여섯 계열(1,500)과 일곱 계열(1,690)을 모두 해방할 수 있다.
        coinsPerLevelUp = config.defineInteger(
                "coins_per_level_up",
                COINS_PER_LEVEL_UP,
                COINS_PER_LEVEL_UP,
                COINS_PER_LEVEL_UP
        ).withComments("플레이어가 레벨업 시 얻는 직업코인의 양");
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

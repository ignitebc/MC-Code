package com.daqem.jobsplus;

import com.daqem.jobsplus.integration.arc.holder.holders.job.JobManager;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupManager;
import dev.architectury.injectables.annotations.ExpectPlatform;

public class JobsPlusExpectPlatform
{

    @ExpectPlatform
    public static JobManager getJobManager()
    {
        // 오류를 발생시키면 런타임에 콘텐츠가 교체되어야 합니다.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static PowerupManager getPowerupManager()
    {
        // 오류를 발생시키면 런타임에 콘텐츠가 교체되어야 합니다.
        throw new AssertionError();
    }
}

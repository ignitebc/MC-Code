package com.daqem.jobsplus.integration.arc.data.type;

import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.data.type.IActionDataType;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.resources.Identifier;

public interface JobsPlusActionDataType<T> extends ActionDataType<T>
{

    IActionDataType<Double> JOB_EXP = register(JobsPlus.getId("job_exp"));
    IActionDataType<Job> ONLY_FOR_JOB = register(JobsPlus.getId("only_for_job"));

    static <T> IActionDataType<T> register(Identifier location)
    {
        return () -> location;
    }
}

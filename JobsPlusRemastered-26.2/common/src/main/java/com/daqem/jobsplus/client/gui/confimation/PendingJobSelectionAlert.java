package com.daqem.jobsplus.client.gui.confimation;

import com.daqem.jobsplus.player.job.Job;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 직업 선택 직후 서버가 화면을 갱신(ClientboundOpenJobsScreenPacket)하면서
 * 열려 있는 화면을 교체하기 때문에, 완료 알림은 갱신된 화면 위에서 띄워야 한다.
 * 이를 위해 선택한 직업을 잠시 보관하는 클라이언트 전용 상태.
 * <p>
 * 서버가 요청을 거절할 수도 있으므로, 갱신 패킷의 직업 목록에서 해당 직업이
 * 실제로 선택(레벨 1 이상)됐는지 확인한 뒤에만 알림 문구를 돌려준다.
 */
public class PendingJobSelectionAlert
{

    @Nullable
    private static Identifier pendingJobLocation;
    @Nullable
    private static String pendingJobName;

    private PendingJobSelectionAlert()
    {
    }

    public static void set(Identifier jobLocation, String jobName)
    {
        pendingJobLocation = jobLocation;
        pendingJobName = jobName;
    }

    /**
     * 갱신된 직업 목록에서 보류 중인 직업이 실제로 선택됐으면 직업명을 돌려주고,
     * 아니면 {@code null}을 돌려준다. 어느 쪽이든 보류 상태는 비운다.
     */
    @Nullable
    public static String consumeIfSelected(List<Job> jobs)
    {
        Identifier jobLocation = pendingJobLocation;
        String jobName = pendingJobName;
        pendingJobLocation = null;
        pendingJobName = null;

        if (jobLocation == null || jobName == null || jobs == null)
        {
            return null;
        }

        for (Job job : jobs)
        {
            boolean isPendingJob = job.getJobInstance() != null
                    && jobLocation.equals(job.getJobInstance().getLocation());
            if (isPendingJob && job.getLevel() >= 1)
            {
                return jobName;
            }
        }
        return null;
    }
}

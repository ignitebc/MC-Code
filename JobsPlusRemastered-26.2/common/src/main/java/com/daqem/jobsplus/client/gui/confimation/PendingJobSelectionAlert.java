package com.daqem.jobsplus.client.gui.confimation;

import org.jetbrains.annotations.Nullable;

/**
 * 직업 선택 직후 서버가 화면을 갱신(ClientboundOpenJobsScreenPacket)하면서
 * 열려 있는 화면을 교체하기 때문에, 완료 알림은 갱신된 화면 위에서 띄워야 한다.
 * 이를 위해 선택한 직업명을 잠시 보관하는 클라이언트 전용 상태.
 */
public class PendingJobSelectionAlert
{

    @Nullable
    private static String pendingJobName;

    private PendingJobSelectionAlert()
    {
    }

    public static void set(String jobName)
    {
        pendingJobName = jobName;
    }

    @Nullable
    public static String consume()
    {
        String jobName = pendingJobName;
        pendingJobName = null;
        return jobName;
    }
}

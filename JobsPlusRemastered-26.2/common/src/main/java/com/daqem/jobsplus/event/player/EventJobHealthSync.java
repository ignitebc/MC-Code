package com.daqem.jobsplus.event.player;

import com.daqem.jobsplus.player.JobHealthSync;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.event.events.common.PlayerEvent;

/**
 * 접속할 때 보유 직업에 맞춰 최대 체력을 다시 맞춘다.
 * <p>
 * 저장된 직업 기록이 없는 첫 접속자는 불러오기 단계에서 체력을 맞추지 않는다. 직업을 고르기 전 체력이
 * 첫 접속부터 적용되도록 접속 시점에 한 번 더 맞춘다.
 */
public final class EventJobHealthSync
{
    private EventJobHealthSync()
    {
    }

    public static void registerEvent()
    {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player instanceof JobsServerPlayer jobsServerPlayer)
            {
                JobHealthSync.sync(jobsServerPlayer);
            }
        });
    }
}

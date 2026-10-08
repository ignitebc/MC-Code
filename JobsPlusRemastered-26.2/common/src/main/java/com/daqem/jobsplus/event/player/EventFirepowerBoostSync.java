package com.daqem.jobsplus.event.player;

import com.daqem.jobsplus.player.job.powerup.FirepowerBoost;
import dev.architectury.event.events.common.PlayerEvent;

/**
 * 접속할 때 사냥꾼 화력 증강의 장탄 보너스를 클라이언트에 보낸다.
 * <p>
 * TACZ 클라이언트가 이 값을 알아야 늘어난 장탄 수까지 재장전을 허용하고 탄약 수를 맞게 표시한다.
 * 이후 스킬 구매·ON/OFF·직업 변경은 직업 데이터를 갱신할 때 함께 보낸다.
 */
public final class EventFirepowerBoostSync
{
    private EventFirepowerBoostSync()
    {
    }

    public static void registerEvent()
    {
        PlayerEvent.PLAYER_JOIN.register(FirepowerBoost::sync);
    }
}

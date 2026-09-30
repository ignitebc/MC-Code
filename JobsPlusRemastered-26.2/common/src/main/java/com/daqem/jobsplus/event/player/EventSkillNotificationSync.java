package com.daqem.jobsplus.event.player;

import com.daqem.jobsplus.player.SkillNotificationSettings;
import dev.architectury.event.events.common.PlayerEvent;

/**
 * 접속할 때 저장된 스킬 알림 설정을 클라이언트에 보낸다.
 * <p>
 * 클라이언트는 이 값을 받아야 스킬 화면 버튼에 현재 상태를 표시하고,
 * 확인 창에서 끌지 켤지를 올바르게 묻는다.
 */
public final class EventSkillNotificationSync
{
    private EventSkillNotificationSync()
    {
    }

    public static void registerEvent()
    {
        PlayerEvent.PLAYER_JOIN.register(SkillNotificationSettings::sync);
    }
}

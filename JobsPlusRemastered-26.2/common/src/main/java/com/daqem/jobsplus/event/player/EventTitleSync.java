package com.daqem.jobsplus.event.player;

import com.daqem.jobsplus.player.title.TitleManager;
import dev.architectury.event.events.common.PlayerEvent;

/**
 * 접속할 때 장착 칭호의 팀 소속을 맞추고 칭호 탭 정보를 보낸다.
 * <p>
 * 닉네임을 바꾸고 들어온 보유자도 이때 새 이름으로 배지가 옮겨진다.
 */
public final class EventTitleSync
{
    private EventTitleSync()
    {
    }

    public static void registerEvent()
    {
        PlayerEvent.PLAYER_JOIN.register(TitleManager::onPlayerJoin);
    }
}

package com.daqem.jobsplus.event.player;

import com.daqem.jobsplus.player.title.TitleManager;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerPlayer;

/**
 * 장비로 얻는 칭호(하늘의지배자, 영원한정점)의 조건을 1초마다 확인한다.
 * <p>
 * 겉날개는 엔드 배 액자와 상점에서 얻고, +10강은 강화 모드에서 일어나므로 획득 지점마다 연결하는 대신
 * 플레이어가 가진 장비를 직접 살펴본다.
 */
public final class EventTitleEquipmentCheck
{
    private static final long CHECK_INTERVAL_TICKS = 20L;

    private EventTitleEquipmentCheck()
    {
    }

    public static void registerEvent()
    {
        TickEvent.PLAYER_POST.register(player -> {
            if (!(player instanceof ServerPlayer serverPlayer))
            {
                return;
            }
            if (serverPlayer.level().getGameTime() % CHECK_INTERVAL_TICKS != 0L)
            {
                return;
            }
            TitleManager.onEquipmentCheck(serverPlayer);
        });
    }
}

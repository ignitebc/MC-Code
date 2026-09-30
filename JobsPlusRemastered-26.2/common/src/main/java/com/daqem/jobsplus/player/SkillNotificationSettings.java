package com.daqem.jobsplus.player;

import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.jobsplus.networking.s2c.ClientboundSkillNotificationsPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;

/**
 * 스킬 발동 채팅 알림 설정을 바꾸고 클라이언트에 현재 값을 알린다.
 * <p>
 * 값은 ArcLib 플레이어 데이터에 저장된다. 알림을 보내는 쪽이 ArcLib이므로
 * JobsPlus에 따로 두면 두 값이 어긋날 수 있다.
 */
public final class SkillNotificationSettings
{

    private SkillNotificationSettings()
    {
    }

    public static void set(ServerPlayer player, boolean enabled)
    {
        if (player instanceof ArcServerPlayer arcServerPlayer)
        {
            arcServerPlayer.arc$setSkillNotificationsEnabled(enabled);
        }
        sync(player);
    }

    public static void sync(ServerPlayer player)
    {
        if (player instanceof ArcServerPlayer arcServerPlayer)
        {
            boolean enabled = arcServerPlayer.arc$isSkillNotificationsEnabled();
            NetworkManager.sendToPlayer(player, new ClientboundSkillNotificationsPacket(enabled));
        }
    }
}

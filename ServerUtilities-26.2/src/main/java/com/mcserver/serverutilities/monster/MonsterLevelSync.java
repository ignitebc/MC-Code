package com.mcserver.serverutilities.monster;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * 몬스터 레벨을 클라이언트에 알린다.
 *
 * <p>레벨은 장비 추첨 때 한 번 정해지고 바뀌지 않으므로, 플레이어가 몬스터를 추적하기 시작할 때만 보낸다.
 * Fabric은 이 이벤트를 엔티티 생성 패킷을 보낸 뒤에 부르므로 클라이언트에는 이미 해당 개체가 있다.
 */
public final class MonsterLevelSync {
    private MonsterLevelSync() { }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(MonsterLevelPayload.TYPE, MonsterLevelPayload.STREAM_CODEC);
        EntityTrackingEvents.START_TRACKING.register(MonsterLevelSync::onStartTracking);
    }

    private static void onStartTracking(Entity entity, ServerPlayer player) {
        if (!(entity instanceof MonsterEquipmentAccess state)) return;
        int level = state.serverutilities$monsterLevel();
        if (!MonsterLevel.isVisible(level)) return;
        // 이 모듈이 없는 클라이언트는 받을 수 없으므로 보내지 않는다.
        if (!ServerPlayNetworking.canSend(player, MonsterLevelPayload.TYPE)) return;
        ServerPlayNetworking.send(player, new MonsterLevelPayload(entity.getId(), level));
    }
}

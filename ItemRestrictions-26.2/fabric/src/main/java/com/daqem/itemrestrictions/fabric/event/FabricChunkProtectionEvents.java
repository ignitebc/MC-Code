package com.daqem.itemrestrictions.fabric.event;

import com.daqem.itemrestrictions.chunk.ChunkProtection;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

/**
 * Fabric API 이벤트 채널에 대한 청크 보호 등록.
 *
 * <p>Architectury의 BlockEvent.BREAK는 플레이어가 직접 캐는 경로에서만 발화된다.
 * FallingTree의 일괄 벌목처럼 Fabric의 PlayerBlockBreakEvents.BEFORE를 직접 호출하는
 * 모드는 그 검사를 지나치므로, 같은 보호 판정을 이 채널에도 걸어 우회를 막는다.
 */
public final class FabricChunkProtectionEvents {

    private FabricChunkProtectionEvents() {
    }

    public static void registerEvents() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (ChunkProtection.denyAndNotify(level, pos, player)) {
                return false;
            }
            return true;
        });
    }
}

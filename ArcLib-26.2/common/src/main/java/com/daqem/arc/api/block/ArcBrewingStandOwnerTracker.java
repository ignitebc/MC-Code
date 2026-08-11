package com.daqem.arc.api.block;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 양조대가 슬롯별 물약 주인과 마지막 상호작용 플레이어를 스스로 기억하게 하는 인터페이스.
 * <p>
 * 이전에는 전역 Map이 ServerPlayer와 블록 엔티티를 강참조로 붙들어 청크 언로드나
 * 로그아웃 후에도 GC되지 않았다. 상태를 블록 엔티티 자신에게 UUID로만 두면
 * 블록 엔티티 생명주기에 따라 함께 해제되고, 차원이 달라도 좌표가 섞이지 않는다.
 * 보상 지급 시점에는 UUID로 현재 접속 중인 플레이어만 다시 찾는다.
 */
public interface ArcBrewingStandOwnerTracker {

    void arc$setLastPlayerToInteract(UUID playerUuid);

    @Nullable
    UUID arc$getLastPlayerToInteract();

    @Nullable
    UUID arc$getPotionSlotOwner(int slot);

    void arc$setPotionSlotOwner(int slot, @Nullable UUID ownerUuid);
}

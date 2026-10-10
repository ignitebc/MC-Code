package com.tacz.guns.api.client.gameplay;

import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 클라이언트 총기 조작자
 * 지금은 LocalPlayer에만 쓴다
 */
@Environment(EnvType.CLIENT)
public interface IClientPlayerGunOperator {
    /**
     * LocalPlayer가 Mixin으로 이 인터페이스를 구현한다
     */
    static IClientPlayerGunOperator fromLocalPlayer(LocalPlayer player) {
        return (IClientPlayerGunOperator) player;
    }

    /**
     * 플레이어가 발사할 수 있는지 확인하고 클라이언트 발사 로직을 실행한다.
     *
     * @return 발사 결과
     */
    ShootResult shoot();

    /**
     * 클라이언트 총기 교체 로직을 실행한다.
     */
    void draw(ItemStack lastItem);

    /**
     * 클라이언트 수동 재장전
     */
    void bolt();

    /**
     * 클라이언트 재장전
     */
    void reload();

    /**
     * 클라이언트 살펴보기
     */
    void inspect();

    /**
     * 클라이언트 발사 모드 전환
     */
    void fireSelect();

    /**
     * 클라이언트 조준
     */
    void aim(boolean isAim);

    /**
     * 클라이언트 엎드리기
     */
    void crawl(boolean isCrawl);

    /**
     * 클라이언트 근접 공격(총검)
     */
    void melee();

    /**
     * 클라이언트가 조준 중인지
     */
    boolean isAim();

    /**
     * 엎드려 있는지
     */
    boolean isCrawl();

    LocalPlayerDataHolder getDataHolder();

    /**
     * 클라이언트 조준 진행도
     *
     * @return 0~1. 1이면 조준이 100% 진행된 상태
     */
    float getClientAimingProgress(float partialTicks);

    /**
     * 클라이언트 사격 대기 시간
     */
    long getClientShootCoolDown();

    boolean isReadyToDraw();

    void resetDraw();

    boolean chargeShoot(boolean isCharge);

    float getChargeProgress();

    boolean isCharging();
}

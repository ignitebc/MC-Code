package com.tacz.guns.api.entity;

import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface IGunOperator {
    /**
     * LivingEntity가 Mixin으로 이 인터페이스를 구현한다
     */
    static IGunOperator fromLivingEntity(LivingEntity entity) {
        return (IGunOperator) entity;
    }

    /**
     * 서버에서 동기화한 사격 대기 시간을 얻는다
     */
    long getSynShootCoolDown();

    /**
     * 서버에서 동기화한 근접 공격(주로 총검) 대기 시간을 얻는다
     */
    long getSynMeleeCoolDown();

    /**
     * 서버에서 동기화한 총기 교체 대기 시간을 얻는다
     */
    long getSynDrawCoolDown();

    /**
     * 서버에서 동기화한 수동 재장전 대기 시간을 얻는다
     */
    boolean getSynIsBolting();

    /**
     * 서버에서 동기화한 재장전 상태를 얻는다
     */
    ReloadState getSynReloadState();

    /**
     * 서버에서 동기화한 조준 진행도를 얻는다
     */
    float getSynAimingProgress();

    /**
     * 이 엔티티가 조준 중인지 얻는다.
     * getSynAimingProgress() > 0과 같지 않다는 점에 주의한다.
     * 플레이어가 조준 중이면 조준 진행도가 늘고, 아니면 줄어든다.
     */
    boolean getSynIsAiming();

    /**
     * 플레이어가 총을 들고 달린 시간을 얻는다.
     * 총기 데이터의 sprintTime보다 크지 않고 0보다 작지 않다.
     */
    float getSynSprintTime();

    /**
     * 재장전 대기, 발사 대기 등 총기 조작 데이터를 초기화한다.
     */
    void initialData();

    /**
     * 서버 총기 교체 로직
     */
    void draw(Supplier<ItemStack> itemStackSupplier);

    /**
     * 서버 노리쇠 당기기 로직
     */
    void bolt();

    /**
     * 서버 재장전 로직
     */
    void reload();

    /**
     * 서버 재장전 취소 로직
     */
    void cancelReload();

    /**
     * 서버 발사 모드 전환 로직
     */
    void fireSelect();

    /**
     * 서버 조준경 배율 조정 로직
     */
    void zoom();

    /**
     * 서버 근접 공격(총검) 로직
     */
    void melee();

    /**
     * 엔티티 위치에서 지정한 방향으로 쏜다
     *
     * @param pitch 발사 방향의 피치 각(xRot)
     * @param yaw   발사 방향의 요 각(yRot)
     * @return 이번 사격 결과
     */
    ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw);

    /**
     * 엔티티 위치에서 지정한 방향으로 쏜다. 대기 시간 계산에는 지정한 timestamp를 쓴다
     *
     * @param pitch     발사 방향의 피치 각(xRot)
     * @param yaw       발사 방향의 요 각(yRot)
     * @param timestamp 지정한 시각. base timestamp 기준의 상대 시각이다
     * @return 이번 사격 결과
     */
    ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp);

    default ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress) {
        return shoot(pitch, yaw, timestamp);
    }

    /**
     * 서버: 이 조작자가 탄약 수의 영향을 받는지
     *
     * @return false면 발사할 때 플레이어 인벤토리와 총기 안의 탄약을 모두 확인하지 않는다
     */
    boolean needCheckAmmo();

    /**
     * 서버: 발사할 때 탄약을 소모하는지
     *
     * @return false면 발사해도 총기 탄약을 소모하지 않는다
     */
    boolean consumesAmmoOrNot();

    /**
     * 상황에 따라 플레이어가 있어야 할 질주 상태를 돌려준다. 플레이어가 질주 상태를 바꿀 때 호출된다.
     * 이 로직은 클라이언트와 정확히 대응해야 하며, 다르면 클라이언트 표시와 서버 상태가 어긋난다.
     * (예: 클라이언트에서는 질주하는 것처럼 보이지만 서버에서는 실제로 질주하지 않음)
     *
     * @see com.tacz.guns.client.gameplay.LocalPlayerSprint#getProcessedSprintStatus
     */
    boolean getProcessedSprintStatus(boolean sprint);

    /**
     * 서버: 조준 로직을 적용한다
     *
     * @param isAim 조준 여부
     */
    void aim(boolean isAim);

    /**
     * 서버: 엎드리기 로직을 적용한다
     */
    void crawl(boolean isCrawl);

    /**
     * 총기의 부착물 속성 보정값을 갱신한다
     * <p>
     * 부착물이 바꾼 속성 값을 엔티티에 캐시해 잦은 계산을 피하고 성능을 높인다
     *
     * @param cacheProperty 갱신한 부착물 속성 보정값
     */
    void updateCacheProperty(AttachmentCacheProperty cacheProperty);

    /**
     * 부착물 속성 보정값 캐시를 얻는다
     *
     * @return 대부분의 경우 null일 수 없다
     */
    @Nullable
    AttachmentCacheProperty getCacheProperty();

    ShooterDataHolder getDataHolder();

    /**
     * 예광탄 카운터를 1 늘리고, 넘겨받은 예광탄 간격으로 현재 탄이 예광탄인지 계산한다.
     *
     * @param tracerCountInterval 예광탄 간격
     * @return 예광탄인지
     */
    boolean nextBulletIsTracer(int tracerCountInterval);
}

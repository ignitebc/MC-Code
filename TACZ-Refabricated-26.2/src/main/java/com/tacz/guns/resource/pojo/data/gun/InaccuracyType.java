package com.tacz.guns.resource.pojo.data.gun;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.util.HitboxHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public enum InaccuracyType {
    /**
     * 제자리에 서 있음
     */
    @SerializedName("stand")
    STAND,
    /**
     * 이동
     */
    @SerializedName("move")
    MOVE,
    /**
     * 웅크리기. 다른 FPS 게임의 앉아 쏴로 본다
     */
    @SerializedName("sneak")
    SNEAK,
    /**
     * 엎드리기. 바닐라에서도 실제로 엎드릴 수 있다
     */
    @SerializedName("lie")
    LIE,
    /**
     * 조준 상태
     */
    @SerializedName("aim")
    AIM,
    /**
     * 질주 직후 사격. 총을 든 채 질주하면 쏠 수 없으므로 "질주를 멈춘 뒤 {@link #RUN_PENALTY_MS} 안에 사격"을 판정한다
     */
    @SerializedName("run")
    RUN,
    /**
     * 비행: 겉날개 활공 또는 크리에이티브 모드 비행
     */
    @SerializedName("fly")
    FLY;

    /**
     * 질주를 멈춘 뒤 얼마 안에 사격해야 RUN으로 치는지. 단위는 밀리초다
     */
    public static final long RUN_PENALTY_MS = 1000;
    /**
     * 총기 데이터에 run / fly가 없으면 그 총의 stand 수치에 배수를 곱해 추정한다
     */
    public static final float RUN_STAND_RATIO = 2f;
    public static final float FLY_STAND_RATIO = 4f;

    /**
     * 현재 부정확도 상태를 가져온다
     *
     * @param livingEntity 사수
     * @return 부정확도 상황
     */
    public static InaccuracyType getInaccuracyType(LivingEntity livingEntity) {
        float aimingProgress = IGunOperator.fromLivingEntity(livingEntity).getSynAimingProgress();
        // 조준이 가장 우선한다
        if (aimingProgress == 1.0f) {
            return InaccuracyType.AIM;
        }
        if (isFly(livingEntity)) {
            return InaccuracyType.FLY;
        }
        // MOJANG의 묘한 설계로, 엎드린 자세 이름이 SWIMMING이다
        if (!livingEntity.isSwimming() && livingEntity.getPose() == Pose.SWIMMING) {
            return InaccuracyType.LIE;
        }
        if (livingEntity.getPose() == Pose.CROUCHING) {
            return InaccuracyType.SNEAK;
        }
        if (isRun(livingEntity)) {
            return InaccuracyType.RUN;
        }
        if (isMove(livingEntity)) {
            return InaccuracyType.MOVE;
        }
        return InaccuracyType.STAND;
    }

    public static Map<InaccuracyType, Float> getDefaultInaccuracy() {
        Map<InaccuracyType, Float> inaccuracy = Maps.newHashMap();
        inaccuracy.put(InaccuracyType.STAND, 5f);
        inaccuracy.put(InaccuracyType.MOVE, 5.75f);
        inaccuracy.put(InaccuracyType.SNEAK, 3.5f);
        inaccuracy.put(InaccuracyType.LIE, 2.5f);
        inaccuracy.put(InaccuracyType.AIM, 0.15f);
        inaccuracy.put(InaccuracyType.RUN, 5f * RUN_STAND_RATIO);
        inaccuracy.put(InaccuracyType.FLY, 5f * FLY_STAND_RATIO);
        return inaccuracy;
    }

    private static boolean isFly(LivingEntity livingEntity) {
        if (livingEntity.isFallFlying()) {
            return true;
        }
        return livingEntity instanceof Player player && player.getAbilities().flying;
    }

    private static boolean isRun(LivingEntity livingEntity) {
        long lastSprintTimestamp = IGunOperator.fromLivingEntity(livingEntity).getDataHolder().lastSprintTimestamp;
        if (lastSprintTimestamp == -1) {
            return false;
        }
        return System.currentTimeMillis() - lastSprintTimestamp < RUN_PENALTY_MS;
    }

    private static boolean isMove(LivingEntity livingEntity) {
        // 26.2 맞춤: 원본 1.21.1은 Math.abs(walkDist - walkDistO), 곧 "이번 tick 수평 이동량 * 0.6"을 썼다.
        // 이식할 때 walkAnimation.speed()로 바꿨는데, 둘은 차원이 다르다:
        //   walkDist 증가량        = 이동량 * 0.6
        //   walkAnimation.speed  = min(이동량 * 4.0, 1.0)   (LivingEntity#updateWalkAnimation 참고)
        // 후자는 전자의 약 6.7배라 0.05 문턱값이 크게 부풀려진다 — 아주 느리게 움직여도 "이동 중"으로 판정된다.
        //
        // 26.2에서 walkDist는 moveDist로 이름이 바뀌었지만 moveDistO는 <b>남지 않아</b>(javap 확인)
        // 증가량을 바로 계산할 수 없다. "이번 tick 수평 이동량"과 같은 속도 값을 쓰고 0.6을 다시 곱해 차원을 되돌린다.
        // (플레이어 분기는 아래에서 실제 속도로 덮어쓰므로 이 줄은 주로 플레이어가 아닌 엔티티에 영향을 준다.)
        double distance = livingEntity.getDeltaMovement().horizontalDistance() * 0.6;
        if (livingEntity instanceof Player player) {
            distance = HitboxHelper.getPlayerVelocity(player).length();
        }
        return distance > 0.05f;
    }

    public boolean isAim() {
        return this == AIM;
    }
}

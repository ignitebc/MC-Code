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
     * 站立不动
     */
    @SerializedName("stand")
    STAND,
    /**
     * 移动
     */
    @SerializedName("move")
    MOVE,
    /**
     * 潜行，认为是其他 FPS 游戏中的半蹲
     */
    @SerializedName("sneak")
    SNEAK,
    /**
     * 趴下，原版确实可以趴下
     */
    @SerializedName("lie")
    LIE,
    /**
     * 瞄准状态
     */
    @SerializedName("aim")
    AIM,
    /**
     * 疾跑后立即射击。持枪疾跑时无法开火，所以判定的是“停止疾跑后 {@link #RUN_PENALTY_MS} 内开火”
     */
    @SerializedName("run")
    RUN,
    /**
     * 飞行：鞘翅滑翔或创造模式飞行
     */
    @SerializedName("fly")
    FLY;

    /**
     * 停止疾跑后多久以内开火仍算作 RUN，单位毫秒
     */
    public static final long RUN_PENALTY_MS = 1000;
    /**
     * 枪械数据没有写 run / fly 时，按该枪 stand 数值的倍数推算
     */
    public static final float RUN_STAND_RATIO = 2f;
    public static final float FLY_STAND_RATIO = 4f;

    /**
     * 获取当前的不准确度状态
     *
     * @param livingEntity 射手
     * @return 不准度情况
     */
    public static InaccuracyType getInaccuracyType(LivingEntity livingEntity) {
        float aimingProgress = IGunOperator.fromLivingEntity(livingEntity).getSynAimingProgress();
        // 瞄准优先级最高
        if (aimingProgress == 1.0f) {
            return InaccuracyType.AIM;
        }
        if (isFly(livingEntity)) {
            return InaccuracyType.FLY;
        }
        // MOJANG 的奇妙设计，趴下的姿势名称是 SWIMMING
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
        // 26.2 对齐：上游 1.21.1 用的是 Math.abs(walkDist - walkDistO)，即“本 tick 的水平位移 * 0.6”。
        // 移植时换成了 walkAnimation.speed()，两者量纲不同：
        //   walkDist 增量        = 位移 * 0.6
        //   walkAnimation.speed  = min(位移 * 4.0, 1.0)   （见 LivingEntity#updateWalkAnimation）
        // 后者约为前者的 6.7 倍，会让 0.05 阈值被显著放大 —— 极慢速移动也判定为“移动中”。
        //
        // 26.2 中 walkDist 已更名 moveDist，但<b>没有</b>保留 moveDistO（javap 确认），
        // 无法直接算增量。改用与“本 tick 水平位移”等价的速度量并乘回 0.6 还原量纲。
        // （玩家分支下面会用实际速度覆盖，所以本行主要影响非玩家实体。）
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

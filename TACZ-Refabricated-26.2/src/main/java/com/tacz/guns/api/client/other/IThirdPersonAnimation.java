package com.tacz.guns.api.client.other;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public interface IThirdPersonAnimation {
    /**
     * 3인칭 애니메이션: 주 손에 총기를 들고 있을 때
     *
     * @param entity   총기를 든 엔티티
     * @param rightArm 오른팔 모델
     * @param leftArm  왼팔 모델
     * @param head     머리 모델
     */
    void animateGunHold(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head);

    /**
     * 3인칭 애니메이션: 총기로 조준할 때
     *
     * @param entity      총기를 든 엔티티
     * @param rightArm    오른팔 모델
     * @param leftArm     왼팔 모델
     * @param head        머리 모델
     * @param aimProgress 조준 진행도 0~1
     */
    void animateGunAim(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head, float aimProgress);
}

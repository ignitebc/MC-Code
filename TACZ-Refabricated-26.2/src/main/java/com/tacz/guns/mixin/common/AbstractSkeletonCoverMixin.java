package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.ai.CoverBowAttackGoal;
import com.tacz.guns.entity.ai.GunCoverGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * 스켈레톤 계열에 엄폐 행동을 붙인다. 총을 들면 엄폐 Goal이 근접 공격보다 먼저 움직이고,
 * 활을 들면 바닐라 활 공격 대신 엄폐형 활 공격을 쓴다.
 */
@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonCoverMixin extends Monster {
    @Shadow
    @Final
    private RangedBowAttackGoal<AbstractSkeleton> bowGoal;
    /** 생성자 도중 처음 무기를 확인할 때 만든다. 이 필드의 초기화 시점에 기대지 않는다. */
    @Unique
    @Nullable
    private CoverBowAttackGoal<AbstractSkeleton> tacz$coverBowGoal;

    protected AbstractSkeletonCoverMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void tacz$addGunCoverGoal(CallbackInfo ci) {
        // 총을 든 스켈레톤은 근접 공격(우선순위 4)을 쓰므로 그보다 먼저 이동을 잡는다.
        this.goalSelector.addGoal(3, new GunCoverGoal(this));
    }

    /** 바닐라가 활 공격을 붙였으면 같은 우선순위에 엄폐형 활 공격으로 바꿔 끼운다. 활 공격 간격 설정은 바닐라 Goal에 남는다. */
    @Inject(method = "reassessWeaponGoal", at = @At("TAIL"))
    private void tacz$useCoverBowGoal(CallbackInfo ci) {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        if (this.tacz$coverBowGoal == null) {
            this.tacz$coverBowGoal = new CoverBowAttackGoal<>((AbstractSkeleton) (Object) this, this.bowGoal);
        }
        this.goalSelector.removeGoal(this.tacz$coverBowGoal);
        boolean holdsBow = this.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() == this.bowGoal);
        if (holdsBow) {
            this.goalSelector.removeGoal(this.bowGoal);
            this.goalSelector.addGoal(4, this.tacz$coverBowGoal);
        }
    }
}

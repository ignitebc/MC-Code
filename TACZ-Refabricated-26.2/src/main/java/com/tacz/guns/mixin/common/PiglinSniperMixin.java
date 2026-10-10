package com.tacz.guns.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.ai.SniperGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

/**
 * 저격총·지정사수소총을 든 피글린과 피글린 야수가 좀비·스켈레톤과 같은 저격 행동을 하게 한다.
 * <p>
 * 피글린은 Goal이 아니라 Brain으로 움직인다. Brain의 전투 행동은 매 틱 대상 쪽으로 걸을 곳을 다시 정해 저격 자리를 덮어쓰므로,
 * 저격하는 동안에는 Brain을 한 틱씩 건너뛰고 저격 행동이 이동과 시선을 맡는다. 공격 대상 기억은 그대로 남아 있어 사격은 이어진다.
 * 대상이 가까이 오거나 저격할 수 없게 되면 Brain을 다시 돌려 원래대로 달려가며 싸운다.
 */
@Mixin({Piglin.class, PiglinBrute.class})
public abstract class PiglinSniperMixin extends AbstractPiglin {
    /** 처음 필요할 때 만든다. 이 필드의 초기화 시점에 기대지 않는다. */
    @Unique
    @Nullable
    private SniperGoal.BrainDriver tacz$sniperDriver;

    protected PiglinSniperMixin(EntityType<? extends AbstractPiglin> type, Level level) {
        super(type, level);
    }

    @WrapOperation(
            method = "customServerAiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/Brain;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void tacz$snipeInsteadOfBrain(Brain<?> brain, ServerLevel level, LivingEntity entity, Operation<Void> original) {
        if (this.tacz$sniperDriver == null) {
            this.tacz$sniperDriver = new SniperGoal.BrainDriver(this);
        }
        boolean sniping = this.tacz$sniperDriver.tick();
        if (sniping) {
            return;
        }
        original.call(brain, level, entity);
    }
}

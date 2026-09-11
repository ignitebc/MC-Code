package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.NetherPlayerTargetGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class NetherHostilityMixin {
    @Shadow
    @Final
    protected GoalSelector targetSelector;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void serverutilities$registerNetherHostility(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;

        if (mob.level().isClientSide()) {
            return;
        }

        EntityType<?> entityType = mob.getType();
        boolean affectedMob = entityType == EntityType.ZOMBIFIED_PIGLIN
                || entityType == EntityType.ENDERMAN;

        if (!affectedMob) {
            return;
        }

        // 생성자에 등록하여 저장된 개체의 재로드와 차원 이동에도 대응한다.
        // 기존 보복·분노 대상 선택이 우선하도록 낮은 우선순위를 사용한다.
        targetSelector.addGoal(3, new NetherPlayerTargetGoal(mob));
    }
}

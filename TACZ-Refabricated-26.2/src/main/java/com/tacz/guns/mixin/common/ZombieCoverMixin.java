package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.ai.GunCoverGoal;
import com.tacz.guns.entity.ai.SniperGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 총을 든 좀비 계열에 엄폐 행동을 붙인다.
 * <p>
 * 드라운드와 좀비 피글린은 이 메서드를 덮어써 자체 행동을 쓰므로 대상에서 빠진다.
 * 허스크와 좀비 주민은 그대로 물려받는다.
 */
@Mixin(Zombie.class)
public abstract class ZombieCoverMixin extends Monster {
    protected ZombieCoverMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "addBehaviourGoals", at = @At("TAIL"))
    private void tacz$addGunCoverGoal(CallbackInfo ci) {
        // 근접 추격(우선순위 3)보다 먼저 이동을 잡는다. 두 Goal은 든 총 종류로 갈려 동시에 쓰이지 않는다.
        this.goalSelector.addGoal(2, new GunCoverGoal(this));
        this.goalSelector.addGoal(2, new SniperGoal(this));
    }
}

package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.ai.MonsterFriendlyFire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.PiglinBruteSpecificSensor;
import net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 총을 든 피글린이 총을 든 위더 스켈레톤을 원수로 기억하지 않게 한다.
 * <p>
 * 피글린은 감지기가 찾아 둔 원수(위더 스켈레톤, 위더)를 공격 대상으로 고른다. 둘 다 총을 들었으면 이 기억을 지워
 * 플레이어 등 다음 대상을 고르게 한다. 이미 싸우던 중이었다면 바닐라 규칙대로 더 이상 맞는 대상이 아니라고 보고 멈춘다.
 */
@Mixin({PiglinSpecificSensor.class, PiglinBruteSpecificSensor.class})
public abstract class PiglinSensorTruceMixin {
    @Inject(method = "doTick", at = @At("TAIL"))
    private void tacz$forgetArmedNemesis(ServerLevel level, LivingEntity piglin, CallbackInfo ci) {
        if (!MonsterFriendlyFire.isArmed(piglin)) {
            return;
        }
        Brain<?> brain = piglin.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_NEMESIS)) {
            return;
        }
        boolean armedNemesis = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS)
                .filter(MonsterFriendlyFire::isArmed)
                .isPresent();
        if (armedNemesis) {
            brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        }
    }
}

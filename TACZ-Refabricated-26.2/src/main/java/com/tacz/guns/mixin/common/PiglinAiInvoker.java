package com.tacz.guns.mixin.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** 총소리 경보를 받은 피글린을 맞았을 때와 같은 경로로 화나게 한다. */
@Mixin(PiglinAi.class)
public interface PiglinAiInvoker {
    @Invoker("wasHurtBy")
    static void tacz$wasHurtBy(ServerLevel level, Piglin piglin, LivingEntity attacker) {
        throw new AssertionError();
    }
}

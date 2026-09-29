package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.metrics.SpawnReasonTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 몹 생성 마무리 시점에 스폰 원인을 태그로 남긴다.
 * 하위 몹 클래스의 finalizeSpawn도 Mob.finalizeSpawn을 호출하므로 여기 한 곳에서 처리된다.
 */
@Mixin(Mob.class)
public abstract class MixinMobSpawnReason
{
    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void jobsplus$tagSpawnReason(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                         SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir)
    {
        SpawnReasonTag.tag((Mob) (Object) this, reason);
    }
}

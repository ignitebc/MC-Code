package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.mixin.accessor.DropMultiplierAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityDropMultiplier implements DropMultiplierAccessor
{

    @Unique
    private int jobsplus$dropMultiplier = 1;

    @Override
    public int jobsplus$getDropMultiplier()
    {
        return jobsplus$dropMultiplier;
    }

    @Override
    public void jobsplus$setDropMultiplier(int multiplier)
    {
        this.jobsplus$dropMultiplier = Math.max(1, multiplier);
    }

    @Override
    public void jobsplus$clearDropMultiplier()
    {
        this.jobsplus$dropMultiplier = 1;
    }

    /**
     * 엔티티가 드롭을 스폰할 때 ItemStack count를 증폭
     *
     * 주의: 아래 타겟 메서드 시그니처는 매핑/버전에 따라 약간 다를 수 있습니다. (1.21.10 Yarn에서
     * LivingEntity#spawnAtLocation(ItemStack) 또는 유사 메서드 사용)
     */
    @ModifyArg(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"), index = 0)
    private ItemStack jobsplus$multiplyDropStack(ItemStack original)
    {
        int m = this.jobsplus$dropMultiplier;
        if (m <= 1)
        {
            return original;
        }

        // 빈 스택/비정상 방지
        if (original == null || original.isEmpty())
        {
            return original;
        }

        ItemStack copy = original.copy();
        // 오버플로우 방지: Minecraft 스택 최대치는 보통 64
        int newCount = Math.min(copy.getMaxStackSize(), copy.getCount() * m);
        copy.setCount(newCount);
        return copy;
    }
}

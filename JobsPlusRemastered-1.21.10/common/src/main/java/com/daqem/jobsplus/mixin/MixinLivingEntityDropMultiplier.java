package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.accessor.DropMultiplierAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 1.21.10 기준:
 * - spawnAtLocation(ItemStack) 오버로드가 사라지고
 * - spawnAtLocation(ServerLevel, ItemStack[, ...]) 형태로 바뀜
 *
 * 따라서 타겟 시그니처를 1.21.10에 맞춰야 Mixin이 적용됩니다.
 */
@Mixin(Entity.class)
public abstract class MixinLivingEntityDropMultiplier implements DropMultiplierAccessor {

    @Unique
    private int jobsplus$dropMultiplier = 1;

    @Override
    public int jobsplus$getDropMultiplier() {
        return jobsplus$dropMultiplier;
    }

    @Override
    public void jobsplus$setDropMultiplier(int multiplier) {
        this.jobsplus$dropMultiplier = Math.max(1, multiplier);
    }

    @Override
    public void jobsplus$clearDropMultiplier() {
        this.jobsplus$dropMultiplier = 1;
    }

    /**
     * Entity#spawnAtLocation(ServerLevel, ItemStack) 대응
     * - argsOnly=true 이므로 "파라미터" 중 ItemStack만 찾아서 교체합니다.
     */
    @ModifyVariable(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private ItemStack jobsplus$multiplyDropStack_2args(ItemStack original) {
        return jobsplus$applyMultiplier(original);
    }

    /**
     * 1.21.10에서 존재할 수 있는 추가 오버로드도 같이 대응 (대표적으로 float/boolean 등이 붙는 경우가 있음)
     * - 아래는 "ServerLevel + ItemStack + float" 형태를 우선 대응합니다.
     */
    @ModifyVariable(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private ItemStack jobsplus$multiplyDropStack_3args(ItemStack original) {
        return jobsplus$applyMultiplier(original);
    }

    @Unique
    private ItemStack jobsplus$applyMultiplier(ItemStack original) {
        int m = this.jobsplus$dropMultiplier;
        if (m <= 1) return original;
        if (original == null || original.isEmpty()) return original;

        ItemStack copy = original.copy();

        long multiplied = (long) copy.getCount() * (long) m;
        int newCount = (int) Math.min(copy.getMaxStackSize(), multiplied);
        copy.setCount(newCount);

        return copy;
    }
}

package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.accessor.DropMultiplierAccessor;
import com.daqem.arc.player.SkillActivationNotifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

/**
 * 26.2 기준:
 * - spawnAtLocation(ItemStack) 오버로드가 사라지고
 * - spawnAtLocation(ServerLevel, ItemStack[, ...]) 형태로 바뀜
 *
 * 따라서 타겟 시그니처를 26.2에 맞춰야 Mixin이 적용됩니다.
 */
@Mixin(Entity.class)
public abstract class MixinLivingEntityDropMultiplier implements DropMultiplierAccessor {

    @Unique
    private int jobsplus$dropMultiplier = 1;

    @Unique
    private UUID jobsplus$dropRewardPlayer;

    @Unique
    private Component jobsplus$dropSkillName;

    @Override
    public int jobsplus$getDropMultiplier() {
        return jobsplus$dropMultiplier;
    }

    @Override
    public void jobsplus$setDropMultiplier(int multiplier) {
        this.jobsplus$dropMultiplier = Math.max(1, multiplier);
    }

    @Override
    public UUID jobsplus$getDropRewardPlayer() {
        return this.jobsplus$dropRewardPlayer;
    }

    @Override
    public void jobsplus$setDropRewardPlayer(UUID playerUuid) {
        this.jobsplus$dropRewardPlayer = playerUuid;
    }

    @Override
    public Component jobsplus$getDropSkillName() {
        return this.jobsplus$dropSkillName;
    }

    @Override
    public void jobsplus$setDropSkillName(Component skillName) {
        this.jobsplus$dropSkillName = skillName;
    }

    @Override
    public void jobsplus$clearDropMultiplier() {
        this.jobsplus$dropMultiplier = 1;
        this.jobsplus$dropRewardPlayer = null;
        this.jobsplus$dropSkillName = null;
    }

    /** 최종 드롭 생성 오버로드 한 곳에서만 배수를 적용해 위임 과정의 중복 적용을 막습니다. */
    @ModifyVariable(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private ItemStack jobsplus$multiplyDropStack(ItemStack original) {
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

        int extraCount = newCount - original.getCount();
        if (extraCount > 0 && this.jobsplus$dropRewardPlayer != null) {
            Entity entity = (Entity) (Object) this;
            if (entity.level() instanceof ServerLevel serverLevel) {
                ServerPlayer player = serverLevel.getServer().getPlayerList()
                        .getPlayer(this.jobsplus$dropRewardPlayer);
                if (player != null) {
                    SkillActivationNotifier.notifyExtraDrop(
                            player, this.jobsplus$dropSkillName, original.copyWithCount(extraCount));
                }
            }
        }

        return copy;
    }
}

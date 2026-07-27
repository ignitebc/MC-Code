package com.daqem.arc.mixin;

import com.daqem.arc.api.IArcAbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow implements IArcAbstractArrow {

    @Unique
    private int arc$fireDurationTicks;

    @Shadow protected abstract ItemStack getPickupItem();

    @Override
    public ItemStack arc$getPickupItem() {
        return getPickupItem();
    }

    @Override
    public int arc$getFireDurationTicks() {
        return arc$fireDurationTicks;
    }

    @Override
    public void arc$setFireDurationTicks(int fireDurationTicks) {
        this.arc$fireDurationTicks = fireDurationTicks;
    }

    @ModifyArg(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"),
            index = 0)
    private float arc$modifyFireDuration(float fireDurationSeconds) {
        return arc$fireDurationTicks > 0 ? arc$fireDurationTicks / 20F : fireDurationSeconds;
    }
}

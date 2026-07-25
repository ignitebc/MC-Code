package com.daqem.arc.mixin;

import com.daqem.arc.api.IArcAbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow implements IArcAbstractArrow {

    @Shadow protected abstract ItemStack getPickupItem();

    @Override
    public ItemStack arc$getPickupItem() {
        return getPickupItem();
    }
}

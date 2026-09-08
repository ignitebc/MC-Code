package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlaceRecipe.class)
public abstract class BackpackRecipePlacementMixin
{
    @Shadow @Final private Inventory inventory;

    @Inject(method = "getAmountOfFreeSlotsInInventory", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$freeSlots(CallbackInfoReturnable<Integer> cir)
    {
        BackpackInventory backpack = BackpackInventory.get(this.inventory);
        int free = cir.getReturnValue();
        for (int i = 0; i < backpack.capacity(); i++)
        {
            if (backpack.getItem(BackpackInventory.STORAGE_START + i).isEmpty()) free++;
        }
        cir.setReturnValue(free);
    }
}

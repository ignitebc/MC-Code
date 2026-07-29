package com.daqem.arc.mixin;

import com.daqem.arc.Arc;
import com.daqem.arc.api.block.ArcHopperFedContainer;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.player.brewing.BrewingStandData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseContainerBlockEntity.class)
public class MixinBaseContainerBlockEntity implements ArcHopperFedContainer {

    @Unique
    private static final String arc$HOPPER_FED_COUNT_TAG = "ArcHopperFedCount";

    @Unique
    private int arc$hopperFedItemCount;

    @Override
    public void arc$recordHopperInsertion(int slot, int amount) {
        boolean isFurnaceInput = (Object) this instanceof AbstractFurnaceBlockEntity && slot == 0;
        boolean isBrewingIngredient = (Object) this instanceof BrewingStandBlockEntity && slot == 3;
        if (amount <= 0 || (!isFurnaceInput && !isBrewingIngredient)) {
            return;
        }

        long hopperFedItemCount = (long) this.arc$hopperFedItemCount + amount;
        this.arc$hopperFedItemCount = (int) Math.min(Integer.MAX_VALUE, hopperFedItemCount);
        ((BaseContainerBlockEntity) (Object) this).setChanged();
    }

    @Override
    public int arc$consumeHopperFedItems(int amount) {
        if (amount <= 0 || this.arc$hopperFedItemCount <= 0) {
            return 0;
        }

        int consumedAmount = Math.min(amount, this.arc$hopperFedItemCount);
        this.arc$hopperFedItemCount -= consumedAmount;
        ((BaseContainerBlockEntity) (Object) this).setChanged();
        return consumedAmount;
    }

    @Inject(at = @At("TAIL"), method = "loadAdditional(Lnet/minecraft/world/level/storage/ValueInput;)V")
    private void arc$loadHopperFedItemCount(ValueInput valueInput, CallbackInfo ci) {
        this.arc$hopperFedItemCount = Math.max(0, valueInput.getIntOr(arc$HOPPER_FED_COUNT_TAG, 0));
    }

    @Inject(at = @At("TAIL"), method = "saveAdditional(Lnet/minecraft/world/level/storage/ValueOutput;)V")
    private void arc$saveHopperFedItemCount(ValueOutput valueOutput, CallbackInfo ci) {
        if (this.arc$hopperFedItemCount > 0) {
            valueOutput.putInt(arc$HOPPER_FED_COUNT_TAG, this.arc$hopperFedItemCount);
        }
    }

    @Inject(at = @At("HEAD"), method = "stillValid(Lnet/minecraft/world/entity/player/Player;)Z")
    private void stillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof ArcServerPlayer arcServerPlayer) {
            if ((BaseContainerBlockEntity) (Object) this instanceof BrewingStandBlockEntity brewingStand) {
                BlockPos blockPos = brewingStand.getBlockPos();
                if (Arc.BREWING_STANDS.containsKey(blockPos)) {
                    Arc.BREWING_STANDS.get(blockPos).setLastPlayerToInteract(arcServerPlayer);
                } else {
                    Arc.BREWING_STANDS.put(blockPos, new BrewingStandData(brewingStand));
                }
            }
        }
    }
}

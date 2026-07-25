package com.daqem.arc.mixin;

import com.daqem.arc.Arc;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.player.brewing.BrewingStandData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity {

    @Inject(at = @At("HEAD"), method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;)V")
    private static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BrewingStandBlockEntity brewingStandBlockEntity, CallbackInfo info) {
        if (!Arc.BREWING_STANDS.containsKey(blockPos)) {
            Arc.BREWING_STANDS.put(blockPos, new BrewingStandData(brewingStandBlockEntity));
        }
        BrewingStandData brewingStandData = Arc.BREWING_STANDS.get(blockPos);
        for (int i = 0; i < 3; i++) {
            if (brewingStandBlockEntity.getItem(i).isEmpty()) {
                if (brewingStandData.getBrewingStandItemOwner(i) != null) {
                    brewingStandData.removeBrewingStandItemOwner(i);
                }
            } else {
                if (brewingStandData.getBrewingStandItemOwner(i) == null) {
                    if (brewingStandData.getLastPlayerToInteract() != null) {
                        brewingStandData.addBrewingStandItemOwner(i, brewingStandData.getLastPlayerToInteract());
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "doBrew(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/NonNullList;)V")
    private static void doBrew(Level level, BlockPos blockPos, NonNullList<ItemStack> nonNullList, CallbackInfo ci) {
        if (Arc.BREWING_STANDS.containsKey(blockPos)) {
            BrewingStandData brewingStandData = Arc.BREWING_STANDS.get(blockPos);
            if (brewingStandData.getBrewingStandItemOwners().size() == nonNullList.stream().filter(itemStack -> (itemStack.getItem() instanceof PotionItem)).toList().size()) {
                int i = 0;
                for (ArcServerPlayer player : brewingStandData.getBrewingStandItemOwners().values()) {
                    PlayerEvents.onBrewPotion(player, nonNullList.get(i), blockPos, level);
                    ++i;
                }
            }
        }
    }
}

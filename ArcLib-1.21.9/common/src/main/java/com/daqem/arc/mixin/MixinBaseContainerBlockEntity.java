package com.daqem.arc.mixin;

import com.daqem.arc.Arc;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.player.brewing.BrewingStandData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseContainerBlockEntity.class)
public class MixinBaseContainerBlockEntity {

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

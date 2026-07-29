package com.daqem.itemrestrictions.mixin.block;

import com.daqem.itemrestrictions.level.block.ItemRestrictionsBrewingStandBlockEntity;
import com.daqem.itemrestrictions.level.block.ItemRestrictionsFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseContainerBlockEntity.class)
public abstract class MixinBaseContainerBlockEntity extends BlockEntity {

    public MixinBaseContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(at = @At("TAIL"), method = "stillValid(Lnet/minecraft/world/entity/player/Player;)Z")
    private void stillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (this instanceof ItemRestrictionsBrewingStandBlockEntity brewingStand) {
                if (brewingStand.itemrestrictions$getPlayer() != serverPlayer) {
                    brewingStand.itemrestrictions$setPlayer(serverPlayer);
                    brewingStand.itemrestrictions$setPlayerUUID(serverPlayer.getUUID());
                    if (brewingStand instanceof BrewingStandBlockEntity brewingStandBlockEntity) {
                        brewingStandBlockEntity.saveWithFullMetadata(serverPlayer.level().registryAccess());
                    }
                }
            }
            if (this instanceof ItemRestrictionsFurnaceBlockEntity furnace) {
                if (furnace.itemrestrictions$getPlayer() != serverPlayer) {
                    furnace.itemrestrictions$setPlayer(serverPlayer);
                    furnace.itemrestrictions$setPlayerUUID(serverPlayer.getUUID());
                    if (furnace instanceof AbstractFurnaceBlockEntity furnaceBlockEntity) {
                        furnaceBlockEntity.saveWithFullMetadata(serverPlayer.level().registryAccess());
                    }
                }
            }
        }
    }
}

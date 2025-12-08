package com.daqem.arc.mixin;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.event.triggers.BlockEvents;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase {

    @Inject(at = @At("RETURN"), method = "useItemOn", cancellable = true)
    public void use(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (player instanceof ArcServerPlayer arcServerPlayer) {
            if (cir.getReturnValue() == InteractionResult.CONSUME) {
                BlockState state = level.getBlockState(blockHitResult.getBlockPos());
                ActionResult actionResult = BlockEvents.onBlockInteract(arcServerPlayer, state, blockHitResult.getBlockPos(), level);
                if (actionResult.shouldCancelAction()) {
                    cir.setReturnValue(InteractionResult.FAIL);
                }
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F", cancellable = true)
    public void getDestroyProgress(Player player, BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Float> cir) {
        if (player instanceof ArcPlayer arcPlayer) {
            float destroySpeedModifier = new ActionDataBuilder(arcPlayer, ActionType.GET_DESTROY_SPEED)
                    .withData(ActionDataType.ITEM_STACK, player.getMainHandItem())
                    .withData(ActionDataType.ITEM, player.getMainHandItem().getItem())
                    .withData(ActionDataType.BLOCK_STATE, blockGetter.getBlockState(blockPos))
                    .withData(ActionDataType.BLOCK_POSITION, blockPos)
                    .build()
                    .sendToAction()
                    .getDestroySpeedModifier();
            float returnValue = cir.getReturnValue()
                    * destroySpeedModifier;
            cir.setReturnValue(returnValue);
        }
    }
}

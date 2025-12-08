package com.daqem.arc.mixin;

import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AxeItem.class)
public abstract class MixinAxeItem extends Item {

    @Shadow protected abstract Optional<BlockState> getStripped(BlockState arg);

    public MixinAxeItem(Properties properties) {
        super(properties);
    }

    @Inject(at = @At("HEAD"), method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;")
    public void useOn(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (useOnContext.getPlayer() instanceof ArcServerPlayer serverPlayer) {
            BlockPos clickedPos = useOnContext.getClickedPos();
            BlockState blockState = serverPlayer.arc$getLevel().getBlockState(clickedPos);
            if (this.getStripped(blockState).isPresent()) {
                PlayerEvents.onStripLog(serverPlayer, clickedPos, useOnContext.getLevel());

            }
        }
    }
}

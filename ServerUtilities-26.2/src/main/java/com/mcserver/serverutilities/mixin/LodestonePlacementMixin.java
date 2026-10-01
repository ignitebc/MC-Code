package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.lodestone.LodestoneOwnership;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class LodestonePlacementMixin {
    @Unique
    private static final Component SERVERUTILITIES_LIMIT_MESSAGE =
            Component.literal("자석석은 플레이어당 한 개만 설치할 수 있습니다.");

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void serverutilities$limitLodestonePlacement(BlockPlaceContext context,
                                                         CallbackInfoReturnable<InteractionResult> cir) {
        if (((BlockItem) (Object) this).getBlock() != Blocks.LODESTONE) {
            return;
        }
        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (LodestoneOwnership.canPlace(serverPlayer)) {
            return;
        }
        serverPlayer.sendSystemMessage(SERVERUTILITIES_LIMIT_MESSAGE, true);
        cir.setReturnValue(InteractionResult.FAIL);
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void serverutilities$recordLodestonePlacement(BlockPlaceContext context,
                                                          CallbackInfoReturnable<InteractionResult> cir) {
        if (((BlockItem) (Object) this).getBlock() != Blocks.LODESTONE) {
            return;
        }
        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!cir.getReturnValue().consumesAction()) {
            return;
        }
        LodestoneOwnership.recordPlacement(serverPlayer, context.getClickedPos());
    }
}

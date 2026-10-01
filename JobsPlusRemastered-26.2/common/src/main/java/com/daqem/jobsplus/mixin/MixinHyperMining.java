package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.hyper.HyperMiningHandler;
import com.daqem.jobsplus.accessor.HyperMiningAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinHyperMining implements HyperMiningAccess
{
    @Shadow @Final protected ServerPlayer player;
    @Unique private BlockPos jobsplus$requestedPos;
    @Unique private Direction jobsplus$requestedFace;
    @Unique private BlockState jobsplus$centerState;
    @Unique private ItemStack jobsplus$usedTool;
    @Unique private boolean jobsplus$miningRange;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void jobsplus$rememberFace(BlockPos pos, ServerboundPlayerActionPacket.Action action,
                                      Direction face, int maxBuildHeight, int sequence, CallbackInfo ci)
    {
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK)
        {
            this.jobsplus$requestedPos = pos.immutable();
            this.jobsplus$requestedFace = face;
            this.jobsplus$usedTool = null;
            this.jobsplus$centerState = null;
        }
        else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK)
        {
            this.jobsplus$requestedPos = null;
            this.jobsplus$usedTool = null;
            this.jobsplus$centerState = null;
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void jobsplus$captureBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        if (!this.jobsplus$miningRange && pos.equals(this.jobsplus$requestedPos))
        {
            this.jobsplus$usedTool = this.player.getMainHandItem();
            this.jobsplus$centerState = null;
        }
    }

    @Override
    public void jobsplus$afterBlockBreak(BlockPos pos, BlockState centerState)
    {
        if (!this.jobsplus$miningRange && pos.equals(this.jobsplus$requestedPos))
        {
            // Fabric AFTER는 중앙 블록의 전리품·내구도 처리 전이다. 성공 여부만 기록한다.
            this.jobsplus$centerState = centerState;
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void jobsplus$finishMining(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        if (this.jobsplus$miningRange || !pos.equals(this.jobsplus$requestedPos))
        {
            return;
        }
        this.jobsplus$requestedPos = null;
        BlockState centerState = this.jobsplus$centerState;
        this.jobsplus$centerState = null;
        if (!cir.getReturnValueZ() || centerState == null
                || this.jobsplus$usedTool == null || this.jobsplus$requestedFace == null)
        {
            return;
        }
        this.jobsplus$miningRange = true;
        try
        {
            HyperMiningHandler.mine(this.player, pos, this.jobsplus$requestedFace,
                    centerState, this.jobsplus$usedTool);
        }
        finally
        {
            this.jobsplus$miningRange = false;
            this.jobsplus$usedTool = null;
        }
    }
}

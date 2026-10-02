package com.daqem.jobsplus.mixin.achievement;

import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.jobsplus.achievement.AchievementManager;
import com.daqem.jobsplus.achievement.BlockBreakSnapshot;
import com.mcserver.serverutilities.level.ToolPlacedBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

/** 설치 기록이 블록 제거 때 삭제되기 전에 판정하고, 실제 파괴 완료 후에만 누적한다. */
@Mixin(ServerPlayerGameMode.class)
public abstract class MixinAchievementBlockBreak
{
    @Shadow @Final protected ServerPlayer player;
    @Unique private final Deque<BlockBreakSnapshot> jobsplus$breaks = new ArrayDeque<>();

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void jobsplus$beforeBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        BlockState state = player.level().getBlockState(pos);
        boolean natural = !ToolPlacedBlockData.get(player.level()).contains(pos);
        if (player instanceof ArcServerPlayer arcPlayer && arcPlayer.arc$getBlockPosCache().contains(player.level(), pos))
        {
            natural = false;
        }
        jobsplus$breaks.push(new BlockBreakSnapshot(state, natural));
    }

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void jobsplus$confirmRemoved(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        jobsplus$breaks.getFirst().confirmRemoved();
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void jobsplus$afterBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        BlockBreakSnapshot snapshot = jobsplus$breaks.pop();
        if (cir.getReturnValueZ() && snapshot.removed())
        {
            AchievementManager.recordBlock(player, snapshot.state(), snapshot.natural());
        }
    }
}

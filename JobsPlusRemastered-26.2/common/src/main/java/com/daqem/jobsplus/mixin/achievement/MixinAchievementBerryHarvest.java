package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SweetBerryBushBlock.class)
public abstract class MixinAchievementBerryHarvest
{
    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    private void jobsplus$countBerryHarvest(BlockState state, Level level, BlockPos pos, Player player,
                                            BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!(player instanceof ServerPlayer serverPlayer) || state.getValue(SweetBerryBushBlock.AGE) != 3)
        {
            return;
        }
        BlockState after = level.getBlockState(pos);
        if (after.getBlock() instanceof SweetBerryBushBlock && after.getValue(SweetBerryBushBlock.AGE) == 1)
        {
            AchievementManager.recordHarvest(serverPlayer, state);
        }
    }
}

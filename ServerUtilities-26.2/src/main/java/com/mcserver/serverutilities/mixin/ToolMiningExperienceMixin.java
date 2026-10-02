package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolLevelRules;
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

/** 플레이어가 직접 지정한 블록 한 개만 인정한다. 추가 범위 채굴은 EXP를 주지 않는다. */
@Mixin(value = ServerPlayerGameMode.class, priority = 1100)
abstract class ToolMiningExperienceMixin {
    @Shadow @Final
    protected ServerPlayer player;

    @Unique private BlockPos serverutilities$requestedPos;
    @Unique private BlockPos serverutilities$experiencePos;
    @Unique private ItemStack serverutilities$usedTool = ItemStack.EMPTY;
    @Unique private boolean serverutilities$blockRemoved;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void serverutilities$rememberRequestedBlock(BlockPos pos, ServerboundPlayerActionPacket.Action action,
                                                       Direction direction, int worldHeight, int sequence,
                                                       CallbackInfo callback) {
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            serverutilities$requestedPos = pos.immutable();
            serverutilities$experiencePos = null;
            serverutilities$usedTool = ItemStack.EMPTY;
            serverutilities$blockRemoved = false;
            ToolLevelRules.ensureLevel(player.getMainHandItem());
        } else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            serverutilities$requestedPos = null;
            serverutilities$experiencePos = null;
            serverutilities$usedTool = ItemStack.EMPTY;
            serverutilities$blockRemoved = false;
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void serverutilities$captureDirectBreak(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (!pos.equals(serverutilities$requestedPos)) return;
        // 요청을 먼저 소모해 같은 destroyBlock 호출 안의 추가 채굴과 구분한다.
        serverutilities$requestedPos = null;
        ItemStack tool = player.getMainHandItem();
        BlockState state = player.level().getBlockState(pos);
        if (!ToolLevelRules.canGainMiningExperience(player, tool, player.level(), pos, state)) return;
        serverutilities$experiencePos = pos.immutable();
        serverutilities$usedTool = tool;
    }

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void serverutilities$confirmBlockRemoved(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (pos.equals(serverutilities$experiencePos)) serverutilities$blockRemoved = true;
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void serverutilities$rewardDirectBreak(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (!pos.equals(serverutilities$experiencePos)) return;
        ItemStack tool = serverutilities$usedTool;
        boolean blockRemoved = serverutilities$blockRemoved;
        serverutilities$experiencePos = null;
        serverutilities$usedTool = ItemStack.EMPTY;
        serverutilities$blockRemoved = false;
        // 보호 플러그인이 취소한 행동은 제외한다. 바닐라 내구도 차감 이후 실제 도구에 기록한다.
        if (!callback.getReturnValue() || !blockRemoved) return;
        ToolLevelRules.addExperience(player, tool);
    }
}

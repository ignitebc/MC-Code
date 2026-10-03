package com.mcserver.serverutilities.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 직접·범위 채굴에서 실제로 제거한 유효 블록마다 도구 EXP를 지급한다. */
@Mixin(value = ServerPlayerGameMode.class, priority = 1100)
abstract class ToolMiningExperienceMixin {
    @Shadow @Final
    protected ServerPlayer player;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void serverutilities$prepareTool(BlockPos pos, ServerboundPlayerActionPacket.Action action,
                                             Direction direction, int worldHeight, int sequence,
                                             CallbackInfo callback) {
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            ToolLevelRules.ensureLevel(player.getMainHandItem());
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void serverutilities$captureBreak(BlockPos pos, CallbackInfoReturnable<Boolean> callback,
                                              @Share("experienceTool") LocalRef<ItemStack> experienceTool) {
        ItemStack tool = player.getMainHandItem();
        BlockState state = player.level().getBlockState(pos);
        // 제거 후에는 설치 기록도 지워지므로 경험치 자격을 미리 확인한다.
        if (!ToolLevelRules.canGainMiningExperience(player, tool, player.level(), pos, state)) return;
        // destroyBlock 재진입마다 별도 지역 변수를 사용해 중앙·주변 블록의 기록이 섞이지 않게 한다.
        experienceTool.set(tool);
    }

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void serverutilities$confirmBlockRemoved(BlockPos pos, CallbackInfoReturnable<Boolean> callback,
                                                     @Share("blockRemoved") LocalBooleanRef blockRemoved) {
        blockRemoved.set(true);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void serverutilities$rewardBreak(BlockPos pos, CallbackInfoReturnable<Boolean> callback,
                                             @Share("experienceTool") LocalRef<ItemStack> experienceTool,
                                             @Share("blockRemoved") LocalBooleanRef blockRemoved) {
        // 보호 플러그인이 취소한 행동은 제외한다. 바닐라 내구도 차감 이후 실제 도구에 기록한다.
        if (!callback.getReturnValueZ() || !blockRemoved.get()) return;
        ItemStack tool = experienceTool.get();
        if (tool == null) return;
        ToolLevelRules.addExperience(player, tool);
    }
}

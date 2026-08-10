package com.daqem.arc.event.triggers;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEvents {

    public static void registerEvents() {

        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (placer instanceof ArcServerPlayer arcServerPlayer) {

                ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.PLACE_BLOCK)
                        .withData(ActionDataType.BLOCK_STATE, state)
                        .withData(ActionDataType.BLOCK_POSITION, pos)
                        .withData(ActionDataType.WORLD, level)
                        .build()
                        .sendToAction();

                if (actionResult.shouldCancelAction()) {
                    return EventResult.interruptFalse();
                }

                if (state.getBlock() instanceof CropBlock) {
                    ActionResult actionResult1 = onPlantCrop(arcServerPlayer, state, pos, level);
                    if (actionResult1.shouldCancelAction()) {
                        return EventResult.interruptFalse();
                    }
                }

                arcServerPlayer.arc$getBlockPosCache().add(level, pos);
            }
            return EventResult.pass();
        });

        // BREAK_BLOCK 보상은 파괴 전 이벤트가 아니라 파괴가 확정된 이후에 지급한다.
        // 파괴 전 이벤트에서 지급하면 다른 모드(청크 보호 등)가 파괴를 취소해도 보상이 남고,
        // FallingTree처럼 추가 원목마다 파괴 전 이벤트를 호출하는 모드에서 보상이 증폭된다.
        // 플랫폼별 파괴 완료 이벤트에서 onBlockBreakComplete를 호출한다. (Fabric: PlayerBlockBreakEvents.AFTER)
    }

    /**
     * 블록이 실제로 파괴된 뒤 호출되어 BREAK_BLOCK 액션을 실행한다.
     */
    public static void onBlockBreakComplete(ServerLevel serverLevel, BlockPos pos, BlockState state,
                                            ArcServerPlayer arcServerPlayer) {
        final BlockPos blockPos = pos.immutable();
        final int expDrop = BlockBreakExpTracker.consume(blockPos, serverLevel.getGameTime());

        // ★ 핵심: "캔 순간"의 도구를 반드시 캡처
        final ItemStack usedTool =
                arcServerPlayer.arc$getServerPlayer().getMainHandItem().copy();

        new ActionDataBuilder(arcServerPlayer, ActionType.BREAK_BLOCK)
                .withData(ActionDataType.BLOCK_STATE, state)
                .withData(ActionDataType.BLOCK_POSITION, blockPos)
                .withData(ActionDataType.EXP_DROP, expDrop)
                .withData(ActionDataType.WORLD, serverLevel)
                .withData(ActionDataType.ITEM_STACK, usedTool)
                .build()
                .sendToAction();

        if (state.getBlock() instanceof CropBlock) {
            onHarvestCrop(arcServerPlayer, state, blockPos, serverLevel);
        }
    }

    public static ActionResult onBlockInteract(
            ArcServerPlayer player, BlockState state, BlockPos pos, Level level) {

        return new ActionDataBuilder(player, ActionType.INTERACT_BLOCK)
                .withData(ActionDataType.BLOCK_STATE, state)
                .withData(ActionDataType.BLOCK_POSITION, pos)
                .withData(ActionDataType.WORLD, level)
                .build()
                .sendToAction();
    }

    public static ActionResult onPlantCrop(
            ArcServerPlayer player, BlockState state, BlockPos pos, Level level) {

        return new ActionDataBuilder(player, ActionType.PLANT_CROP)
                .withData(ActionDataType.BLOCK_STATE, state)
                .withData(ActionDataType.BLOCK_POSITION, pos)
                .withData(ActionDataType.WORLD, level)
                .build()
                .sendToAction();
    }

    public static ActionResult onHarvestCrop(
            ArcServerPlayer player, BlockState state, BlockPos pos, Level level) {

        return new ActionDataBuilder(player, ActionType.HARVEST_CROP)
                .withData(ActionDataType.BLOCK_STATE, state)
                .withData(ActionDataType.BLOCK_POSITION, pos)
                .withData(ActionDataType.WORLD, level)
                .withData(ActionDataType.ITEM_STACK, player.arc$getServerPlayer().getMainHandItem().copy())
                .build()
                .sendToAction();
    }
}

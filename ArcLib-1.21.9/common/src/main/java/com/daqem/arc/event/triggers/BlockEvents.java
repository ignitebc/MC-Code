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

                arcServerPlayer.arc$getBlockPosCache().add(pos);
            }
            return EventResult.pass();
        });

        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {

            if (!(player instanceof ArcServerPlayer arcServerPlayer)) {
                return EventResult.pass();
            }

            if (!(level instanceof ServerLevel serverLevel)) {
                return EventResult.pass();
            }

            final BlockPos blockPos = pos.immutable();
            final BlockState originalState = state;
            final int expDrop = (xp == null) ? 0 : xp.get();

            // ★ 핵심: "캔 순간"의 도구를 반드시 캡처
            final ItemStack usedTool =
                    arcServerPlayer.arc$getServerPlayer().getMainHandItem().copy();

            new ActionDataBuilder(arcServerPlayer, ActionType.BREAK_BLOCK)
                    .withData(ActionDataType.BLOCK_STATE, originalState)
                    .withData(ActionDataType.BLOCK_POSITION, blockPos)
                    .withData(ActionDataType.EXP_DROP, expDrop)
                    .withData(ActionDataType.WORLD, serverLevel)
                    .withData(ActionDataType.ITEM_STACK, usedTool)
                    .build()
                    .sendToAction();

            if (originalState.getBlock() instanceof CropBlock) {
                onHarvestCrop(arcServerPlayer, originalState, blockPos, serverLevel);
            }

            return EventResult.pass();
        });
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
                .build()
                .sendToAction();
    }
}

package com.daqem.arc.event.triggers;

import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.core.BlockPos;
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
            if (player instanceof ArcServerPlayer arcServerPlayer) {
                ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.BREAK_BLOCK)
                        .withData(ActionDataType.BLOCK_STATE, state)
                        .withData(ActionDataType.BLOCK_POSITION, pos)
                        .withData(ActionDataType.EXP_DROP, xp == null ? 0 : xp.get())
                        .withData(ActionDataType.WORLD, level)
                        .build()
                        .sendToAction();

                if (actionResult.shouldCancelAction()) {
                    return EventResult.interruptFalse();
                }

                if (state.getBlock() instanceof CropBlock) {
                    ActionResult actionResult1 = onHarvestCrop(arcServerPlayer, state, pos, level);
                    if (actionResult1.shouldCancelAction()) {
                        return EventResult.interruptFalse();
                    }
                }
            }
            return EventResult.pass();
        });
    }

    public static ActionResult onBlockInteract(ArcServerPlayer player, BlockState state, BlockPos pos, Level level) {
        return new ActionDataBuilder(player, ActionType.INTERACT_BLOCK)
                .withData(ActionDataType.BLOCK_STATE, state)
                .withData(ActionDataType.BLOCK_POSITION, pos)
                .withData(ActionDataType.WORLD, level)
                .build()
                .sendToAction();
    }

    public static ActionResult onPlantCrop(ArcServerPlayer player, BlockState state, BlockPos pos, Level level) {
        return new ActionDataBuilder(player, ActionType.PLANT_CROP)
                .withData(ActionDataType.BLOCK_STATE, state)
                .withData(ActionDataType.BLOCK_POSITION, pos)
                .withData(ActionDataType.WORLD, level)
                .build()
                .sendToAction();
    }

    public static ActionResult onHarvestCrop(ArcServerPlayer player, BlockState state, BlockPos pos, Level level) {
        return new ActionDataBuilder(player, ActionType.HARVEST_CROP)
                .withData(ActionDataType.BLOCK_STATE, state)
                .withData(ActionDataType.BLOCK_POSITION, pos)
                .withData(ActionDataType.WORLD, level)
                .build()
                .sendToAction();
    }
}

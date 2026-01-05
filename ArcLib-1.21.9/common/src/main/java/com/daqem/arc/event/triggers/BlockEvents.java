package com.daqem.arc.event.triggers;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEvents
{

    public static void registerEvents()
    {
        BlockEvent.PLACE.register((level, pos, state, placer) ->
        {
            if (placer instanceof ArcServerPlayer arcServerPlayer)
            {
                ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.PLACE_BLOCK).withData(ActionDataType.BLOCK_STATE, state).withData(ActionDataType.BLOCK_POSITION, pos).withData(ActionDataType.WORLD, level).build().sendToAction();

                if (actionResult.shouldCancelAction())
                {
                    return EventResult.interruptFalse();
                }

                if (state.getBlock() instanceof CropBlock)
                {
                    ActionResult actionResult1 = onPlantCrop(arcServerPlayer, state, pos, level);
                    if (actionResult1.shouldCancelAction())
                    {
                        return EventResult.interruptFalse();
                    }
                }
                arcServerPlayer.arc$getBlockPosCache().add(pos);
            }
            return EventResult.pass();
        });

        BlockEvent.BREAK.register((level, pos, state, player, xp) ->
        {
            if (!(player instanceof ArcServerPlayer arcServerPlayer))
            {
                return EventResult.pass();
            }

            // 서버 월드가 아니면 처리하지 않음(안전장치)
            if (!(level instanceof ServerLevel serverLevel))
            {
                return EventResult.pass();
            }

            // BREAK 이벤트가 "실제 파괴"보다 먼저 불릴 수 있어,
            // 다음 서버 틱(작업 큐)에서 해당 위치 블록이 원래와 달라졌을 때만 보상 처리
            final BlockPos checkPos = pos.immutable();
            final BlockState originalState = state;
            final int expDrop = (xp == null) ? 0 : xp.get();

            MinecraftServer server = serverLevel.getServer();
            server.execute(() ->
            {
                // 월드가 언로드 되었을 가능성 방어
                if (serverLevel.isClientSide())
                    return;

                BlockState currentState = serverLevel.getBlockState(checkPos);

                // 블록이 실제로 "부서졌거나/치환됐는지" 확인:
                // 원래 블록과 동일하면 아직 파괴되지 않은 것으로 보고 무시
                if (currentState.getBlock() == originalState.getBlock())
                {
                    return;
                }

                ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.BREAK_BLOCK).withData(ActionDataType.BLOCK_STATE, originalState).withData(ActionDataType.BLOCK_POSITION, checkPos).withData(ActionDataType.EXP_DROP, expDrop).withData(ActionDataType.WORLD, serverLevel).build().sendToAction();

                // 취소는 “이미 파괴된 뒤”에는 의미가 약하지만,
                // 구조상 ActionResult 집계는 유지합니다.
                // (원한다면 여기서 별도 롤백 처리 등을 추가해야 함)

                if (originalState.getBlock() instanceof CropBlock)
                {
                    onHarvestCrop(arcServerPlayer, originalState, checkPos, serverLevel);
                }
            });

            return EventResult.pass();
        });
    }

    public static ActionResult onBlockInteract(ArcServerPlayer player, BlockState state, BlockPos pos, Level level)
    {
        return new ActionDataBuilder(player, ActionType.INTERACT_BLOCK).withData(ActionDataType.BLOCK_STATE, state).withData(ActionDataType.BLOCK_POSITION, pos).withData(ActionDataType.WORLD, level).build().sendToAction();
    }

    public static ActionResult onPlantCrop(ArcServerPlayer player, BlockState state, BlockPos pos, Level level)
    {
        return new ActionDataBuilder(player, ActionType.PLANT_CROP).withData(ActionDataType.BLOCK_STATE, state).withData(ActionDataType.BLOCK_POSITION, pos).withData(ActionDataType.WORLD, level).build().sendToAction();
    }

    public static ActionResult onHarvestCrop(ArcServerPlayer player, BlockState state, BlockPos pos, Level level)
    {
        return new ActionDataBuilder(player, ActionType.HARVEST_CROP).withData(ActionDataType.BLOCK_STATE, state).withData(ActionDataType.BLOCK_POSITION, pos).withData(ActionDataType.WORLD, level).build().sendToAction();
    }
}

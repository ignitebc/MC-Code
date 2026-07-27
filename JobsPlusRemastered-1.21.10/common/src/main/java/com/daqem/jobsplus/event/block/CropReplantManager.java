package com.daqem.jobsplus.event.block;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class CropReplantManager
{

    private static final List<ReplantRequest> REPLANT_REQUESTS = new ArrayList<>();

    private CropReplantManager()
    {
    }

    public static void registerEvent()
    {
        TickEvent.SERVER_POST.register(server ->
        {
            REPLANT_REQUESTS.removeIf(request ->
            {
                if (request.level().getGameTime() < request.replantGameTime())
                {
                    return false;
                }

                if (request.level().getBlockState(request.blockPos()).isAir()
                        && request.blockState().canSurvive(request.level(), request.blockPos()))
                {
                    request.level().setBlockAndUpdate(request.blockPos(), request.blockState());
                }
                return true;
            });
        });
    }

    public static void schedule(ServerLevel level, BlockPos blockPos, CropBlock cropBlock)
    {
        BlockPos immutableBlockPos = blockPos.immutable();
        REPLANT_REQUESTS.removeIf(request ->
                request.level() == level && request.blockPos().equals(immutableBlockPos));
        REPLANT_REQUESTS.add(new ReplantRequest(
                level,
                immutableBlockPos,
                cropBlock.getStateForAge(0),
                level.getGameTime() + 1));
    }

    private record ReplantRequest(
            ServerLevel level,
            BlockPos blockPos,
            BlockState blockState,
            long replantGameTime)
    {
    }
}

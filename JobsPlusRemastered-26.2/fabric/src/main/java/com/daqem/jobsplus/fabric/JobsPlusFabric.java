package com.daqem.jobsplus.fabric;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.accessor.HyperMiningAccess;
import com.daqem.jobsplus.command.arguments.EnumArgument;
import com.daqem.jobsplus.command.arguments.JobArgument;
import com.daqem.jobsplus.command.arguments.PowerupArgument;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class JobsPlusFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        JobsPlus.init();

        registerCommandArgumentTypes();
        registerHyperMining();
    }

    private void registerHyperMining() {
        Identifier phase = JobsPlus.getId("hyper_mining");
        // Arc 보상 뒤에 성공을 기록한다. 실제 범위 채굴은 destroyBlock 반환 직전에 시작한다.
        PlayerBlockBreakEvents.AFTER.addPhaseOrdering(Event.DEFAULT_PHASE, phase);
        PlayerBlockBreakEvents.AFTER.register(phase, (level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.gameMode instanceof HyperMiningAccess access) {
                access.jobsplus$afterBlockBreak(pos, state);
            }
        });
    }

    private void registerCommandArgumentTypes() {
        ArgumentTypeRegistry.registerArgumentType(JobsPlus.getId("job"), JobArgument.class, SingletonArgumentInfo.contextFree(JobArgument::job));
        ArgumentTypeRegistry.registerArgumentType(JobsPlus.getId("powerup"), PowerupArgument.class, SingletonArgumentInfo.contextFree(PowerupArgument::powerup));
        //noinspection rawtypes,unchecked
        ArgumentTypeRegistry.registerArgumentType(JobsPlus.getId("enum"), EnumArgument.class, new EnumArgument.Info());
    }
}

package com.daqem.arc.fabric;

import com.daqem.arc.Arc;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.command.argument.ActionArgument;
import com.daqem.arc.event.triggers.BlockEvents;
import com.daqem.arc.registry.ArcRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.server.level.ServerLevel;

public class ArcFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Arc.initCommon();
        ArcRegistry.init();
        registerCommandArgumentTypes();
        registerBlockBreakCompleteEvent();
    }

    private void registerCommandArgumentTypes() {
        ArgumentTypeRegistry.registerArgumentType(Arc.getId("action"), ActionArgument.class, SingletonArgumentInfo.contextFree(ActionArgument::action));
    }

    /**
     * BREAK_BLOCK 보상은 파괴가 실제로 완료된 뒤(AFTER)에만 지급한다.
     * BEFORE 시점 지급은 보호 모드가 파괴를 취소해도 보상이 남고,
     * FallingTree가 추가 원목마다 BEFORE를 호출해 보상이 증폭되는 문제가 있다.
     */
    private void registerBlockBreakCompleteEvent() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel && player instanceof ArcServerPlayer arcServerPlayer) {
                BlockEvents.onBlockBreakComplete(serverLevel, pos, state, arcServerPlayer);
            }
        });
    }
}

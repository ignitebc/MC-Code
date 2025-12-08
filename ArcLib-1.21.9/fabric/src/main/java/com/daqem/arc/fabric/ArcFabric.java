package com.daqem.arc.fabric;

import com.daqem.arc.Arc;
import com.daqem.arc.command.argument.ActionArgument;
import com.daqem.arc.registry.ArcRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class ArcFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Arc.initCommon();
        ArcRegistry.init();
        registerCommandArgumentTypes();
    }

    private void registerCommandArgumentTypes() {
        ArgumentTypeRegistry.registerArgumentType(Arc.getId("action"), ActionArgument.class, SingletonArgumentInfo.contextFree(ActionArgument::action));
    }
}

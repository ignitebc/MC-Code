package com.daqem.arc.neoforge;

import com.daqem.arc.Arc;
import dev.architectury.utils.EnvExecutor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Arc.MOD_ID)
public class ArcNeoForge {

    public ArcNeoForge(IEventBus modEventBus) {
        EnvExecutor.getEnvSpecific(
                () -> () -> new SideProxyNeoForge.Client(modEventBus),
                () -> () -> new SideProxyNeoForge.Server(modEventBus)
        );
    }
}

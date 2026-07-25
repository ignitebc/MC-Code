package com.daqem.arc.fabric.client;

import com.daqem.arc.Arc;
import com.daqem.arc.client.ArcClient;
import com.daqem.arc.registry.ArcRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;

public class ArcFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ArcClient.init();
    }
}

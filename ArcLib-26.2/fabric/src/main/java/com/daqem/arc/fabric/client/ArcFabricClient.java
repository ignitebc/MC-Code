package com.daqem.arc.fabric.client;

import com.daqem.arc.client.ArcClient;
import net.fabricmc.api.ClientModInitializer;

public class ArcFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ArcClient.init();
    }
}

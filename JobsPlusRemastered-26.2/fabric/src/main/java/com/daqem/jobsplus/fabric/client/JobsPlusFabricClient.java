package com.daqem.jobsplus.fabric.client;

import com.daqem.jobsplus.client.JobsPlusClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class JobsPlusFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        JobsPlusClient.init();
        registerKeyBindings();
    }

    private static void registerKeyBindings() {
        KeyMappingHelper.registerKeyMapping(JobsPlusClient.OPEN_MENU);
    }
}

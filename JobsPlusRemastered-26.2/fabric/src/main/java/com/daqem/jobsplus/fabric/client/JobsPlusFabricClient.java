package com.daqem.jobsplus.fabric.client;

import com.daqem.jobsplus.client.JobsPlusClient;
import com.daqem.jobsplus.fabric.networking.JobProtocolPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;

public class JobsPlusFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(JobProtocolPayload.TYPE, (payload, context) -> {
            // 수신 채널 등록으로 서버에 직업 패킷 v2 지원을 알린다.
        });
        JobsPlusClient.init();
        registerKeyBindings();
    }

    private static void registerKeyBindings() {
        KeyMappingHelper.registerKeyMapping(JobsPlusClient.OPEN_MENU);
    }
}

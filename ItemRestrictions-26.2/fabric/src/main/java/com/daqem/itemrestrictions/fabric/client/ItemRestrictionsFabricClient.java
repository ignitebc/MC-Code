package com.daqem.itemrestrictions.fabric.client;

import com.daqem.itemrestrictions.client.ItemRestrictionsClient;
import net.fabricmc.api.ClientModInitializer;

public class ItemRestrictionsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemRestrictionsClient.init();
    }
}

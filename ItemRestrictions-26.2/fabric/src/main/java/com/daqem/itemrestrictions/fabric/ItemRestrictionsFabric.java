package com.daqem.itemrestrictions.fabric;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.fabric.event.FabricChunkProtectionEvents;
import net.fabricmc.api.ModInitializer;

public class ItemRestrictionsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ItemRestrictions.init();
        FabricChunkProtectionEvents.registerEvents();
    }
}

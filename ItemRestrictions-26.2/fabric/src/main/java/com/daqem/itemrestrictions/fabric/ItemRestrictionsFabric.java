package com.daqem.itemrestrictions.fabric;

import com.daqem.itemrestrictions.ItemRestrictions;
import net.fabricmc.api.ModInitializer;

public class ItemRestrictionsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ItemRestrictions.init();
    }
}

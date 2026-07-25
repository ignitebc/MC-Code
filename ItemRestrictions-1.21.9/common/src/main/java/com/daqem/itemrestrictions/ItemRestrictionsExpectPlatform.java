package com.daqem.itemrestrictions;

import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class ItemRestrictionsExpectPlatform {

    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemRestrictionManager getItemRestrictionManager() {
        throw new AssertionError();
    }
}

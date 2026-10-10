package com.daqem.itemrestrictions;

import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class ItemRestrictionsExpectPlatform {

    @ExpectPlatform
    public static Path getConfigDirectory() {
        // 오류만 던진다. 실행 시 실제 구현으로 바뀐다.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemRestrictionManager getItemRestrictionManager() {
        throw new AssertionError();
    }
}

package com.daqem.arc;

import com.daqem.arc.data.ActionManager;
import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class ArcExpectPlatform {

    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ActionManager getActionManager() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }
}

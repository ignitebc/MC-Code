package com.daqem.arc;

import com.daqem.arc.data.ActionManager;
import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class ArcExpectPlatform {

    @ExpectPlatform
    public static Path getConfigDirectory() {
        // 오류만 던진다. 실행 시 실제 구현으로 바뀐다.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ActionManager getActionManager() {
        // 오류만 던진다. 실행 시 실제 구현으로 바뀐다.
        throw new AssertionError();
    }
}

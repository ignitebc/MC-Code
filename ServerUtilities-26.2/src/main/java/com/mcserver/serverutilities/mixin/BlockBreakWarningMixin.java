package com.mcserver.serverutilities.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
abstract class BlockBreakWarningMixin {
    @WrapWithCondition(
            method = "handleBlockBreakAction",
            at = @At(value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false),
            require = 1,
            allow = 1
    )
    private boolean serverutilities$shouldLogBlockBreakWarning(Logger logger, String message,
                                                              Object expectedPos, Object receivedPos) {
        // 로그 호출만 건너뛰고 바닐라의 채굴 취소와 파괴 진행 표시 정리는 그대로 실행한다.
        return !"Mismatch in destroy block pos: {} {}".equals(message);
    }
}

package moe.caramel.chat.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 다른 화면이 열려 있지 않을 때 Enter로 채팅 화면을 연다.
 */
@Mixin(KeyboardHandler.class)
public final class MixinKeyboardHandler {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void openChatWithEnter(
            final long windowId,
            final int action,
            final KeyEvent event,
            final CallbackInfo ci
    ) {
        if (action != GLFW.GLFW_PRESS
                || this.minecraft.player == null
                || this.minecraft.gui.screen() != null) {
            return;
        }

        int key = event.key();
        if (key != GLFW.GLFW_KEY_ENTER && key != GLFW.GLFW_KEY_KP_ENTER) {
            return;
        }

        this.minecraft.gui.openChatScreen(ChatComponent.ChatMethod.MESSAGE);
        ci.cancel();
    }
}

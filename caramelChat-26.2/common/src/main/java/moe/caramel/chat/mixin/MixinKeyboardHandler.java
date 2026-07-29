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
 * Opens the chat screen with Enter while no other screen is open.
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

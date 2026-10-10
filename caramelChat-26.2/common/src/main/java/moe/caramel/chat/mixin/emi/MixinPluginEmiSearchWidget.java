package moe.caramel.chat.mixin.emi;

import moe.caramel.chat.controller.EditBoxController;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * (EMI Mixin) 검색 결과를 곧바로 새로 고친다.
 */
@Mixin(targets = "dev.emi.emi.screen.widget.EmiSearchWidget", remap = false)
public final class MixinPluginEmiSearchWidget {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(final CallbackInfo ci) {
        final EditBox editBox = ((EditBox) (Object) this);
        EditBoxController.getWrapper(editBox)
            .setInsertCallback(() -> {
                if (editBox.responder != null) {
                    editBox.responder.accept(editBox.value);
                }
            });
    }
}

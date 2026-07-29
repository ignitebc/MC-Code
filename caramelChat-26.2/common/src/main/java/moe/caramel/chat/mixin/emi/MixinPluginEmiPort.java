package moe.caramel.chat.mixin.emi;

import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * (EMI Mixin) Fix crash in PREVIEW mode.
 */
@Mixin(targets = "dev.emi.emi.EmiPort", remap = false)
public final class MixinPluginEmiPort {

    @Inject(method = "ordered", at = @At("HEAD"), cancellable = true)
    private static void fixCrashInPreviewMode(final Component text, final CallbackInfoReturnable<FormattedCharSequence> cir) {
        if (text == null) {
            cir.setReturnValue(FormattedCharSequence.EMPTY);
        }
    }
}

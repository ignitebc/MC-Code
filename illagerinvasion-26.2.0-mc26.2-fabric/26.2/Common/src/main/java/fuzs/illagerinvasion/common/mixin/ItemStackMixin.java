package fuzs.illagerinvasion.common.mixin;

import fuzs.illagerinvasion.common.world.item.enhancement.EnhancementHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void appendEnhancementLevel(CallbackInfoReturnable<Component> callback) {
        ItemStack itemStack = (ItemStack) (Object) this;
        int enhancementLevel = EnhancementHelper.getEnhancementLevel(itemStack);
        if (enhancementLevel <= 0 || !EnhancementHelper.isEnhanceableEquipment(itemStack)) {
            return;
        }

        MutableComponent displayName = Component.empty().append(callback.getReturnValue());
        Component enhancementText = Component.literal(" +" + enhancementLevel + "강").withStyle(ChatFormatting.RED);
        displayName.append(enhancementText);
        callback.setReturnValue(displayName);
    }
}

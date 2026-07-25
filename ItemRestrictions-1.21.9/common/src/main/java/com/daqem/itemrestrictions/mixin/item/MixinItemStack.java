package com.daqem.itemrestrictions.mixin.item;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.data.RestrictionResult;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.level.player.ItemRestrictionsServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    private void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (player instanceof ItemRestrictionsServerPlayer itemRestrictionsPlayer) {
                if (player instanceof ArcPlayer arcPlayer) {
                    RestrictionResult craftingResult = itemRestrictionsPlayer.itemrestrictions$isRestricted(new ActionDataBuilder(arcPlayer, null)
                            .withData(ActionDataType.ITEM_STACK, jobsplus$getItemStack())
                            .build());
                    if (craftingResult.isRestricted(RestrictionType.USE_ITEM)) {
                        serverPlayer.sendSystemMessage(ItemRestrictions.translatable(RestrictionType.USE_ITEM.getTranslationKey()).withStyle(ChatFormatting.RED), true);
                        serverPlayer.inventoryMenu.sendAllDataToRemote();
                        cir.setReturnValue(InteractionResult.FAIL);
                        cir.cancel();
                    }
                }
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Unique
    private ItemStack jobsplus$getItemStack() {
        return (ItemStack) (Object) this;
    }
}

package fuzs.illagerinvasion.mixin;

import fuzs.illagerinvasion.init.ModItems;
import fuzs.illagerinvasion.init.ModRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {
    @Shadow
    private int age;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void discardRemovedItems(CallbackInfo callback) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (itemEntity.level().isClientSide() || !isRemovedItem(itemEntity.getItem())) {
            return;
        }

        itemEntity.setNeverPickUp();
        if (this.age >= 20) {
            itemEntity.discard();
            callback.cancel();
        }
    }

    @Unique
    private static boolean isRemovedItem(ItemStack itemStack) {
        if (itemStack.is(ModItems.UNUSUAL_DUST_ITEM.value())
                || itemStack.is(ModItems.MAGICAL_FIRE_CHARGE_ITEM.value())
                || itemStack.is(ModItems.ILLUSIONARY_DUST_ITEM.value())
                || itemStack.is(ModItems.LOST_CANDLE_ITEM.value())
                || itemStack.is(ModItems.HORN_OF_SIGHT_ITEM.value())
                || itemStack.is(ModItems.HALLOWED_GEM_ITEM.value())
                || itemStack.is(ModItems.PRIMAL_ESSENCE_ITEM.value())
                || itemStack.is(ModItems.PLATINUM_CHUNK_ITEM.value())
                || itemStack.is(ModItems.PLATINUM_SHEET_ITEM.value())
                || itemStack.is(ModItems.PLATINUM_INFUSED_HATCHET_ITEM.value())) {
            return true;
        }

        PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return potionContents.is(ModRegistry.BERSERKING_POTION)
                || potionContents.is(ModRegistry.LONG_BERSERKING_POTION)
                || potionContents.is(ModRegistry.STRONG_BERSERKING_POTION);
    }
}

package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import com.autovw.advancednetherite.common.backpack.BackpackInventoryAccess;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class BackpackInventoryMixin implements BackpackInventoryAccess, Container
{
    @Unique
    private final BackpackInventory advancednetherite$backpack = new BackpackInventory();

    @Override
    public BackpackInventory advancednetherite$getBackpackInventory()
    {
        return this.advancednetherite$backpack;
    }

    @Inject(method = "getContainerSize", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$size(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(cir.getReturnValue() + BackpackInventory.SLOT_COUNT);
    }

    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$get(int slot, CallbackInfoReturnable<ItemStack> cir)
    {
        if (BackpackInventory.isBackpackSlot(slot)) cir.setReturnValue(this.advancednetherite$backpack.getItem(slot));
    }

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$set(int slot, ItemStack stack, CallbackInfo ci)
    {
        if (!BackpackInventory.isBackpackSlot(slot)) return;
        this.advancednetherite$backpack.setItem(slot, stack);
        ci.cancel();
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$remove(int slot, int count, CallbackInfoReturnable<ItemStack> cir)
    {
        if (BackpackInventory.isBackpackSlot(slot)) cir.setReturnValue(this.advancednetherite$backpack.removeItem(slot, count));
    }

    @Inject(method = "removeItemNoUpdate", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$take(int slot, CallbackInfoReturnable<ItemStack> cir)
    {
        if (BackpackInventory.isBackpackSlot(slot)) cir.setReturnValue(this.advancednetherite$backpack.removeItemNoUpdate(slot));
    }

    @Inject(method = "removeItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$removeReference(ItemStack stack, CallbackInfo ci)
    {
        if (!this.advancednetherite$backpack.owns(stack)) return;
        for (int slot = BackpackInventory.EQUIPMENT_SLOT; slot < BackpackInventory.STORAGE_START + BackpackInventory.MAX_CAPACITY; slot++)
        {
            if (this.advancednetherite$backpack.getItem(slot) == stack)
            {
                this.advancednetherite$backpack.setItem(slot, ItemStack.EMPTY);
                ci.cancel();
                return;
            }
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack)
    {
        return !BackpackInventory.isBackpackSlot(slot) || this.advancednetherite$backpack.canPlace(slot, stack);
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$addToSlot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir)
    {
        if (BackpackInventory.isBackpackSlot(slot)) cir.setReturnValue(this.advancednetherite$backpack.insertAt(slot, stack));
    }

    @Inject(method = "getSlotWithRemainingSpace", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$remainingSpace(ItemStack incoming, CallbackInfoReturnable<Integer> cir)
    {
        if (cir.getReturnValue() != -1) return;
        for (int i = 0; i < this.advancednetherite$backpack.capacity(); i++)
        {
            int slot = BackpackInventory.STORAGE_START + i;
            ItemStack stack = this.advancednetherite$backpack.getItem(slot);
            if (!stack.isEmpty() && stack.isStackable() && ItemStack.isSameItemSameComponents(stack, incoming)
                    && stack.getCount() < getMaxStackSize(stack))
            {
                cir.setReturnValue(slot);
                return;
            }
        }
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$addOverflow(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir)
    {
        if (slot == -1 && this.advancednetherite$backpack.insert(stack))
        {
            this.setChanged();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$returnStack(ItemStack stack, boolean sendPacket, CallbackInfo ci)
    {
        if (this.advancednetherite$backpack.capacity() == 0) return;
        Inventory inventory = (Inventory) (Object) this;
        inventory.add(stack);
        if (!stack.isEmpty()) inventory.player.drop(stack, false);
        if (sendPacket && inventory.player instanceof ServerPlayer player)
        {
            player.inventoryMenu.broadcastChanges();
        }
        ci.cancel();
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void advancednetherite$save(ValueOutput.TypedOutputList<ItemStackWithSlot> output, CallbackInfo ci)
    {
        for (int slot = BackpackInventory.EQUIPMENT_SLOT; slot < BackpackInventory.STORAGE_START + BackpackInventory.MAX_CAPACITY; slot++)
        {
            ItemStack stack = this.advancednetherite$backpack.getItem(slot);
            if (!stack.isEmpty()) output.add(new ItemStackWithSlot(slot, stack));
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void advancednetherite$load(ValueInput.TypedInputList<ItemStackWithSlot> input, CallbackInfo ci)
    {
        this.advancednetherite$backpack.clear();
        for (ItemStackWithSlot item : input)
        {
            if (BackpackInventory.isBackpackSlot(item.slot())) this.advancednetherite$backpack.setItem(item.slot(), item.stack());
        }
    }

    @Inject(method = "isEmpty", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$isEmpty(CallbackInfoReturnable<Boolean> cir)
    {
        if (!this.advancednetherite$backpack.isEmpty()) cir.setReturnValue(false);
    }

    @Inject(method = "clearContent", at = @At("TAIL"))
    private void advancednetherite$clear(CallbackInfo ci)
    {
        this.advancednetherite$backpack.clear();
    }

    @Inject(method = "dropAll", at = @At("TAIL"))
    private void advancednetherite$dropAll(CallbackInfo ci)
    {
        Inventory inventory = (Inventory) (Object) this;
        for (int slot = BackpackInventory.EQUIPMENT_SLOT; slot < BackpackInventory.STORAGE_START + BackpackInventory.MAX_CAPACITY; slot++)
        {
            ItemStack stack = this.advancednetherite$backpack.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) inventory.player.drop(stack, true, false);
        }
    }

    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    private void advancednetherite$craftingContents(StackedItemContents contents, CallbackInfo ci)
    {
        for (int i = 0; i < BackpackInventory.MAX_CAPACITY; i++)
        {
            contents.accountSimpleStack(this.advancednetherite$backpack.getItem(BackpackInventory.STORAGE_START + i));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void advancednetherite$tickContents(CallbackInfo ci)
    {
        Inventory inventory = (Inventory) (Object) this;
        for (int i = 0; i < BackpackInventory.MAX_CAPACITY; i++)
        {
            ItemStack stack = this.advancednetherite$backpack.getItem(BackpackInventory.STORAGE_START + i);
            if (!stack.isEmpty()) stack.inventoryTick(inventory.player.level(), inventory.player, null);
        }
    }

    @Inject(method = "findSlotMatchingCraftingIngredient", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$craftingSlot(Holder<Item> ingredient, ItemStack existing, CallbackInfoReturnable<Integer> cir)
    {
        if (cir.getReturnValue() != -1) return;
        for (int i = 0; i < BackpackInventory.MAX_CAPACITY; i++)
        {
            int slot = BackpackInventory.STORAGE_START + i;
            ItemStack stack = this.advancednetherite$backpack.getItem(slot);
            if (!stack.isEmpty() && stack.is(ingredient) && Inventory.isUsableForCrafting(stack)
                    && (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, stack)))
            {
                cir.setReturnValue(slot);
                return;
            }
        }
    }
}

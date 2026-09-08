package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import com.autovw.advancednetherite.common.item.BackpackItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Server Utilities가 있을 때 사망 손실로 선택된 장착 가방과 내용물을 함께 삭제한다. */
@Pseudo
@Mixin(targets = "com.mcserver.serverutilities.death.DeathRules", remap = false)
public abstract class BackpackDeathPenaltyMixin
{
    @Redirect(method = "beforeDrops", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;removeItemNoUpdate(I)Lnet/minecraft/world/item/ItemStack;"),
            remap = false)
    private static ItemStack advancednetherite$deleteBackpackContents(Inventory inventory, int slot)
    {
        ItemStack removed = inventory.removeItemNoUpdate(slot);
        if (slot == BackpackInventory.EQUIPMENT_SLOT && removed.getItem() instanceof BackpackItem)
        {
            // 일반 교체·사망 드롭과 구별한다. 초과분 드롭 없이 추가 12칸을 모두 비운다.
            BackpackInventory.get(inventory).clear();
        }
        return removed;
    }
}

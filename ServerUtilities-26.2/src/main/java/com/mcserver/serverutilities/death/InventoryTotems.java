package com.mcserver.serverutilities.death;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * 손에 불사의 토템이 없을 때 인벤토리와 배낭에서 토템을 찾아 쓴다.
 * <p>
 * 바닐라는 양손만 보지만 손에 들고 싸우기 어려워 인벤토리 어디에 있든 발동하게 한다. 찾는 순서는 핫바, 나머지 인벤토리,
 * 배낭 칸이다(양손은 바닐라가 먼저 본다). 효과와 통계·발전 과제·화면 연출은 바닐라 토템과 같다.
 * 토템은 아이템 종류가 아니라 죽음 보호 데이터로 찾으므로 같은 데이터가 붙은 다른 아이템도 같이 동작한다.
 */
public final class InventoryTotems {
    /** 바닐라가 토템 연출을 보낼 때 쓰는 엔티티 이벤트 번호 */
    private static final byte TOTEM_USE_EVENT = 35;

    private InventoryTotems() { }

    /** 토템을 하나 써서 죽음을 막았으면 true */
    public static boolean use(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            // 방어구·보조 손 칸은 건너뛴다. 보조 손은 바닐라가 이미 봤다.
            if (isVanillaEquipmentSlot(slot)) continue;
            ItemStack stack = inventory.getItem(slot);
            DeathProtection protection = stack.get(DataComponents.DEATH_PROTECTION);
            if (protection == null) continue;
            ItemStack used = stack.copy();
            stack.shrink(1);
            if (stack.isEmpty()) inventory.setItem(slot, ItemStack.EMPTY);
            inventory.setChanged();
            applyLikeVanilla(player, used, protection);
            return true;
        }
        return false;
    }

    /** 바닐라 손 토템과 같은 순서로 통계, 발전 과제, 진동, 체력, 효과, 연출을 적용한다. */
    private static void applyLikeVanilla(ServerPlayer player, ItemStack used, DeathProtection protection) {
        player.awardStat(Stats.ITEM_USED.get(used.getItem()));
        CriteriaTriggers.USED_TOTEM.trigger(player, used);
        used.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
        player.setHealth(1.0F);
        protection.applyEffects(used, player);
        player.level().broadcastEntityEvent(player, TOTEM_USE_EVENT);
    }

    private static boolean isVanillaEquipmentSlot(int slot) {
        int equipmentStart = Inventory.INVENTORY_SIZE;
        int equipmentEnd = equipmentStart + Inventory.EQUIPMENT_SLOT_MAPPING.size();
        return slot >= equipmentStart && slot < equipmentEnd;
    }
}

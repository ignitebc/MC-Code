package com.mcserver.serverutilities.death;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DeathRules {
    private static final int PENALTY_ITEM_COUNT = 2;
    private static final Identifier PROTECTION_SCROLL =
            Identifier.fromNamespaceAndPath("advancednetherite", "death_item_protection_scroll");

    private DeathRules() { }

    public static void beforeDrops(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;
        DeathProtectedPlayer protectedPlayer = (DeathProtectedPlayer) player;
        // 이미 성립한 보호는 설정 변경·재접속과 무관하게 리스폰까지 유지한다.
        if (protectedPlayer.serverutilities$isDeathProtected()) return;
        var config = ServerUtilities.config();
        Inventory inventory = player.getInventory();
        if (config.deathProtection() && consumeProtectionScroll(inventory)) {
            protectedPlayer.serverutilities$setDeathProtected(true);
            broadcast(player, Component.empty().append(playerName(player))
                    .append("님의 사망 시 아이템 보존권이 사용되어 모든 소지품이 보호되었습니다."));
            return;
        }
        if (!config.deathPenalty()) return;

        List<Integer> filledSlots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) filledSlots.add(slot);
        }
        if (filledSlots.isEmpty()) {
            broadcast(player, Component.empty().append(playerName(player))
                    .append("님이 죽었습니다. 소지품이 없어 삭제된 아이템이 없습니다."));
            return;
        }
        List<Component> itemNames = new ArrayList<>();
        while (itemNames.size() < PENALTY_ITEM_COUNT && !filledSlots.isEmpty()) {
            int slot = filledSlots.remove(player.getRandom().nextInt(filledSlots.size()));
            // 배낭 삭제 연동이 함께 비운 내부 칸은 두 번째 손실로 세지 않고 다시 뽑는다.
            if (inventory.getItem(slot).isEmpty()) continue;
            ItemStack removed = inventory.removeItemNoUpdate(slot);
            if (removed.isEmpty()) continue;
            itemNames.add(removed.getHoverName().copy()
                    .append(removed.getCount() > 1 ? " " + removed.getCount() + "개" : "")
                    .withStyle(ChatFormatting.RED));
        }
        inventory.setChanged();
        MutableComponent lostItems = Component.empty();
        for (int i = 0; i < itemNames.size(); i++) {
            if (i > 0) lostItems.append(", ");
            lostItems.append(itemNames.get(i));
        }
        broadcast(player, Component.empty().append(playerName(player)).append("님이 죽었습니다. ")
                .append(lostItems).append("이(가) 사라집니다."));
    }

    private static boolean consumeProtectionScroll(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !PROTECTION_SCROLL.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) continue;
            stack.shrink(1);
            if (stack.isEmpty()) inventory.setItem(slot, ItemStack.EMPTY);
            inventory.setChanged();
            return true;
        }
        return false;
    }

    private static Component playerName(ServerPlayer player) {
        return player.getName().copy().withStyle(ChatFormatting.GOLD);
    }

    private static void broadcast(ServerPlayer player, Component message) {
        player.level().getServer().getPlayerList().broadcastSystemMessage(message, false);
    }
}

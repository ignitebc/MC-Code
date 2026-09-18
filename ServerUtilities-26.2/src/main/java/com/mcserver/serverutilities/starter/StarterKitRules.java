package com.mcserver.serverutilities.starter;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** 최초 접속 한 번만 시작 장비를 지급한다. */
public final class StarterKitRules {
    private static final Component GIVEN_MESSAGE =
            Component.literal("시작 장비를 지급했습니다. 금 방어구 한 벌과 방패, 금 곡괭이·도끼·삽입니다.");
    private static final List<ArmorPiece> ARMOR = List.of(
            new ArmorPiece(EquipmentSlot.HEAD, Items.GOLDEN_HELMET),
            new ArmorPiece(EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE),
            new ArmorPiece(EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS),
            new ArmorPiece(EquipmentSlot.FEET, Items.GOLDEN_BOOTS));
    private static final List<Item> CARRIED = List.of(
            Items.SHIELD, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL);

    private StarterKitRules() { }

    public static void onJoin(ServerPlayer player) {
        if (!ServerUtilities.config().starterKit()) return;
        StarterKitAccess state = (StarterKitAccess) player;
        if (state.serverutilities$starterKitGiven()) return;

        // 지급 도중 문제가 생겨도 두 번 받지 않도록 기록을 먼저 남긴다.
        state.serverutilities$setStarterKitGiven(true);
        for (ArmorPiece piece : ARMOR) {
            equipOrStore(player, piece.slot(), new ItemStack(piece.item()));
        }
        for (Item item : CARRIED) {
            store(player, new ItemStack(item));
        }
        player.sendSystemMessage(GIVEN_MESSAGE);
    }

    private static void equipOrStore(ServerPlayer player, EquipmentSlot slot, ItemStack stack) {
        // 첫 접속이면 착용 칸이 비어 있다. 이미 차 있으면 덮어쓰지 않고 인벤토리로 보낸다.
        if (player.getItemBySlot(slot).isEmpty()) {
            player.setItemSlot(slot, stack);
            return;
        }
        store(player, stack);
    }

    private static void store(ServerPlayer player, ItemStack stack) {
        boolean added = player.addItem(stack);
        // 인벤토리가 가득 찼으면 발밑에 떨어뜨려 지급을 놓치지 않는다.
        if (!added && !stack.isEmpty()) player.drop(stack, false);
    }

    private record ArmorPiece(EquipmentSlot slot, Item item) { }
}

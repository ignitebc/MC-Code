package com.mcserver.serverutilities.starter;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** 최초 접속 한 번만 시작 장비를 지급한다. */
public final class StarterKitRules {
    private static final Identifier LEGEND_PET_BOX_ID =
            Identifier.fromNamespaceAndPath("advancednetherite", "legend_petbox");
    private static final Component GIVEN_MESSAGE =
            Component.literal("시작 장비를 지급했습니다. 금 방어구 한 벌과 방패, 금 곡괭이·도끼·삽·괭이·검입니다.");
    private static final List<ArmorPiece> ARMOR = List.of(
            new ArmorPiece(EquipmentSlot.HEAD, Items.GOLDEN_HELMET),
            new ArmorPiece(EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE),
            new ArmorPiece(EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS),
            new ArmorPiece(EquipmentSlot.FEET, Items.GOLDEN_BOOTS));
    private static final List<Item> CARRIED = List.of(
            Items.SHIELD, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE,
            Items.GOLDEN_SWORD);

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

        // 테스트 서버 보상. Advanced Netherite가 설치된 경우에만 지급한다.
        BuiltInRegistries.ITEM.get(LEGEND_PET_BOX_ID).ifPresent(holder -> {
            store(player, new ItemStack(holder.value(), 1));
            player.sendSystemMessage(Component.literal("전설 펫 상자 1개를 지급했습니다."));
        });

        List<ItemStack> gunKit = createStarterGunKit(player.getRandom());
        if (gunKit.isEmpty()) return;
        // 이름과 탄약 수는 지급 전에 읽는다. 인벤토리에 넣으면 스택이 합쳐져 개수가 바뀔 수 있다.
        Component gunMessage = describeGunKit(gunKit);
        for (ItemStack stack : gunKit) {
            store(player, stack);
        }
        player.sendSystemMessage(gunMessage);
    }

    /**
     * 시작 장비에 함께 줄 총기와 탄약. 첫 번째가 총기이고 나머지는 탄약이다.
     * <p>
     * TACZ의 선택적 Mixin이 무작위 권총 1정과 그 총의 탄약 3탄창으로 바꾼다. TACZ가 없으면 주지 않는다.
     */
    private static List<ItemStack> createStarterGunKit(RandomSource random) {
        return List.of();
    }

    private static Component describeGunKit(List<ItemStack> gunKit) {
        ItemStack gun = gunKit.getFirst();
        int ammoCount = 0;
        for (ItemStack ammo : gunKit.subList(1, gunKit.size())) {
            ammoCount += ammo.getCount();
        }
        return Component.literal("무작위 권총: ").append(gunName(gun))
                .append(Component.literal(", 탄약 " + ammoCount + "발 (3탄창)"));
    }

    // TACZ 총기의 이름 메서드는 클라이언트 전용이라 서버에서는 공통 이름이 나온다.
    // TACZ의 선택적 Mixin이 총기별 이름으로 바꾼다.
    private static Component gunName(ItemStack gun) {
        return gun.getHoverName();
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

package com.mcserver.serverutilities.monster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class MonsterEquipmentRules {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final Item[][] ARMOR_SETS = {
            {Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS},
            {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS},
            {Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS},
            {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS},
            {Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS}
    };
    private static final List<Item> MELEE_WEAPONS = List.of(
            Items.COPPER_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
            Items.COPPER_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
            Items.COPPER_SPEAR, Items.IRON_SPEAR, Items.GOLDEN_SPEAR, Items.DIAMOND_SPEAR, Items.NETHERITE_SPEAR);

    private MonsterEquipmentRules() { }

    public static boolean isMonster(Mob mob) {
        return mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER;
    }

    public static void onSpawn(Entity entity) {
        if (!(entity instanceof Mob mob) || entity.level().isClientSide()) return;
        if (!isMonster(mob)) return;
        MonsterEquipmentAccess state = (MonsterEquipmentAccess) mob;
        if (state.serverutilities$equipmentRolled()) return;
        // 부위별 추첨이 아니라 개체당 한 번 추첨하여 당첨 개체에 풀세트를 지급한다.
        boolean equipped = mob.getRandom().nextInt(5) == 0;
        state.serverutilities$finishEquipmentRoll(equipped);
        if (!equipped) return;

        Item[] armor = ARMOR_SETS[mob.getRandom().nextInt(ARMOR_SETS.length)];
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            mob.setItemSlot(ARMOR_SLOTS[i], new ItemStack(armor[i]));
        }
        mob.setItemSlot(EquipmentSlot.MAINHAND, createWeapon(mob, MELEE_WEAPONS));
        mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        preventEquipmentDrops(mob);
    }

    // TACZ의 선택적 Mixin이 로드된 총기를 이 무기 후보군에 함께 넣는다.
    private static ItemStack createWeapon(Mob mob, List<Item> meleeWeapons) {
        return new ItemStack(meleeWeapons.get(mob.getRandom().nextInt(meleeWeapons.size())));
    }

    public static void preventEquipmentDrops(Mob mob) {
        for (EquipmentSlot slot : ARMOR_SLOTS) mob.setDropChance(slot, 0.0f);
        mob.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        mob.setDropChance(EquipmentSlot.OFFHAND, 0.0f);
        // 줍기로 지급 장비를 교체해 확정 드롭 상태가 되는 것을 방지한다.
        mob.setCanPickUpLoot(false);
    }
}

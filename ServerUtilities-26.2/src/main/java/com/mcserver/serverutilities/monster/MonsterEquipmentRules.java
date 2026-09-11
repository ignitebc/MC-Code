package com.mcserver.serverutilities.monster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public final class MonsterEquipmentRules {
    /** 방어구와 무기 각각의 지급 확률. 두 추첨은 서로 영향을 주지 않는다. */
    private static final int EQUIPMENT_CHANCE_DENOMINATOR = 5;

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

    /**
     * 무작위 장비를 지급할 몬스터인지 확인한다. 좀비 계열과 스켈레톤 계열만 대상이다.
     * <p>
     * 이 두 계열은 방어구와 손에 든 무기를 모두 그려 주고, 무기를 바꿔 줘도 근접 공격을
     * 그대로 이어 간다. 거미나 크리퍼는 장비를 그리지 않아 방어도만 몰래 오르고,
     * 레이드 몬스터는 석궁과 주문 같은 고유 공격 수단에 묶여 있어 무기를 바꾸면 손해다.
     */
    public static boolean isEquippableMonster(Mob mob) {
        return mob instanceof Zombie || mob instanceof AbstractSkeleton;
    }

    public static void onSpawn(Entity entity) {
        if (!(entity instanceof Mob mob) || entity.level().isClientSide()) return;
        if (!isMonster(mob) || !isOverworld(mob)) return;
        MonsterEquipmentAccess state = (MonsterEquipmentAccess) mob;
        if (state.serverutilities$equipmentRolled()) return;

        if (!isEquippableMonster(mob)) {
            state.serverutilities$finishEquipmentRoll(false, false);
            return;
        }

        // 방어구와 무기를 따로 추첨하므로 한쪽만 갖춘 개체도 나온다.
        boolean armorEquipped = rollEquipment(mob);
        boolean weaponEquipped = rollEquipment(mob);
        state.serverutilities$finishEquipmentRoll(armorEquipped, weaponEquipped);

        if (armorEquipped) {
            Item[] armor = ARMOR_SETS[mob.getRandom().nextInt(ARMOR_SETS.length)];
            for (int i = 0; i < ARMOR_SLOTS.length; i++) {
                mob.setItemSlot(ARMOR_SLOTS[i], new ItemStack(armor[i]));
            }
        }
        if (weaponEquipped) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, createWeapon(mob, MELEE_WEAPONS));
            mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        preventEquipmentDrops(mob, armorEquipped, weaponEquipped);
    }

    private static boolean isOverworld(Mob mob) {
        return Level.OVERWORLD.equals(mob.level().dimension());
    }

    private static boolean rollEquipment(Mob mob) {
        return mob.getRandom().nextInt(EQUIPMENT_CHANCE_DENOMINATOR) == 0;
    }

    // TACZ의 선택적 Mixin이 로드된 총기를 이 무기 후보군에 함께 넣는다.
    private static ItemStack createWeapon(Mob mob, List<Item> meleeWeapons) {
        return new ItemStack(meleeWeapons.get(mob.getRandom().nextInt(meleeWeapons.size())));
    }

    public static void preventEquipmentDrops(Mob mob, boolean armorEquipped, boolean weaponEquipped) {
        if (armorEquipped) {
            for (EquipmentSlot slot : ARMOR_SLOTS) mob.setDropChance(slot, 0.0f);
        }
        if (weaponEquipped) {
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
            mob.setDropChance(EquipmentSlot.OFFHAND, 0.0f);
        }
        // 줍기로 지급 장비를 교체해 확정 드롭 상태가 되는 것을 방지한다.
        if (armorEquipped || weaponEquipped) mob.setCanPickUpLoot(false);
    }
}

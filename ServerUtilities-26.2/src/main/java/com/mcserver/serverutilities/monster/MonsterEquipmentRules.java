package com.mcserver.serverutilities.monster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public final class MonsterEquipmentRules {
    /** 방어구와 무기 각각의 지급 확률(%). 두 추첨은 서로 영향을 주지 않는다. */
    private static final int EQUIPMENT_CHANCE_PERCENT = 30;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    /**
     * 지급할 방어구 세트와 레벨 계산용 등급 점수(F=1 ~ S=7).
     * <p>
     * 4부위 방어도 합이 낮은 순서로 매긴다. 가죽 7, 구리 10, 금 11, 사슬 12, 철 15, 다이아몬드 20,
     * 네더라이트 20이며, 방어도 합이 같은 다이아몬드와 네더라이트는 부위당 방어 강도(2, 3)로 나눈다.
     * 몬스터 방어구에는 장비 티어를 붙이지 않으므로 원래 수치 그대로다. 세트는 모두 같은 확률로 뽑는다.
     */
    private static final List<ArmorSet> ARMOR_SETS = List.of(
            new ArmorSet(1, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
            new ArmorSet(2, Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS),
            new ArmorSet(3, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS),
            new ArmorSet(4, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS,
                    Items.CHAINMAIL_BOOTS),
            new ArmorSet(5, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),
            new ArmorSet(6, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS),
            new ArmorSet(7, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS,
                    Items.NETHERITE_BOOTS));
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

    /**
     * 방어구만 추첨하는 몬스터인지 확인한다. 피글린과 피글린 야수가 대상이다.
     * <p>
     * 피글린은 석궁 사격과 금 물물교환이, 야수는 금 도끼가 고유 행동이라 무기는 건드리지 않는다.
     * 네더에서만 사는 몬스터이므로 오버월드 조건도 두지 않는다.
     */
    public static boolean isArmorOnlyMonster(Mob mob) {
        return mob instanceof AbstractPiglin;
    }

    public static void onSpawn(Entity entity) {
        if (!(entity instanceof Mob mob) || entity.level().isClientSide()) return;
        if (!isMonster(mob)) return;
        MonsterEquipmentAccess state = (MonsterEquipmentAccess) mob;
        if (state.serverutilities$equipmentRolled()) return;

        // 조건에 맞지 않는 개체도 추첨을 끝낸 것으로 기록한다.
        // 기록하지 않으면 생성 대기 표시가 남아, 네더에서 태어난 개체가 나중에 오버월드로 넘어올 때 추첨된다.
        boolean fullyEquippable = isFullyEquippable(mob);
        boolean rollsArmor = rollsArmor(mob);

        // 방어구와 무기를 따로 추첨하므로 한쪽만 갖춘 개체도 나온다.
        boolean armorEquipped = rollsArmor && rollEquipment(mob);
        boolean weaponEquipped = fullyEquippable && rollEquipment(mob);

        if (armorEquipped) {
            ArmorSet armor = ARMOR_SETS.get(mob.getRandom().nextInt(ARMOR_SETS.size()));
            List<Item> pieces = armor.pieces();
            for (int i = 0; i < ARMOR_SLOTS.length; i++) {
                mob.setItemSlot(ARMOR_SLOTS[i], new ItemStack(pieces.get(i)));
            }
        }
        if (weaponEquipped) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, createWeapon(mob, MELEE_WEAPONS));
            mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        preventEquipmentDrops(mob, armorEquipped, weaponEquipped);

        int level;
        if (mob instanceof Creeper) {
            level = rollCreeperLevel(mob);
        } else {
            // 레벨은 실제로 입힌 장비에서 계산하므로 지급을 마친 뒤 기록한다.
            level = calculateLevel(mob, armorEquipped, weaponEquipped);
        }
        state.serverutilities$finishEquipmentRoll(armorEquipped, weaponEquipped, level);
    }

    /** 크리퍼는 장비가 없으므로 LV1~LV7을 같은 확률로 뽑는다. 레벨이 폭발 피해와 블록 파괴 범위를 정한다. */
    private static int rollCreeperLevel(Mob mob) {
        return CreeperLevel.fromRoll(mob.getRandom().nextInt(CreeperLevel.LEVEL_COUNT));
    }

    /**
     * 추첨으로 지급한 장비로 정한 머리 위 레벨. 추첨 대상이 아닌 몬스터는 레벨을 표시하지 않는다.
     * <p>
     * 몬스터가 원래 가진 장비(스켈레톤의 활, 드라운드의 삼지창, 피글린의 금 검 등)는 등급이 없으므로
     * 지급받지 못한 것과 같게 본다. 추첨이 끝난 뒤에는 장비가 바뀌지 않으므로 한 번만 계산한다.
     */
    public static int calculateLevel(Mob mob, boolean armorEquipped, boolean weaponEquipped) {
        if (!rollsArmor(mob)) return MonsterLevel.NONE;

        int armorScore = MonsterLevel.MISSING_SCORE;
        if (armorEquipped) {
            armorScore = armorScore(mob.getItemBySlot(EquipmentSlot.CHEST));
        }
        int weaponScore = MonsterLevel.MISSING_SCORE;
        if (weaponEquipped) {
            weaponScore = weaponScore(mob.getItemBySlot(EquipmentSlot.MAINHAND));
        }
        return MonsterLevel.of(armorScore, weaponScore);
    }

    /** 방어구와 무기를 모두 추첨하는 몬스터인지 */
    private static boolean isFullyEquippable(Mob mob) {
        return isEquippableMonster(mob) && isOverworld(mob);
    }

    /** 방어구를 추첨하는 몬스터인지. 레벨을 표시하는 대상과 같다. */
    private static boolean rollsArmor(Mob mob) {
        return isFullyEquippable(mob) || isArmorOnlyMonster(mob);
    }

    private static boolean isOverworld(Mob mob) {
        return Level.OVERWORLD.equals(mob.level().dimension());
    }

    private static boolean rollEquipment(Mob mob) {
        return mob.getRandom().nextInt(100) < EQUIPMENT_CHANCE_PERCENT;
    }

    // TACZ의 선택적 Mixin이 로드된 총기를 이 무기 후보군에 함께 넣는다.
    private static ItemStack createWeapon(Mob mob, List<Item> meleeWeapons) {
        return new ItemStack(meleeWeapons.get(mob.getRandom().nextInt(meleeWeapons.size())));
    }

    /** 지급한 방어구 세트의 등급 점수. 흉갑으로 세트를 찾는다. */
    private static int armorScore(ItemStack chest) {
        for (ArmorSet set : ARMOR_SETS) {
            if (chest.is(set.chest())) return set.score();
        }
        return MonsterLevel.MISSING_SCORE;
    }

    // 여기서 다루는 무기는 근접 무기 후보뿐이다. TACZ의 선택적 Mixin이 총기일 때 총기 등급 점수로 바꾼다.
    private static int weaponScore(ItemStack weapon) {
        return MonsterLevel.MELEE_WEAPON_SCORE;
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
        // 피글린은 던져 준 금을 주워야 물물교환이 되므로 줍기를 막지 않는다.
        if ((armorEquipped || weaponEquipped) && !isArmorOnlyMonster(mob)) mob.setCanPickUpLoot(false);
    }

    /** 방어구 한 벌. 부위 순서는 {@link #ARMOR_SLOTS}와 같다. */
    private record ArmorSet(int score, Item head, Item chest, Item legs, Item feet) {
        List<Item> pieces() {
            return List.of(this.head, this.chest, this.legs, this.feet);
        }
    }
}

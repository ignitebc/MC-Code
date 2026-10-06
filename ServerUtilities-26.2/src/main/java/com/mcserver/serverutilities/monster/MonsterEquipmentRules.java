package com.mcserver.serverutilities.monster;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MonsterEquipmentRules {
    /** 방어구와 무기 각각의 지급 확률(%). 두 추첨은 서로 영향을 주지 않는다. */
    private static final int EQUIPMENT_CHANCE_PERCENT = 30;
    /** 네더에서 생성된 성체 피글린·피글린 야수가 총기를 드는 확률(%). 방어구 추첨과 서로 영향을 주지 않는다. */
    private static final int NETHER_GUN_CHANCE_PERCENT = 20;
    /**
     * 방어구 세트 추첨의 전체 몫(만분율). 세트마다 같은 몫을 나누고, 나누어떨어지지 않아 남는 몫은
     * 가장 낮은 세트에 준다. 11세트면 각 909(9.09%)이고 가죽만 910(9.10%)이다.
     */
    private static final int ARMOR_ROLL_TOTAL = 10_000;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    /** 부위별 아이템 ID 뒷부분. 순서는 {@link #ARMOR_SLOTS}와 같다. */
    private static final List<String> ARMOR_PIECE_SUFFIXES = List.of("_helmet", "_chestplate", "_leggings", "_boots");
    private static final int CHEST_PIECE_INDEX = 1;
    private static final String MINECRAFT = "minecraft";
    private static final String ADVANCED_NETHERITE = "advancednetherite";
    /**
     * 지급할 방어구 세트와 레벨 계산용 등급 점수(F=1 ~ S+=11). 점수가 낮은 세트부터 둔다.
     * <p>
     * 4부위 방어도 합이 낮은 순서로 매긴다. 가죽 7(F), 구리 10(E), 금 11(D), 사슬 12(C), 철 15(C+),
     * 다이아몬드 20(B), 네더라이트 20(B+), 잿빛 24(A), 태양빛 28(A+), 영혼빛 32(S), 서리빛 36(S+)이다.
     * 방어도 합이 같은 다이아몬드와 네더라이트는 부위당 방어 강도(2, 3)로 나눈다.
     * 몬스터 방어구에는 장비 티어를 붙이지 않으므로 원래 수치 그대로다.
     * <p>
     * Advanced Netherite 방어구는 컴파일 의존 없이 아이템 ID로 찾는다. 모드가 없으면 해당 세트는 추첨에서 빠진다.
     */
    private static final List<ArmorSet> ARMOR_SETS = List.of(
            armorSet(1, MINECRAFT, "leather"),
            armorSet(2, MINECRAFT, "copper"),
            armorSet(3, MINECRAFT, "golden"),
            armorSet(4, MINECRAFT, "chainmail"),
            armorSet(5, MINECRAFT, "iron"),
            armorSet(6, MINECRAFT, "diamond"),
            armorSet(7, MINECRAFT, "netherite"),
            armorSet(8, ADVANCED_NETHERITE, "ash"),
            armorSet(9, ADVANCED_NETHERITE, "sunlight"),
            armorSet(10, ADVANCED_NETHERITE, "soul"),
            armorSet(11, ADVANCED_NETHERITE, "frost"));
    private static final List<Item> MELEE_WEAPONS = List.of(
            Items.COPPER_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
            Items.COPPER_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
            Items.COPPER_SPEAR, Items.IRON_SPEAR, Items.GOLDEN_SPEAR, Items.DIAMOND_SPEAR, Items.NETHERITE_SPEAR);

    private MonsterEquipmentRules() { }

    public static boolean isMonster(Mob mob) {
        return mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER;
    }

    /**
     * 방어구와 무기를 모두 지급할 수 있는 몬스터 계열인지 확인한다. 좀비 계열과 스켈레톤 계열만 대상이다.
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
        if (!isMonster(mob)) return;
        MonsterEquipmentAccess state = (MonsterEquipmentAccess) mob;
        if (state.serverutilities$equipmentRolled()) return;

        // 조건에 맞지 않는 개체도 추첨을 끝낸 것으로 기록한다.
        // 기록하지 않으면 생성 대기 표시가 남아, 네더에서 태어난 개체가 나중에 오버월드로 넘어올 때 추첨된다.
        boolean rollsArmor = rollsArmor(mob);

        // 방어구와 무기를 따로 추첨하므로 한쪽만 갖춘 개체도 나온다.
        boolean armorEquipped = rollsArmor && rollChance(mob, EQUIPMENT_CHANCE_PERCENT);
        ItemStack weapon = rollWeapon(mob);
        boolean weaponEquipped = !weapon.isEmpty();

        if (armorEquipped) {
            List<Item> pieces = rollArmorPieces(mob);
            for (int i = 0; i < ARMOR_SLOTS.length; i++) {
                mob.setItemSlot(ARMOR_SLOTS[i], new ItemStack(pieces.get(i)));
            }
        }
        if (weaponEquipped) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
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
     * 추첨이 끝난 뒤에는 장비가 바뀌지 않으므로 생성할 때 한 번 계산한다.
     */
    public static int calculateLevel(Mob mob, boolean armorEquipped, boolean weaponEquipped) {
        if (!rollsArmor(mob)) return MonsterLevel.NONE;
        return equipmentLevel(mob, armorEquipped, weaponEquipped);
    }

    /**
     * 지급한 방어구 점수와 총기 점수를 더한 레벨. 합이 0이면 LV1이다.
     * <p>
     * 근접 무기와 원래 가진 장비(스켈레톤의 활, 드라운드의 삼지창, 피글린의 금 검·석궁 등)는 0점이다.
     * 현재 차원을 보지 않으므로 저장된 개체를 다시 계산할 때도 쓴다.
     */
    public static int equipmentLevel(Mob mob, boolean armorEquipped, boolean weaponEquipped) {
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

    /** 방어구와 무기를 모두 추첨하는 몬스터인지. 오버월드의 좀비·스켈레톤 계열이 대상이다. */
    private static boolean isFullyEquippable(Mob mob) {
        return isEquippableMonster(mob) && isOverworld(mob);
    }

    /**
     * 원래 무기를 그대로 쓰고 방어구를 추첨하는 몬스터인지.
     * <p>
     * 피글린과 피글린 야수는 차원과 관계없이 대상이다. 석궁 사격·금 도끼·금 물물교환이 고유 행동이라
     * 근접 무기로 바꾸지 않는다. 네더의 스켈레톤 계열(스켈레톤, 위더 스켈레톤)도 활과 돌 검을 그대로 쓴다.
     */
    private static boolean keepsNativeWeapon(Mob mob) {
        return isPiglin(mob) || isNetherSkeleton(mob);
    }

    /** 방어구를 추첨하는 몬스터인지. 레벨을 표시하는 대상과 같다. */
    private static boolean rollsArmor(Mob mob) {
        return isFullyEquippable(mob) || keepsNativeWeapon(mob);
    }

    /**
     * 총기를 추첨하는 피글린인지. 네더에서 생성된 성체 피글린과 피글린 야수만 대상이다.
     * <p>
     * 아기 피글린은 공격을 시작하지 않아 총을 들어도 쏘지 않으므로 제외한다.
     */
    private static boolean rollsNetherGun(Mob mob) {
        return isPiglin(mob) && isNether(mob) && !mob.isBaby();
    }

    /** 피글린 계열인지. 던져 준 금을 주워야 물물교환이 되므로 줍기 자체는 막지 않는다. */
    private static boolean isPiglin(Mob mob) {
        return mob instanceof AbstractPiglin;
    }

    private static boolean isNetherSkeleton(Mob mob) {
        return mob instanceof AbstractSkeleton && isNether(mob);
    }

    private static boolean isOverworld(Mob mob) {
        return Level.OVERWORLD.equals(mob.level().dimension());
    }

    private static boolean isNether(Mob mob) {
        return Level.NETHER.equals(mob.level().dimension());
    }

    private static boolean rollChance(Mob mob, int chancePercent) {
        return mob.getRandom().nextInt(100) < chancePercent;
    }

    /**
     * 지급할 방어구 4부위. 등록된 세트끼리 만분율 몫을 똑같이 나누고, 남는 몫은 가장 낮은 세트에 준다.
     */
    private static List<Item> rollArmorPieces(Mob mob) {
        List<List<Item>> availableSets = availableArmorSets();
        int setShare = ARMOR_ROLL_TOTAL / availableSets.size();
        int lowestSetShare = ARMOR_ROLL_TOTAL - setShare * (availableSets.size() - 1);

        int roll = mob.getRandom().nextInt(ARMOR_ROLL_TOTAL);
        if (roll < lowestSetShare) return availableSets.getFirst();

        int setIndex = 1 + (roll - lowestSetShare) / setShare;
        return availableSets.get(setIndex);
    }

    /** 네 부위가 모두 등록된 세트의 아이템. 점수가 낮은 순서이며 바닐라 세트가 있으므로 비지 않는다. */
    private static List<List<Item>> availableArmorSets() {
        List<List<Item>> availableSets = new ArrayList<>(ARMOR_SETS.size());
        for (ArmorSet set : ARMOR_SETS) {
            set.resolvePieces().ifPresent(availableSets::add);
        }
        return availableSets;
    }

    /**
     * 지급할 무기. 지급하지 않으면 빈 아이템이다.
     * <p>
     * 오버월드 좀비·스켈레톤 계열은 근접 무기와 총기 중에서, 네더 피글린 계열은 총기 중에서만 고른다.
     */
    private static ItemStack rollWeapon(Mob mob) {
        if (isFullyEquippable(mob)) {
            if (!rollChance(mob, EQUIPMENT_CHANCE_PERCENT)) return ItemStack.EMPTY;
            return createWeapon(mob, MELEE_WEAPONS);
        }
        if (rollsNetherGun(mob)) {
            if (!rollChance(mob, NETHER_GUN_CHANCE_PERCENT)) return ItemStack.EMPTY;
            return createNetherGun(mob);
        }
        return ItemStack.EMPTY;
    }

    // TACZ의 선택적 Mixin이 로드된 총기를 이 무기 후보군에 함께 넣는다.
    private static ItemStack createWeapon(Mob mob, List<Item> meleeWeapons) {
        return new ItemStack(meleeWeapons.get(mob.getRandom().nextInt(meleeWeapons.size())));
    }

    // TACZ의 선택적 Mixin이 로드된 총기 중 하나를 돌려준다. TACZ가 없으면 지급하지 않고 원래 무기를 쓴다.
    private static ItemStack createNetherGun(Mob mob) {
        return ItemStack.EMPTY;
    }

    /** 지급한 방어구 세트의 등급 점수. 흉갑으로 세트를 찾는다. */
    private static int armorScore(ItemStack chest) {
        Identifier chestId = BuiltInRegistries.ITEM.getKey(chest.getItem());
        for (ArmorSet set : ARMOR_SETS) {
            if (set.chestId().equals(chestId)) return set.score();
        }
        return MonsterLevel.MISSING_SCORE;
    }

    // 근접 무기는 레벨 계산에서 제외한다. TACZ의 선택적 Mixin이 총기일 때 총기 등급 점수(F=1 ~ S=7)로 바꾼다.
    private static int weaponScore(ItemStack weapon) {
        return MonsterLevel.MISSING_SCORE;
    }

    public static void preventEquipmentDrops(Mob mob, boolean armorEquipped, boolean weaponEquipped) {
        if (armorEquipped) {
            for (EquipmentSlot slot : ARMOR_SLOTS) mob.setDropChance(slot, 0.0f);
        }
        if (weaponEquipped) {
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
            // 피글린은 보조손으로 금을 감정한다. 0으로 두면 감정 중에 처치될 때 플레이어가 준 금이 사라진다.
            if (!isPiglin(mob)) mob.setDropChance(EquipmentSlot.OFFHAND, 0.0f);
        }
        // 줍기로 지급 장비를 교체해 확정 드롭 상태가 되는 것을 방지한다.
        // 피글린은 던져 준 금을 주워야 물물교환이 되므로 줍기는 두고, 지급한 칸의 교체만 막는다.
        if ((armorEquipped || weaponEquipped) && !isPiglin(mob)) mob.setCanPickUpLoot(false);
    }

    /**
     * 줍기로 바꾸지 못하게 막을 칸인지. 추첨으로 지급한 방어구 4부위와 주 손만 막는다.
     * <p>
     * 줍기를 막지 않는 피글린이 던져 준 금 무기·석궁으로 총을, 금 방어구로 지급 방어구를 바꾸지 못하게 한다.
     * 막힌 아이템은 피글린 인벤토리로 들어가며, 금 물물교환은 보조손에서 감정하므로 영향이 없다.
     */
    public static boolean isRandomEquipmentSlot(EquipmentSlot slot, boolean armorEquipped, boolean weaponEquipped) {
        if (slot == EquipmentSlot.MAINHAND) return weaponEquipped;
        for (EquipmentSlot armorSlot : ARMOR_SLOTS) {
            if (armorSlot == slot) return armorEquipped;
        }
        return false;
    }

    private static ArmorSet armorSet(int score, String namespace, String material) {
        List<Identifier> pieceIds = new ArrayList<>(ARMOR_PIECE_SUFFIXES.size());
        for (String suffix : ARMOR_PIECE_SUFFIXES) {
            pieceIds.add(Identifier.fromNamespaceAndPath(namespace, material + suffix));
        }
        return new ArmorSet(score, List.copyOf(pieceIds));
    }

    /** 방어구 한 벌. 다른 모드의 방어구도 담도록 아이템 ID로 둔다. 부위 순서는 {@link #ARMOR_SLOTS}와 같다. */
    private record ArmorSet(int score, List<Identifier> pieceIds) {
        Identifier chestId() {
            return this.pieceIds.get(CHEST_PIECE_INDEX);
        }

        /** 네 부위가 모두 등록되어 있으면 아이템 목록을, 하나라도 없으면 빈 값을 돌려준다. */
        Optional<List<Item>> resolvePieces() {
            List<Item> pieces = new ArrayList<>(this.pieceIds.size());
            for (Identifier pieceId : this.pieceIds) {
                Optional<Item> piece = BuiltInRegistries.ITEM.getOptional(pieceId);
                if (piece.isEmpty()) return Optional.empty();
                pieces.add(piece.get());
            }
            return Optional.of(pieces);
        }
    }
}

package com.mcserver.serverutilities.tier;

import com.mcserver.serverutilities.ServerUtilities;
import com.mcserver.serverutilities.level.ToolLevelRules;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 장비 등급의 추첨과 수치 적용을 담당한다.
 *
 * <p>등급은 별도 컴포넌트를 등록하지 않고 아이템의 커스텀 데이터에 기록한다. 커스텀 데이터는
 * 저장과 클라이언트 동기화가 이미 보장되므로 다른 모드의 아이템에도 그대로 붙는다.
 *
 * <p>수치는 언제나 아이템의 기본값에서 다시 계산한다. 그래서 같은 아이템에 여러 번 적용해도
 * 값이 누적되지 않는다. 강화로 붙은 수정자는 등급 배율의 대상이 아니며 그대로 남는다.
 *
 * <p>장비 하나에 내구도, 성능, 체력 등급을 따로 붙인다. 내구도는 모든 대상에 적용한다. 성능은
 * 굴착 도구의 채굴 속도, 무기의 공격력, 사람이 입는 방어구의 방어도에 적용한다. 체력은 굴착 도구,
 * 낚싯대, 무기, 사람이 입는 방어구에만 붙는다. 방어구는 입은 부위에서, 나머지는 주로 쓰는 손에
 * 들었을 때만 최대 체력이 바뀐다.
 */
public final class EquipmentTierRules {
    /** 각 등급을 커스텀 데이터에 기록할 때 쓰는 키 */
    private static final String DURABILITY_TIER_KEY = "EquipmentDurabilityTier";
    private static final String PERFORMANCE_TIER_KEY = "EquipmentPerformanceTier";
    private static final String HEALTH_TIER_KEY = "EquipmentHealthTier";
    /** 등급을 하나만 붙이던 때의 키. 내구도와 성능 등급으로 옮긴 뒤 지운다. */
    private static final String LEGACY_TIER_KEY = "EquipmentTier";
    /** 등급을 적용할 당시의 아이템. 업그레이드로 재질이 바뀐 것을 알아내는 데 쓴다. */
    private static final String TIER_ITEM_KEY = "EquipmentTierItem";

    /** 플레이어의 기본 공격력. 기준표의 공격력은 이 값을 포함하므로 배율도 함께 적용한다. */
    static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0D;
    /** 던진 삼지창의 바닐라 고정 피해. 등급 배율은 이 값에만 걸고 찌르기 인챈트 추가분은 그대로 둔다. */
    static final float TRIDENT_THROWN_DAMAGE = 8.0F;

    /** 인벤토리 전체를 훑는 주기. 매 틱 확인할 필요가 없다. */
    private static final int SCAN_INTERVAL_TICKS = 20;

    private static final String MODIFIER_NAMESPACE = "serverutilities";
    private static final Identifier ATTACK_DAMAGE_MODIFIER_ID = modifierId("tier_attack_damage");
    // 부위마다 식별자가 달라야 한다. 같으면 장비를 입을 때마다 서로 덮어써 마지막 한 부위만 남는다.
    private static final String ARMOR_MODIFIER_PREFIX = "tier_armor.";
    private static final String HEALTH_MODIFIER_PREFIX = "tier_health.";
    /** 네 부위가 같이 쓰던 예전 방어도 수정자. 다시 적용할 때 지운다. */
    private static final Identifier LEGACY_ARMOR_MODIFIER_ID = modifierId("tier_armor");

    private EquipmentTierRules() { }

    /**
     * 플레이어가 들고 있거나 입고 있는 장비 중 등급이 없는 것에 등급을 붙인다.
     *
     * <p>제작, 상자, 상점, 랜덤박스, 명령 지급을 따로 연동하지 않아도 되도록
     * 플레이어 인벤토리를 주기적으로 훑는 방식을 쓴다.
     */
    public static void tick(Player player) {
        if (!ServerUtilities.config().equipmentTiers()) return;
        if (player.tickCount % SCAN_INTERVAL_TICKS != 0) return;

        RandomSource random = player.getRandom();
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ensureTier(inventory.getItem(slot), random);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ensureTier(player.getItemBySlot(slot), random);
        }
    }

    /**
     * 등급이 없으면 추첨해 붙이고, 이미 있으면 현재 아이템 기준으로 수치가 맞는지 확인한다.
     *
     * <p>대장장이 작업대로 상위 재질이 되면 등급 기록은 그대로 따라오지만 내구도와 속성은
     * 이전 재질에서 계산한 값이 남는다. 그래서 기록해 둔 아이템과 달라졌으면 같은 등급으로
     * 다시 계산한다. 등급 자체는 바뀌지 않으므로 S 다이아몬드를 올리면 계속 S로 남는다.
     */
    public static void ensureTier(ItemStack stack, RandomSource random) {
        if (!isTierable(stack)) return;

        EquipmentTierSet tiers = readTiers(stack);
        if (tiers == null) {
            setTiers(stack, completeTiers(stack, random));
            return;
        }

        if (!itemId(stack).equals(readTierItemId(stack))) {
            setTiers(stack, tiers);
        }
    }

    /**
     * 비어 있는 등급만 채운다.
     *
     * <p>예전 단일 등급이 붙은 장비는 그 등급을 내구도와 성능에 그대로 쓰고 체력 등급만 새로 뽑는다.
     * 등급이 하나도 없는 장비는 세 등급을 각각 따로 뽑는다.
     */
    private static EquipmentTierSet completeTiers(ItemStack stack, RandomSource random) {
        CompoundTag tag = customTag(stack);
        EquipmentTier legacyTier = EquipmentTier.byLevel(tag.getIntOr(LEGACY_TIER_KEY, 0));
        EquipmentTier durability = readOrChoose(tag, DURABILITY_TIER_KEY, legacyTier, random);
        EquipmentTier performance = readOrChoose(tag, PERFORMANCE_TIER_KEY, legacyTier, random);
        EquipmentTier health = readOrChoose(tag, HEALTH_TIER_KEY, null, random);
        return new EquipmentTierSet(durability, performance, health);
    }

    private static EquipmentTier readOrChoose(CompoundTag tag, String key, EquipmentTier fallback,
                                              RandomSource random) {
        EquipmentTier recorded = EquipmentTier.byLevel(tag.getIntOr(key, 0));
        if (recorded != null) return recorded;
        if (fallback != null) return fallback;

        EquipmentTier[] tiers = EquipmentTier.values();
        return tiers[random.nextInt(tiers.length)];
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static String readTierItemId(ItemStack stack) {
        return customTag(stack).getStringOr(TIER_ITEM_KEY, "");
    }

    private static CompoundTag customTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /**
     * 아이템에 기록된 등급 묶음.
     *
     * @return 세 등급이 모두 기록돼 있으면 그 묶음. 하나라도 없거나 범위를 벗어나면 null
     */
    public static EquipmentTierSet readTiers(ItemStack stack) {
        if (stack.isEmpty()) return null;

        CompoundTag tag = customTag(stack);
        EquipmentTier durability = EquipmentTier.byLevel(tag.getIntOr(DURABILITY_TIER_KEY, 0));
        EquipmentTier performance = EquipmentTier.byLevel(tag.getIntOr(PERFORMANCE_TIER_KEY, 0));
        EquipmentTier health = EquipmentTier.byLevel(tag.getIntOr(HEALTH_TIER_KEY, 0));
        if (durability == null || performance == null || health == null) return null;
        return new EquipmentTierSet(durability, performance, health);
    }

    /**
     * 수치 계산과 화면 표시에 쓰는 등급 묶음.
     *
     * <p>상자에 있던 예전 장비는 플레이어 인벤토리에 들어오기 전까지 단일 등급만 갖고 있다.
     * 그동안에도 수치와 툴팁이 예전과 같도록 그 등급을 내구도와 성능에 쓰고 체력은 비워 둔다.
     *
     * @return 기록된 등급 묶음. 등급이 전혀 없으면 null
     */
    public static EquipmentTierSet readKnownTiers(ItemStack stack) {
        EquipmentTierSet tiers = readTiers(stack);
        if (tiers != null) return tiers;
        if (stack.isEmpty()) return null;

        EquipmentTier legacyTier = EquipmentTier.byLevel(customTag(stack).getIntOr(LEGACY_TIER_KEY, 0));
        if (legacyTier == null) return null;
        return new EquipmentTierSet(legacyTier, legacyTier, null);
    }

    /** 등급과 적용 대상 아이템을 기록하고 해당 배율을 수치에 반영한다. */
    public static void setTiers(ItemStack stack, EquipmentTierSet tiers) {
        String appliedTo = itemId(stack);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, (CompoundTag tag) -> {
            tag.putInt(DURABILITY_TIER_KEY, tiers.durability().level());
            tag.putInt(PERFORMANCE_TIER_KEY, tiers.performance().level());
            tag.putInt(HEALTH_TIER_KEY, tiers.health().level());
            tag.putString(TIER_ITEM_KEY, appliedTo);
            tag.remove(LEGACY_TIER_KEY);
        });
        applyTiers(stack, tiers);
    }

    /** 등급을 붙일 수 있는 아이템인지. 내구도가 있는 장비만 대상으로 한다. */
    public static boolean isTierable(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Integer baseMaxDamage = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
        return baseMaxDamage != null && baseMaxDamage > 0;
    }

    /** 곡괭이, 도끼, 삽, 괭이. 기준표에서 내구도와 채굴 속도만 다루는 부류다. */
    static boolean isDiggingTool(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES);
    }

    /** 사람이 입는 방어구 4부위. 늑대 갑옷 같은 몸통 방어구는 여기에 들지 않는다. */
    static boolean isHumanoidArmor(ItemStack stack) {
        return humanoidArmorSlot(stack) != null;
    }

    /**
     * 사람이 입는 방어구가 들어가는 부위.
     *
     * @return 방어구 부위. 사람이 입는 방어구가 아니면 null
     */
    private static EquipmentSlotGroup humanoidArmorSlot(ItemStack stack) {
        if (stack.is(ItemTags.HEAD_ARMOR)) return EquipmentSlotGroup.HEAD;
        if (stack.is(ItemTags.CHEST_ARMOR)) return EquipmentSlotGroup.CHEST;
        if (stack.is(ItemTags.LEG_ARMOR)) return EquipmentSlotGroup.LEGS;
        if (stack.is(ItemTags.FOOT_ARMOR)) return EquipmentSlotGroup.FEET;
        return null;
    }

    /** 검, 삼지창, 철퇴, 창처럼 기본 공격력이 있는 무기. 도끼는 굴착 도구로 본다. */
    static boolean isMeleeWeapon(ItemStack stack) {
        if (isDiggingTool(stack)) return false;
        return baseAmount(stack.getItem().components(), Attributes.ATTACK_DAMAGE) > 0.0D;
    }

    /** 활과 쇠뇌. 공격력 속성이 없어 화살 피해에 직접 등급을 반영한다. */
    static boolean isRangedWeapon(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    /** 체력 등급이 최대 체력을 바꾸는 장비. 가위, 방패, 겉날개, 늑대 갑옷은 내구도만 바뀐다. */
    static boolean hasHealthTier(ItemStack stack) {
        if (isDiggingTool(stack)) return true;
        if (ToolLevelRules.isFishingRod(stack)) return true;
        if (isHumanoidArmor(stack)) return true;
        if (isRangedWeapon(stack)) return true;
        return isMeleeWeapon(stack);
    }

    private static void applyTiers(ItemStack stack, EquipmentTierSet tiers) {
        DataComponentMap defaults = stack.getItem().components();

        applyDurability(stack, defaults, tiers.durability());
        applyAttributes(stack, defaults, tiers);
        if (isDiggingTool(stack)) {
            applyMiningSpeed(stack, defaults,
                    tiers.performance().performanceMultiplier() * ToolLevelRules.miningSpeedMultiplier(stack));
        }
    }

    /** LV 변경 시 내구도와 채굴 속도만 다시 계산한다. 공격/방어/체력 수정자는 그대로 둔다. */
    public static void refreshToolStats(ItemStack stack) {
        if (!ToolLevelRules.isLevelable(stack)) return;
        DataComponentMap defaults = stack.getItem().components();
        EquipmentTierSet tiers = readKnownTiers(stack);
        EquipmentTier durabilityTier = null;
        EquipmentTier performanceTier = null;
        if (tiers != null) {
            durabilityTier = tiers.durability();
            performanceTier = tiers.performance();
        }

        applyDurability(stack, defaults, durabilityTier);
        if (isDiggingTool(stack)) {
            double multiplier = ToolLevelRules.miningSpeedMultiplier(stack);
            if (performanceTier != null) multiplier *= performanceTier.performanceMultiplier();
            applyMiningSpeed(stack, defaults, multiplier);
        }
    }

    private static void applyDurability(ItemStack stack, DataComponentMap defaults, EquipmentTier tier) {
        Integer baseMaxDamage = defaults.get(DataComponents.MAX_DAMAGE);
        if (baseMaxDamage == null || baseMaxDamage <= 0) return;

        int tierMaxDamage = baseMaxDamage;
        if (tier != null) tierMaxDamage = tier.scaleDurability(baseMaxDamage);
        int scaledMaxDamage = tierMaxDamage;
        if (ToolLevelRules.isLevelable(stack)) {
            long leveledMaxDamage = Math.round(tierMaxDamage * ToolLevelRules.durabilityMultiplier(stack));
            scaledMaxDamage = (int) Math.clamp(leveledMaxDamage, 1L, Integer.MAX_VALUE);
        }
        int oldMaxDamage = stack.getMaxDamage();
        int oldDamage = stack.getDamageValue();
        stack.set(DataComponents.MAX_DAMAGE, scaledMaxDamage);
        if (ToolLevelRules.isLevelable(stack) && oldMaxDamage > 0 && oldMaxDamage != scaledMaxDamage) {
            // 남은 내구도 비율을 유지한다. 레벨업으로 무료 수리가 발생하지 않도록 올림한다.
            int scaledDamage = (int) Math.ceil(oldDamage * (double) scaledMaxDamage / oldMaxDamage);
            stack.setDamageValue(Math.min(scaledMaxDamage, scaledDamage));
            return;
        }
        if (stack.getDamageValue() > scaledMaxDamage) {
            stack.setDamageValue(scaledMaxDamage);
        }
    }

    /**
     * 공격력, 방어도, 최대 체력 수정자를 붙인다.
     *
     * <p>기본 수정자를 고쳐 쓰는 대신 차이만큼의 수정자를 따로 붙인다. 그래야 강화나 다른 모드가
     * 붙인 수정자를 건드리지 않는다. 차이는 언제나 아이템 기본값에서 구하므로 값이 누적되지 않는다.
     *
     * <p>굴착 도구의 공격력은 바꾸지 않는다. 도끼도 굴착 도구로 보므로 공격력이 그대로 남는다.
     */
    private static void applyAttributes(ItemStack stack, DataComponentMap defaults, EquipmentTierSet tiers) {
        boolean scaleAttackDamage = !isDiggingTool(stack);
        EquipmentSlotGroup armorSlot = humanoidArmorSlot(stack);
        double multiplier = tiers.performance().performanceMultiplier();

        ItemAttributeModifiers baseModifiers =
                defaults.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers current = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, baseModifiers);

        // 아이템 기본 수정자는 언제나 현재 아이템의 것을 쓴다. 대장장이 작업대로 재질이 바뀌면
        // 이전 재질의 기본 수정자가 그대로 남아 있으므로 같은 식별자는 새 것으로 덮는다.
        Set<Identifier> baseModifierIds = new HashSet<>();
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : baseModifiers.modifiers()) {
            baseModifierIds.add(entry.modifier().id());
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        // 강화 같은 다른 출처의 수정자는 그대로 둔다.
        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            Identifier modifierId = entry.modifier().id();
            if (isTierModifier(modifierId) || baseModifierIds.contains(modifierId)) continue;
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }

        for (ItemAttributeModifiers.Entry entry : baseModifiers.modifiers()) {
            if (entry.modifier().operation() != AttributeModifier.Operation.ADD_VALUE) continue;

            Identifier modifierId = null;
            double baseAmount = entry.modifier().amount();
            if (scaleAttackDamage && Attributes.ATTACK_DAMAGE.equals(entry.attribute())) {
                modifierId = ATTACK_DAMAGE_MODIFIER_ID;
                baseAmount += PLAYER_BASE_ATTACK_DAMAGE;
            } else if (armorSlot != null && Attributes.ARMOR.equals(entry.attribute())) {
                modifierId = modifierId(ARMOR_MODIFIER_PREFIX + armorSlot.getSerializedName());
            }
            if (modifierId == null) continue;

            double difference = baseAmount * (multiplier - 1.0D);
            if (difference == 0.0D) continue;

            builder.add(entry.attribute(),
                    new AttributeModifier(modifierId, difference, AttributeModifier.Operation.ADD_VALUE),
                    entry.slot());
        }

        if (hasHealthTier(stack)) {
            // 방어구는 입은 부위에서만, 도구와 무기는 주로 쓰는 손에 들었을 때만 체력이 바뀐다.
            EquipmentSlotGroup healthSlot = EquipmentSlotGroup.MAINHAND;
            if (armorSlot != null) healthSlot = armorSlot;
            Identifier healthModifierId = modifierId(HEALTH_MODIFIER_PREFIX + healthSlot.getSerializedName());
            builder.add(Attributes.MAX_HEALTH,
                    new AttributeModifier(healthModifierId, tiers.health().healthPoints(),
                            AttributeModifier.Operation.ADD_VALUE),
                    healthSlot);
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    /**
     * 도구가 적합한 블록을 캘 때의 속도에만 배율을 적용한다.
     *
     * <p>규칙에 없는 블록의 기본 속도는 맨손 기준값이므로 그대로 둔다.
     */
    private static void applyMiningSpeed(ItemStack stack, DataComponentMap defaults, double multiplier) {
        Tool baseTool = defaults.get(DataComponents.TOOL);
        if (baseTool == null) return;

        List<Tool.Rule> scaledRules = new ArrayList<>(baseTool.rules().size());
        for (Tool.Rule rule : baseTool.rules()) {
            Optional<Float> scaledSpeed = rule.speed().map(speed -> (float) (speed * multiplier));
            scaledRules.add(new Tool.Rule(rule.blocks(), scaledSpeed, rule.correctForDrops()));
        }

        stack.set(DataComponents.TOOL, new Tool(scaledRules, baseTool.defaultMiningSpeed(),
                baseTool.damagePerBlock(), baseTool.canDestroyBlocksInCreative()));
    }

    /**
     * 화살 피해에 쏜 활이나 쇠뇌의 성능 등급을 반영한다.
     *
     * <p>바닐라는 속도와 기본 피해를 곱한 값을 정수로 올린다. 기본 피해에 배율을 곱하면 올림 때문에
     * 등급 차이가 사라지므로, 올림이 끝난 피해에 기본 화살 피해분의 차이만 더한다. 힘 인챈트와 치명타
     * 추가 피해는 근접 무기의 날카로움처럼 등급과 무관하게 그대로 둔다.
     *
     * @param weapon     화살을 쏜 무기. 디스펜서처럼 무기가 없으면 null
     * @param speed      맞힌 순간의 화살 속도
     * @param baseDamage 화살의 기본 피해
     * @param damage     바닐라가 계산한 최종 피해
     */
    public static float scaleArrowDamage(ItemStack weapon, double speed, double baseDamage, float damage) {
        if (weapon == null || !isRangedWeapon(weapon)) return damage;
        EquipmentTierSet tiers = readKnownTiers(weapon);
        if (tiers == null) return damage;

        double baseArrowDamage = Mth.ceil(speed * baseDamage);
        double difference = baseArrowDamage * (tiers.performance().performanceMultiplier() - 1.0D);
        return (float) Math.max(0.0D, damage + difference);
    }

    /**
     * 던진 삼지창의 고정 피해에 그 삼지창의 성능 등급을 반영한다.
     *
     * @param trident    던진 삼지창. 없으면 null
     * @param baseDamage 바닐라 고정 피해
     */
    public static float scaleThrownTridentDamage(ItemStack trident, float baseDamage) {
        if (trident == null) return baseDamage;
        EquipmentTierSet tiers = readKnownTiers(trident);
        if (tiers == null) return baseDamage;

        return (float) (baseDamage * tiers.performance().performanceMultiplier());
    }

    /** 아이템 기본 수정자 중 해당 속성에 더해지는 값의 합 */
    static double baseAmount(DataComponentMap defaults, Holder<Attribute> attribute) {
        ItemAttributeModifiers modifiers =
                defaults.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        double total = 0.0D;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.modifier().operation() != AttributeModifier.Operation.ADD_VALUE) continue;
            if (!attribute.equals(entry.attribute())) continue;
            total += entry.modifier().amount();
        }
        return total;
    }

    private static boolean isTierModifier(Identifier modifierId) {
        if (!MODIFIER_NAMESPACE.equals(modifierId.getNamespace())) return false;
        if (ATTACK_DAMAGE_MODIFIER_ID.equals(modifierId)) return true;
        if (LEGACY_ARMOR_MODIFIER_ID.equals(modifierId)) return true;

        String path = modifierId.getPath();
        return path.startsWith(ARMOR_MODIFIER_PREFIX) || path.startsWith(HEALTH_MODIFIER_PREFIX);
    }

    private static Identifier modifierId(String path) {
        return Identifier.fromNamespaceAndPath(MODIFIER_NAMESPACE, path);
    }
}

package com.mcserver.serverutilities.tier;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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
 * <p>부위별로 바꾸는 항목이 다르다. 굴착 도구는 채굴 속도만, 근접 무기는 공격력만,
 * 사람이 입는 방어구는 방어도만 바꾼다. 내구도는 모든 대상에 적용한다.
 */
public final class EquipmentTierRules {
    /** 등급을 커스텀 데이터에 기록할 때 쓰는 키 */
    private static final String TIER_KEY = "EquipmentTier";
    /** 등급을 적용할 당시의 아이템. 업그레이드로 재질이 바뀐 것을 알아내는 데 쓴다. */
    private static final String TIER_ITEM_KEY = "EquipmentTierItem";

    /** 플레이어의 기본 공격력. 기준표의 공격력은 이 값을 포함하므로 배율도 함께 적용한다. */
    private static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0D;

    /** 인벤토리 전체를 훑는 주기. 매 틱 확인할 필요가 없다. */
    private static final int SCAN_INTERVAL_TICKS = 20;

    private static final Identifier ATTACK_DAMAGE_MODIFIER_ID = modifierId("tier_attack_damage");
    private static final Identifier ARMOR_MODIFIER_ID = modifierId("tier_armor");

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
     * 다시 계산한다. 등급 자체는 바뀌지 않으므로 1티어 다이아몬드를 올리면 계속 1티어로 남는다.
     */
    public static void ensureTier(ItemStack stack, RandomSource random) {
        if (!isTierable(stack)) return;

        EquipmentTier tier = readTier(stack);
        if (tier == null) {
            EquipmentTier[] tiers = EquipmentTier.values();
            setTier(stack, tiers[random.nextInt(tiers.length)]);
            return;
        }

        if (!itemId(stack).equals(readTierItemId(stack))) {
            setTier(stack, tier);
        }
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static String readTierItemId(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getStringOr(TIER_ITEM_KEY, "");
    }

    /**
     * 아이템에 기록된 등급.
     *
     * @return 기록된 등급. 없거나 범위를 벗어나면 null
     */
    public static EquipmentTier readTier(ItemStack stack) {
        if (stack.isEmpty()) return null;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return EquipmentTier.byLevel(customData.copyTag().getIntOr(TIER_KEY, 0));
    }

    /** 등급과 적용 대상 아이템을 기록하고 해당 배율을 수치에 반영한다. */
    public static void setTier(ItemStack stack, EquipmentTier tier) {
        String appliedTo = itemId(stack);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, (CompoundTag tag) -> {
            tag.putInt(TIER_KEY, tier.level());
            tag.putString(TIER_ITEM_KEY, appliedTo);
        });
        applyTier(stack, tier);
    }

    /** 등급을 붙일 수 있는 아이템인지. 내구도가 있는 장비만 대상으로 한다. */
    public static boolean isTierable(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Integer baseMaxDamage = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
        return baseMaxDamage != null && baseMaxDamage > 0;
    }

    /** 곡괭이, 도끼, 삽, 괭이. 기준표에서 내구도와 채굴 속도만 다루는 부류다. */
    private static boolean isDiggingTool(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES);
    }

    /** 사람이 입는 방어구 4부위. 늑대 갑옷 같은 몸통 방어구는 여기에 들지 않는다. */
    private static boolean isHumanoidArmor(ItemStack stack) {
        return stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR)
                || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR);
    }

    private static void applyTier(ItemStack stack, EquipmentTier tier) {
        DataComponentMap defaults = stack.getItem().components();
        boolean diggingTool = isDiggingTool(stack);

        applyDurability(stack, defaults, tier);
        applyAttributes(stack, defaults, tier.performanceMultiplier(), diggingTool);
        if (diggingTool) {
            applyMiningSpeed(stack, defaults, tier.performanceMultiplier());
        }
    }

    private static void applyDurability(ItemStack stack, DataComponentMap defaults, EquipmentTier tier) {
        Integer baseMaxDamage = defaults.get(DataComponents.MAX_DAMAGE);
        if (baseMaxDamage == null || baseMaxDamage <= 0) return;

        int scaledMaxDamage = tier.scaleDurability(baseMaxDamage);
        stack.set(DataComponents.MAX_DAMAGE, scaledMaxDamage);
        if (stack.getDamageValue() > scaledMaxDamage) {
            stack.setDamageValue(scaledMaxDamage);
        }
    }

    /**
     * 공격력과 방어도에 배율을 적용한다.
     *
     * <p>기본 수정자를 고쳐 쓰는 대신 차이만큼의 수정자를 따로 붙인다. 그래야 강화나 다른 모드가
     * 붙인 수정자를 건드리지 않는다. 차이는 언제나 아이템 기본값에서 구하므로 값이 누적되지 않는다.
     *
     * <p>굴착 도구의 공격력은 바꾸지 않는다. 도끼도 굴착 도구로 보므로 공격력이 그대로 남는다.
     */
    private static void applyAttributes(ItemStack stack, DataComponentMap defaults, double multiplier,
                                        boolean diggingTool) {
        boolean scaleAttackDamage = !diggingTool;
        boolean scaleArmor = isHumanoidArmor(stack);

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
            } else if (scaleArmor && Attributes.ARMOR.equals(entry.attribute())) {
                modifierId = ARMOR_MODIFIER_ID;
            }
            if (modifierId == null) continue;

            double difference = baseAmount * (multiplier - 1.0D);
            if (difference == 0.0D) continue;

            builder.add(entry.attribute(),
                    new AttributeModifier(modifierId, difference, AttributeModifier.Operation.ADD_VALUE),
                    entry.slot());
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

    private static boolean isTierModifier(Identifier modifierId) {
        return ATTACK_DAMAGE_MODIFIER_ID.equals(modifierId) || ARMOR_MODIFIER_ID.equals(modifierId);
    }

    private static Identifier modifierId(String path) {
        return Identifier.fromNamespaceAndPath("serverutilities", path);
    }
}

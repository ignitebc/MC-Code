package fuzs.illagerinvasion.world.item.enhancement;

import fuzs.illagerinvasion.IllagerInvasion;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Map;

/**
 * 장비 강화 시스템의 확률 계산과 재료 판정을 담당한다.
 * <p>
 * 강화 단계는 별도 컴포넌트를 등록하지 않고 아이템의 커스텀 데이터에 기록한다. 커스텀 데이터는
 * 저장과 클라이언트 동기화가 이미 보장되므로 다른 모드의 아이템에도 그대로 붙일 수 있다.
 */
public final class EnhancementHelper {

    public static final int MAX_ENHANCEMENT_LEVEL = 10;
    /** 강화 시도 1회당 소모되는 강화 원석 수 */
    public static final int ENHANCEMENT_GEM_COST = 1;
    /** 파괴 판정이 처음 생기는 강화 단계 */
    public static final int DESTROY_CHANCE_START_LEVEL = 3;
    /** 파괴 판정이 처음 생기는 단계의 파괴 확률 */
    public static final int DESTROY_CHANCE_AT_START_LEVEL = 1;
    /** 한 단계 오를 때마다 늘어나는 파괴 확률 */
    public static final int DESTROY_CHANCE_STEP = 2;
    /** 무기 한 단계당 오르는 공격력 */
    public static final double ATTACK_DAMAGE_PER_LEVEL = 1.0D;
    /** 방어구 한 단계당 오르는 최대 체력. 하트 반 칸이 1이다. */
    public static final double MAX_HEALTH_PER_LEVEL = 1.0D;
    /** 채굴 속도 보너스를 계산할 때 기준으로 삼는 효율 마법 레벨 */
    public static final int REFERENCE_EFFICIENCY_LEVEL = 5;
    /** 강화로 붙는 속성 수정자를 구분하는 식별자 */
    public static final ResourceLocation ENHANCEMENT_MODIFIER_ID = IllagerInvasion.id("enhancement");
    /** 강화 대상 장비를 데이터로 조정할 수 있도록 태그로 관리한다. */
    public static final TagKey<Item> ENHANCEABLE_EQUIPMENT = TagKey.create(Registries.ITEM,
            IllagerInvasion.id("enhanceable_equipment"));

    private static final String ENHANCEMENT_LEVEL_KEY = "EnhancementLevel";
    private static final ResourceLocation ENHANCEMENT_GEM_ID = ResourceLocation.fromNamespaceAndPath(
            "advancednetherite",
            "enhancement_gem");
    private static final ResourceLocation PROTECTION_SCROLL_ID = ResourceLocation.fromNamespaceAndPath(
            "advancednetherite",
            "enhance_protection_scroll");
    private static final Map<ResourceLocation, Integer> SUCCESS_SCROLL_BONUSES = Map.of(
            ResourceLocation.fromNamespaceAndPath("advancednetherite", "enhance_success_scroll_3"),
            3,
            ResourceLocation.fromNamespaceAndPath("advancednetherite", "enhance_success_scroll_5"),
            5,
            ResourceLocation.fromNamespaceAndPath("advancednetherite", "enhance_success_scroll_7"),
            7,
            ResourceLocation.fromNamespaceAndPath("advancednetherite", "enhance_success_scroll_10"),
            10);

    private EnhancementHelper() {
    }

    public static int getEnhancementLevel(ItemStack itemStack) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        int storedLevel = customData.copyTag().getIntOr(ENHANCEMENT_LEVEL_KEY, 0);
        return Mth.clamp(storedLevel, 0, MAX_ENHANCEMENT_LEVEL);
    }

    public static void setEnhancementLevel(ItemStack itemStack, int enhancementLevel) {
        int clampedLevel = Mth.clamp(enhancementLevel, 0, MAX_ENHANCEMENT_LEVEL);
        CustomData.update(DataComponents.CUSTOM_DATA,
                itemStack,
                (net.minecraft.nbt.CompoundTag tag) -> tag.putInt(ENHANCEMENT_LEVEL_KEY, clampedLevel));
        applyEnhancementModifiers(itemStack, clampedLevel);
    }

    /**
     * 강화 단계에 해당하는 속성 수정자를 다시 계산해 붙인다.
     * <p>
     * 기존 강화 수정자는 전용 식별자로 걸러 내고 새로 넣으므로, 단계가 오르내려도 값이 누적되지 않는다.
     * 다른 출처의 수정자는 그대로 둔다.
     */
    private static void applyEnhancementModifiers(ItemStack itemStack, int enhancementLevel) {
        ItemAttributeModifiers current = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                itemStack.getItem()
                        .components()
                        .getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY));

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            if (!ENHANCEMENT_MODIFIER_ID.equals(entry.modifier().id())) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        if (enhancementLevel > 0) {
            Holder<Attribute> attribute = null;
            EquipmentSlotGroup slotGroup = null;
            double amount = 0.0D;

            if (itemStack.is(ItemTags.SWORDS)) {
                attribute = Attributes.ATTACK_DAMAGE;
                slotGroup = EquipmentSlotGroup.MAINHAND;
                amount = ATTACK_DAMAGE_PER_LEVEL * enhancementLevel;
            } else if (isEnhanceableTool(itemStack)) {
                attribute = Attributes.MINING_EFFICIENCY;
                slotGroup = EquipmentSlotGroup.MAINHAND;
                amount = getMiningEfficiencyBonus(enhancementLevel);
            } else if (isEnhanceableArmor(itemStack)) {
                attribute = Attributes.MAX_HEALTH;
                slotGroup = EquipmentSlotGroup.ARMOR;
                amount = MAX_HEALTH_PER_LEVEL * enhancementLevel;
            }

            if (attribute != null) {
                builder.add(attribute,
                        new AttributeModifier(ENHANCEMENT_MODIFIER_ID, amount, AttributeModifier.Operation.ADD_VALUE),
                        slotGroup);
            }
        }

        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    public static boolean isEnhanceableTool(ItemStack itemStack) {
        return itemStack.is(ItemTags.AXES) || itemStack.is(ItemTags.PICKAXES) || itemStack.is(ItemTags.SHOVELS)
                || itemStack.is(ItemTags.HOES);
    }

    public static boolean isEnhanceableArmor(ItemStack itemStack) {
        return itemStack.is(ItemTags.HEAD_ARMOR) || itemStack.is(ItemTags.CHEST_ARMOR)
                || itemStack.is(ItemTags.LEG_ARMOR) || itemStack.is(ItemTags.FOOT_ARMOR);
    }

    /**
     * 효율 마법은 채굴 속도에 {@code 레벨 제곱 + 1}을 더한다. 강화 단계만큼 효율 레벨이 오른 것과
     * 같은 속도가 되도록, 효율 V 도구를 기준으로 늘어나는 차이만큼을 준다.
     */
    public static double getMiningEfficiencyBonus(int enhancementLevel) {
        int base = REFERENCE_EFFICIENCY_LEVEL;
        int enhanced = base + enhancementLevel;
        return (double) (enhanced * enhanced - base * base);
    }

    public static boolean isEnhanceableEquipment(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ENHANCEABLE_EQUIPMENT);
    }

    public static boolean isEnhancementGem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return ENHANCEMENT_GEM_ID.equals(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
    }

    public static boolean isSuccessScroll(ItemStack itemStack) {
        return getSuccessScrollBonus(itemStack) > 0;
    }

    public static int getSuccessScrollBonus(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        return SUCCESS_SCROLL_BONUSES.getOrDefault(itemId, 0);
    }

    public static boolean isProtectionScroll(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return PROTECTION_SCROLL_ID.equals(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
    }

    /**
     * 1강 100%에서 시작해 한 단계마다 10%씩 낮아져 10강에서 10%가 된다.
     * 성공률 주문서 보너스를 더한 뒤 100%를 넘지 않도록 자른다.
     */
    public static int getSuccessChance(int attemptLevel, int successScrollBonus) {
        int baseChance = 100 - (attemptLevel - 1) * 10;
        return Mth.clamp(baseChance + successScrollBonus, 0, 100);
    }

    /**
     * 3강 시도의 1%에서 시작해 한 단계마다 2%씩 올라가 10강 시도에서 15%가 된다.
     * 1강과 2강 시도에서는 장비가 파괴되지 않는다.
     */
    public static int getBaseDestroyChance(int attemptLevel) {
        if (attemptLevel < DESTROY_CHANCE_START_LEVEL) {
            return 0;
        }
        return (attemptLevel - DESTROY_CHANCE_START_LEVEL) * DESTROY_CHANCE_STEP + DESTROY_CHANCE_AT_START_LEVEL;
    }

    /**
     * 화면에 표시하고 실제 판정에도 사용하는 파괴 확률이다.
     * 파괴 방지권을 넣으면 파괴가 실패로 대체되므로 0%가 된다.
     */
    public static int getEffectiveDestroyChance(int attemptLevel, boolean protectionScrollPresent) {
        if (protectionScrollPresent) {
            return 0;
        }
        return getBaseDestroyChance(attemptLevel);
    }
}

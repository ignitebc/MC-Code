package fuzs.illagerinvasion.common.world.item.enhancement;

import fuzs.illagerinvasion.common.IllagerInvasion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlotGroup;
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
    public static final int DESTROY_CHANCE_START_LEVEL = 1;
    /** 방어구 한 단계당 오르는 최대 체력. 하트 반 칸이 1이다. */
    public static final double MAX_HEALTH_PER_LEVEL = 1.0D;
    /** 도구 한 단계당 오르는 채굴 효율 */
    public static final double MINING_EFFICIENCY_PER_LEVEL = 0.1D;
    /** 무기와 도구의 공격력에 붙는 강화 속성 수정자 식별자 */
    public static final Identifier ENHANCEMENT_MODIFIER_ID = IllagerInvasion.id("enhancement");
    /** 굴착 도구의 채굴 효율에 붙는 강화 속성 수정자 식별자 */
    public static final Identifier ENHANCEMENT_MINING_MODIFIER_ID = IllagerInvasion.id("enhancement_mining");
    /** 방어구 부위별 강화 속성 수정자 식별자 */
    public static final Identifier HELMET_ENHANCEMENT_MODIFIER_ID = IllagerInvasion.id("enhancement_helmet");
    public static final Identifier CHESTPLATE_ENHANCEMENT_MODIFIER_ID = IllagerInvasion.id(
            "enhancement_chestplate");
    public static final Identifier LEGGINGS_ENHANCEMENT_MODIFIER_ID = IllagerInvasion.id("enhancement_leggings");
    public static final Identifier BOOTS_ENHANCEMENT_MODIFIER_ID = IllagerInvasion.id("enhancement_boots");
    /** 강화 대상 장비를 데이터로 조정할 수 있도록 태그로 관리한다. */
    public static final TagKey<Item> ENHANCEABLE_EQUIPMENT = TagKey.create(Registries.ITEM,
            IllagerInvasion.id("enhanceable_equipment"));

    /**
     * 단계별 공격력 증가량. 도구 종류마다 곡선이 다르므로 계산식 대신 표로 둔다.
     * 배열의 n번째 값이 n+1강의 증가량이다.
     */
    private static final int[] SWORD_AXE_ATTACK_BONUS = {1, 2, 3, 4, 5, 7, 9, 11, 13, 15};
    private static final int[] PICKAXE_SHOVEL_ATTACK_BONUS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 12};
    private static final int[] HOE_ATTACK_BONUS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 11};
    /** 해당 단계를 시도할 때의 성공률과 파괴 확률 */
    private static final int[] SUCCESS_CHANCE = {90, 80, 70, 60, 50, 40, 30, 20, 10, 5};
    private static final int[] DESTROY_CHANCE = {1, 2, 3, 5, 7, 9, 11, 15, 25, 30};

    private static final String ENHANCEMENT_LEVEL_KEY = "EnhancementLevel";
    private static final Identifier ENHANCEMENT_GEM_ID = Identifier.fromNamespaceAndPath(
            "advancednetherite",
            "enhancement_gem");
    private static final Identifier PROTECTION_SCROLL_ID = Identifier.fromNamespaceAndPath(
            "advancednetherite",
            "enhance_protection_scroll");
    private static final Map<Identifier, Integer> SUCCESS_SCROLL_BONUSES = Map.of(
            Identifier.fromNamespaceAndPath("advancednetherite", "enhance_success_scroll_3"),
            3,
            Identifier.fromNamespaceAndPath("advancednetherite", "enhance_success_scroll_5"),
            5,
            Identifier.fromNamespaceAndPath("advancednetherite", "enhance_success_scroll_7"),
            7,
            Identifier.fromNamespaceAndPath("advancednetherite", "enhance_success_scroll_10"),
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
            if (!isEnhancementModifier(entry.modifier().id())) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        if (enhancementLevel > 0) {
            if (isEnhanceableArmor(itemStack)) {
                builder.add(Attributes.MAX_HEALTH,
                        new AttributeModifier(getArmorModifierId(itemStack),
                                MAX_HEALTH_PER_LEVEL * enhancementLevel,
                                AttributeModifier.Operation.ADD_VALUE),
                        getArmorSlotGroup(itemStack));
            } else {
                int attackBonus = getAttackDamageBonus(itemStack, enhancementLevel);
                if (attackBonus > 0) {
                    builder.add(Attributes.ATTACK_DAMAGE,
                            new AttributeModifier(ENHANCEMENT_MODIFIER_ID, attackBonus,
                                    AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND);
                }
                if (isEnhanceableTool(itemStack)) {
                    builder.add(Attributes.MINING_EFFICIENCY,
                            new AttributeModifier(ENHANCEMENT_MINING_MODIFIER_ID,
                                    getMiningEfficiencyBonus(enhancementLevel),
                                    AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND);
                }
            }
        }

        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    /**
     * 강화 단계에 해당하는 공격력 증가량.
     *
     * <p>검과 도끼, 곡괭이와 삽, 괭이가 서로 다른 곡선을 쓴다. 강화 대상이 아닌 아이템은 0을 돌려준다.
     */
    public static int getAttackDamageBonus(ItemStack itemStack, int enhancementLevel) {
        int[] bonuses = getAttackBonusTable(itemStack);
        if (bonuses == null || enhancementLevel <= 0) {
            return 0;
        }
        return bonuses[Mth.clamp(enhancementLevel, 1, MAX_ENHANCEMENT_LEVEL) - 1];
    }

    private static int[] getAttackBonusTable(ItemStack itemStack) {
        if (itemStack.is(ItemTags.SWORDS) || itemStack.is(ItemTags.AXES)) {
            return SWORD_AXE_ATTACK_BONUS;
        }
        if (itemStack.is(ItemTags.PICKAXES) || itemStack.is(ItemTags.SHOVELS)) {
            return PICKAXE_SHOVEL_ATTACK_BONUS;
        }
        if (itemStack.is(ItemTags.HOES)) {
            return HOE_ATTACK_BONUS;
        }
        return null;
    }

    public static boolean isEnhanceableTool(ItemStack itemStack) {
        return itemStack.is(ItemTags.AXES) || itemStack.is(ItemTags.PICKAXES) || itemStack.is(ItemTags.SHOVELS)
                || itemStack.is(ItemTags.HOES);
    }

    public static boolean isEnhanceableArmor(ItemStack itemStack) {
        return itemStack.is(ItemTags.HEAD_ARMOR) || itemStack.is(ItemTags.CHEST_ARMOR)
                || itemStack.is(ItemTags.LEG_ARMOR) || itemStack.is(ItemTags.FOOT_ARMOR);
    }

    public static double getMiningEfficiencyBonus(int enhancementLevel) {
        return MINING_EFFICIENCY_PER_LEVEL * enhancementLevel;
    }

    private static boolean isEnhancementModifier(Identifier modifierId) {
        return ENHANCEMENT_MODIFIER_ID.equals(modifierId)
                || ENHANCEMENT_MINING_MODIFIER_ID.equals(modifierId)
                || HELMET_ENHANCEMENT_MODIFIER_ID.equals(modifierId)
                || CHESTPLATE_ENHANCEMENT_MODIFIER_ID.equals(modifierId)
                || LEGGINGS_ENHANCEMENT_MODIFIER_ID.equals(modifierId)
                || BOOTS_ENHANCEMENT_MODIFIER_ID.equals(modifierId);
    }

    private static EquipmentSlotGroup getArmorSlotGroup(ItemStack itemStack) {
        if (itemStack.is(ItemTags.HEAD_ARMOR)) {
            return EquipmentSlotGroup.HEAD;
        }
        if (itemStack.is(ItemTags.CHEST_ARMOR)) {
            return EquipmentSlotGroup.CHEST;
        }
        if (itemStack.is(ItemTags.LEG_ARMOR)) {
            return EquipmentSlotGroup.LEGS;
        }
        return EquipmentSlotGroup.FEET;
    }

    private static Identifier getArmorModifierId(ItemStack itemStack) {
        if (itemStack.is(ItemTags.HEAD_ARMOR)) {
            return HELMET_ENHANCEMENT_MODIFIER_ID;
        }
        if (itemStack.is(ItemTags.CHEST_ARMOR)) {
            return CHESTPLATE_ENHANCEMENT_MODIFIER_ID;
        }
        if (itemStack.is(ItemTags.LEG_ARMOR)) {
            return LEGGINGS_ENHANCEMENT_MODIFIER_ID;
        }
        return BOOTS_ENHANCEMENT_MODIFIER_ID;
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
        Identifier itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        return SUCCESS_SCROLL_BONUSES.getOrDefault(itemId, 0);
    }

    public static boolean isProtectionScroll(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return PROTECTION_SCROLL_ID.equals(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
    }

    /**
     * 단계별 성공률 표를 따른다. 성공률 주문서 보너스를 더한 뒤 100%를 넘지 않도록 자른다.
     */
    public static int getSuccessChance(int attemptLevel, int successScrollBonus) {
        int baseChance = SUCCESS_CHANCE[Mth.clamp(attemptLevel, 1, MAX_ENHANCEMENT_LEVEL) - 1];
        return Mth.clamp(baseChance + successScrollBonus, 0, 100);
    }

    /**
     * 단계별 파괴 확률 표를 따른다. 1강 시도부터 파괴 판정이 있다.
     */
    public static int getBaseDestroyChance(int attemptLevel) {
        if (attemptLevel < DESTROY_CHANCE_START_LEVEL) {
            return 0;
        }
        return DESTROY_CHANCE[Mth.clamp(attemptLevel, 1, MAX_ENHANCEMENT_LEVEL) - 1];
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

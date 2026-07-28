package fuzs.illagerinvasion.world.item.enhancement;

import fuzs.illagerinvasion.IllagerInvasion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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

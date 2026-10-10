package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.api.impl.IArmorMaterial;
import com.autovw.advancednetherite.api.impl.IDurabilityBarColorModifier;
import com.autovw.advancednetherite.common.AdvancedUtil;
import com.autovw.advancednetherite.config.ConfigHelper;
import com.autovw.advancednetherite.core.util.ModTags;
import com.autovw.advancednetherite.core.util.ModTooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;

import java.util.function.Consumer;

/**
 * @author Autovw
 */
public class AdvancedArmorItem extends Item implements IArmorMaterial, IDurabilityBarColorModifier
{
    private static Item.Properties armorProperties(ArmorMaterial material, ArmorType armorType, Properties properties)
    {
        return properties.durability(armorType.getDurability(AdvancedUtil.getArmorDurabilityMultiplier(material)))
                .attributes(material.createAttributes(armorType))
                .enchantable(material.enchantmentValue())
                .component(DataComponents.EQUIPPABLE, Equippable.builder(armorType.getSlot()).setEquipSound(material.equipSound()).setAsset(material.assetId()).build())
                .repairable(material.repairIngredient());
    }

    private final ArmorMaterial material;

    public AdvancedArmorItem(ArmorMaterial material, ArmorType armorType, Properties properties)
    {
        super(armorProperties(material, armorType, properties).fireResistant());
        this.material = material;
    }

    public boolean pacifiesEndermen(ItemStack stack)
    {
        return stack.is(ModTags.PACIFY_ENDERMEN_ARMOR);
    }

    public boolean pacifiesPiglins(ItemStack stack)
    {
        return stack.is(ModTags.PACIFY_PIGLINS_ARMOR);
    }

    public boolean pacifiesPhantoms(ItemStack stack)
    {
        return stack.is(ModTags.PACIFY_PHANTOMS_ARMOR);
    }

    /**
     * 툴팁을 직접 추가하려면 이 메서드를 {@link Override}한다.
     *
     * @param stack     아이템 스택
     * @param context   툴팁 문맥
     * @param tooltips  툴팁 목록
     * @param flag      디버그 모드(F3 + H)에서만 보이는 툴팁인지 판단할 때 쓴다
     */
    public void addTooltips(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag)
    {
    }

    @Override
    public int durabilityBarColorModifier(int originalColor, ItemStack stack)
    {
        return AdvancedUtil.getDurabilityBarColor(originalColor, stack);
    }

    /* ================ 내부용. javadoc에 연결된 대체 메서드를 사용 ================ */

    /**
     * 이 메서드를 재정의하지 말고, 툴팁을 추가하려면 {@link AdvancedArmorItem#addTooltips(ItemStack, TooltipContext, TooltipDisplay, Consumer, TooltipFlag)}를 쓴다.
     */
    @Internal
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        if (ConfigHelper.get().getClient().showTooltips())
        {
            if (Minecraft.getInstance().hasShiftDown())
            {
                if (pacifiesEndermen(stack))
                    tooltip.accept(ModTooltips.ENDERMAN_PASSIVE_TOOLTIP);
                if (pacifiesPiglins(stack))
                    tooltip.accept(ModTooltips.PIGLIN_PASSIVE_TOOLTIP);
                if (pacifiesPhantoms(stack))
                    tooltip.accept(ModTooltips.PHANTOM_PASSIVE_TOOLTIP);
            }
            else
            {
                if (pacifiesEndermen(stack) || pacifiesPiglins(stack) || pacifiesPhantoms(stack))
                    tooltip.accept(ModTooltips.SHIFT_KEY_TOOLTIP);
            }

            // 애드온이 추가한 툴팁을 모두 붙인다
            addTooltips(stack, context, display, tooltip, flag); // 애드온 툴팁 추가
        }
    }

    /**
     * 이 메서드를 재정의하지 말고, 내구도 막대 색을 바꾸려면 {@link AdvancedArmorItem#durabilityBarColorModifier(int, ItemStack)}를 쓴다.
     */
    @Internal
    @Override
    public int getBarColor(ItemStack stack)
    {
        int originalColor = super.getBarColor(stack);
        return ConfigHelper.get().getClient().matchingDurabilityBars() ? this.durabilityBarColorModifier(originalColor, stack) : originalColor;
    }

    @Override
    public ArmorMaterial getMaterial()
    {
        return this.material;
    }
}

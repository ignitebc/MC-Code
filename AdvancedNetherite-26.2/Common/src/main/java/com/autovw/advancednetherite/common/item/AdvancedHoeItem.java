package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.api.impl.IDurabilityBarColorModifier;
import com.autovw.advancednetherite.api.impl.IToolMaterial;
import com.autovw.advancednetherite.common.AdvancedUtil;
import com.autovw.advancednetherite.config.ConfigHelper;
import com.autovw.advancednetherite.core.util.ModTags;
import com.autovw.advancednetherite.core.util.ModTooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * @author Autovw
 */
public class AdvancedHoeItem extends HoeItem implements IToolMaterial, IDurabilityBarColorModifier
{
    private final ToolMaterial material;

    public AdvancedHoeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties)
    {
        super(material, attackDamage, attackSpeed, properties.fireResistant());
        this.material = material;
    }

    /**
     * 툴팁을 직접 추가하려면 이 메서드를 {@link Override}한다.
     *
     * @param stack     아이템 스택
     * @param context     툴팁 문맥
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
     * 이 메서드를 재정의하지 말고, 툴팁을 추가하려면 {@link AdvancedHoeItem#addTooltips(ItemStack, TooltipContext, TooltipDisplay, Consumer, TooltipFlag)}를 쓴다.
     */
    @Internal
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        if (ConfigHelper.get().getClient().showTooltips())
        {
            if (stack.is(ModTags.DROPS_ADDITIONAL_CROPS) && ConfigHelper.get().getCommon().getAdditionalDrops().enableAdditionalCropDrops())
            {
                if (Minecraft.getInstance().hasShiftDown())
                {
                    tooltip.accept(ModTooltips.ADDITIONAL_CROP_DROPS_TOOLTIP);
                }
                else
                {
                    tooltip.accept(ModTooltips.SHIFT_KEY_TOOLTIP);
                }
            }

            addTooltips(stack, context, display, tooltip, flag); // 애드온 툴팁 추가
        }
    }

    /**
     * 이 메서드를 재정의하지 말고, 내구도 막대 색을 바꾸려면 {@link AdvancedHoeItem#durabilityBarColorModifier(int, ItemStack)}를 쓴다.
     */
    @Internal
    @Override
    public int getBarColor(ItemStack stack)
    {
        int originalColor = super.getBarColor(stack);
        return ConfigHelper.get().getClient().matchingDurabilityBars() ? this.durabilityBarColorModifier(originalColor, stack) : originalColor;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        float originalSpeed = super.getDestroySpeed(stack, state);
        return AdvancedUtil.getDestroySpeed(originalSpeed, stack, state);
    }

    @Override
    public ToolMaterial getMaterial()
    {
        return this.material;
    }

    @Override
    public Type getToolType()
    {
        return Type.HOE;
    }
}

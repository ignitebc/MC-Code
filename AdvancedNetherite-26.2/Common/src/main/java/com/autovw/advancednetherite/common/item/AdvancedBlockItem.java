package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.config.ConfigHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * @author Autovw
 */
public class AdvancedBlockItem extends BlockItem
{
    public AdvancedBlockItem(Block block, Properties properties)
    {
        super(block, properties.fireResistant());
    }

    /**
     * 툴팁을 직접 추가하려면 이 메서드를 {@link Override}한다.
     *
     * @param stack 아이템 스택
     * @param context 툴팁 문맥
     * @param tooltip 툴팁 모음
     * @param flag 툴팁 플래그. 디버그 모드(F3 + H)에서만 보이는 툴팁인지 판단할 때 쓴다.
     */
    public void addTooltips(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
    }

    /* ================ 내부용. javadoc에 연결된 대체 메서드를 사용 ================ */

    /**
     * 이 메서드를 재정의하지 말고, 툴팁을 추가하려면 {@link AdvancedBlockItem#addTooltips(ItemStack, TooltipContext, TooltipDisplay, Consumer, TooltipFlag)}를 쓴다.
     */
    @Internal
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        if (ConfigHelper.get().getClient().showTooltips())
        {
            addTooltips(stack, context, display, tooltip, flag);
        }
    }
}

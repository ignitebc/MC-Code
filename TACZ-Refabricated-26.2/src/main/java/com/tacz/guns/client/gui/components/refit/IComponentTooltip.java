package com.tacz.guns.client.gui.components.refit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Consumer;

public interface IComponentTooltip {
    /**
     * 아이템의 안내 문구를 얻는다
     */
    static List<Component> getTooltipFromItem(ItemStack stack) {
        Options options = Minecraft.getInstance().options;
        LocalPlayer player = Minecraft.getInstance().player;
        return stack.getTooltipLines(TooltipContext.EMPTY, player, options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    /**
     * 이 인터페이스를 붙이면 이 메서드로 안내 문구를 그린다
     *
     * @param consumer 그릴 안내 문구
     */
    void renderTooltip(Consumer<List<Component>> consumer);
}

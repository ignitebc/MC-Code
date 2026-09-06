package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Presentation only; the existing sell button and server validation handle exchanges. */
public class ShopDetailsComponent extends EmptyComponent {
    private final JobsScreenState state;

    public ShopDetailsComponent(JobsScreenState state, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.state = state;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight) {
        if (state.getSelectedRightTab() != RightTab.SHOP) {
            return;
        }
        int x = getTotalX();
        int y = getTotalY();
        JobsTheme.panel(graphics, x, y, getWidth(), getHeight());
        JobsTheme.texture(graphics, JobsTheme.Skin.HEADER, x + 1, y + 1, getWidth() - 2, 18);
        JobsTheme.text(graphics, Component.literal("교환 정보"), x + 8, y + 7, getWidth() - 16, JobsTheme.TEXT);
        var offer = state.getSelectedShopOffer();
        if (offer == null) {
            return;
        }
        Item input = BuiltInRegistries.ITEM.get(offer.inputItemId()).map(holder -> holder.value()).orElse(Items.AIR);
        Item output = BuiltInRegistries.ITEM.get(offer.outputItemId()).map(holder -> holder.value()).orElse(Items.AIR);
        boolean spacious = getHeight() >= 225;
        int iconSize = spacious ? 64 : 32;
        int iconX = x + (getWidth() - iconSize) / 2;
        JobsTheme.texture(graphics, JobsTheme.Skin.SLOT, iconX, y + 28, iconSize, iconSize);
        graphics.pose().pushMatrix();
        graphics.pose().translate(iconX + 4, y + 32);
        float scale = (iconSize - 8) / 16.0f;
        graphics.pose().scale(scale, scale);
        graphics.fakeItem(new ItemStack(output), 0, 0);
        graphics.pose().popMatrix();
        JobsTheme.label(graphics, new ItemStack(output).getHoverName().copy().append(" × " + offer.outputAmount()),
                x + 6, y + iconSize + 35, getWidth() - 12, 12, JobsTheme.TEXT);
        int count = 0;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.isEmpty() && stack.getItem() == input) {
                    count += stack.getCount();
                }
            }
        }
        int boxY = y + iconSize + 54;
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, x + 8, boxY, getWidth() - 16, 34);
        JobsTheme.text(graphics, new ItemStack(input).getHoverName(), x + 14, boxY + 5, getWidth() - 28, JobsTheme.MUTED);
        JobsTheme.text(graphics, Component.literal("필요 " + offer.inputAmount() + "  /  보유 " + count),
                x + 14, boxY + 19, getWidth() - 28, JobsTheme.TEXT);
        JobsTheme.label(graphics, Component.literal(count >= offer.inputAmount() ? "재료 충분" : "재료 부족"),
                x + 8, boxY + 41, getWidth() - 16, 12, count >= offer.inputAmount() ? JobsTheme.CYAN : JobsTheme.ERROR);
    }
}

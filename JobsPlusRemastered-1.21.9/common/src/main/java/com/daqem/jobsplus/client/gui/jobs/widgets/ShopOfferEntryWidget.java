package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShopOfferEntryWidget extends CustomButtonWidget
{
    private final JobsScreenState state;
    private final ShopOffer offer;

    // 선택 하이라이트(진한 보라)
    private static final int SELECT_BG = 0xAA6F2DBD;     // 배경(알파 포함)
    private static final int SELECT_BORDER = 0xFFE6CCFF; // 테두리

    public ShopOfferEntryWidget(int x, int y, JobsScreenState state, ShopOffer offer)
    {
        super(x, y, 98, 24, Component.empty(), null, button -> state.setSelectedShopOffer(offer));
        this.state = state;
        this.offer = offer;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean selected = this.offer.equals(this.state.getSelectedShopOffer());

        // 선택 강조: 배경 + 테두리(진하게)
        if (selected)
        {
            guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, SELECT_BORDER);
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), SELECT_BG);
        }

        // 슬롯 2개 + 화살표
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/item_slot_1"),
                this.getX(), this.getY() + 3, 18, 18, ARGB.white(this.alpha));
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/pagination_arrow_right"),
                this.getX() + 40, this.getY() + 7, 10, 10, ARGB.white(this.alpha));
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/item_slot_1"),
                this.getX() + 62, this.getY() + 3, 18, 18, ARGB.white(this.alpha));

        ItemStack inStack = new ItemStack(resolveItem(this.offer.inputItemId()).orElse(Items.AIR), this.offer.inputAmount());
        ItemStack outStack = new ItemStack(resolveItem(this.offer.outputItemId()).orElse(Items.AIR), this.offer.outputAmount());

        guiGraphics.renderItem(inStack, this.getX() + 1, this.getY() + 4);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, inStack, this.getX() + 1, this.getY() + 4);

        guiGraphics.renderItem(outStack, this.getX() + 63, this.getY() + 4);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, outStack, this.getX() + 63, this.getY() + 4);
    }

    private static Optional<Item> resolveItem(@NotNull ResourceLocation id)
    {
        Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.get(id);
        return holder.map(Holder.Reference::value);
    }
}

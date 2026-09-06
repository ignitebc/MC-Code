package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShopOfferEntryWidget extends CustomButtonWidget
{
    private final JobsScreenState state;
    private final ShopOffer offer;

    private static final int HITBOX_SIZE = 14;

    public ShopOfferEntryWidget(int x, int y, int width, JobsScreenState state, ShopOffer offer)
    {
        super(x, y, width, 26, Component.empty(), null, button -> state.setSelectedShopOffer(offer));
        this.state = state;
        this.offer = offer;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean selected = this.offer.equals(this.state.getSelectedShopOffer());

        JobsTheme.button(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                this.active, isHoveredOrFocused(), false, false);
        if (selected)
        {
            JobsTheme.cutBox(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                    JobsTheme.SELECTED, JobsTheme.CYAN);
            guiGraphics.fill(getX(), getY() + 3, getX() + 2, getY() + getHeight() - 3, JobsTheme.CYAN);
        }
        int inputSlotX = getX() + 4;
        int half = (getWidth() - 16) / 2;
        int outputSlotX = getX() + half + 12;
        int slotY = getY() + 6;
        JobsTheme.sprite(guiGraphics, JobsPlus.getId("jobs/item_slot_1"), inputSlotX, slotY, 14, 14);
        JobsTheme.sprite(guiGraphics, JobsPlus.getId("jobs/item_slot_1"), outputSlotX, slotY, 14, 14);
        JobsTheme.label(guiGraphics, Component.literal("→"), getX() + half + 3, getY(), 9, getHeight(), JobsTheme.CYAN);
        ItemStack inStack = new ItemStack(resolveItem(this.offer.inputItemId()).orElse(Items.AIR), this.offer.inputAmount());
        ItemStack outStack = new ItemStack(resolveItem(this.offer.outputItemId()).orElse(Items.AIR), this.offer.outputAmount());
        drawSmallItem(guiGraphics, inStack, inputSlotX + 1, slotY + 1);
        drawSmallItem(guiGraphics, outStack, outputSlotX + 1, slotY + 1);
        drawFittedText(guiGraphics, inStack.getHoverName().copy().append(" ×" + offer.inputAmount()),
                inputSlotX + 16, getY(), half - 17, getHeight(), JobsTheme.MUTED);
        drawFittedText(guiGraphics, outStack.getHoverName().copy().append(" ×" + offer.outputAmount()),
                outputSlotX + 16, getY(), getX() + getWidth() - outputSlotX - 20, getHeight(), JobsTheme.TEXT);

        // ====== 중요: 여기서는 툴팁을 직접 그리지 않는다 ======
        // 스크롤 컨테이너가 Scissor를 쓰고 있어서, 여기서 툴팁/disableScissor를 만지면 underflow로 터진다.
        // 따라서 "이름만" ShopTooltipState에 기록하고,
        // 실제 박스/표시는 JobsScreen.extractRenderState(...) 마지막에서 그린다.
        if (!inStack.isEmpty() && isMouseOverSlot(mouseX, mouseY, inputSlotX, slotY))
        {
            ShopTooltipState.setHoveredName(inStack.getHoverName(), mouseX, mouseY);
        }
        else if (!outStack.isEmpty() && isMouseOverSlot(mouseX, mouseY, outputSlotX, slotY))
        {
            ShopTooltipState.setHoveredName(outStack.getHoverName(), mouseX, mouseY);
        }
    }

    private static void drawSmallItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(0.75f, 0.75f);
        graphics.item(stack, 0, 0);
        graphics.pose().popMatrix();
    }

    private static void drawFittedText(GuiGraphicsExtractor graphics, Component text, int x, int y,
                                       int width, int height, int color) {
        var font = Minecraft.getInstance().font;
        float scale = Math.min(0.65f, Math.max(1, width) / (float) Math.max(1, font.width(text)));
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y + (height - font.lineHeight * scale) / 2);
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, 0, 0, color, false);
        graphics.pose().popMatrix();
    }

    /**
     * Holder/Optional 기반 레지스트리 조회
     */
    private static Optional<Item> resolveItem(@NotNull Identifier id)
    {
        Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.get(id);
        return holder.map(Holder.Reference::value);
    }

    /**
     * 슬롯(14x14) 기준 마우스 오버 체크
     * - 슬롯 스프라이트 좌표를 넣어야 한다 (아이콘 좌표(inX,inY)가 아니라 슬롯 좌표)
     */
    private boolean isMouseOverSlot(int mouseX, int mouseY, int slotX, int slotY)
    {
        return mouseX >= slotX && mouseX < slotX + HITBOX_SIZE
            && mouseY >= slotY && mouseY < slotY + HITBOX_SIZE;
    }
}

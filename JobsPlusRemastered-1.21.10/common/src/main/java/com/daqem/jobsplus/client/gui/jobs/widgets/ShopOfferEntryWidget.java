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

    // 아이템 렌더 위치 (기존 UI 기준)
    private static final int IN_ITEM_X_OFFSET = 1;
    private static final int OUT_ITEM_X_OFFSET = 63;
    private static final int ITEM_Y_OFFSET = 4;

    // 슬롯/아이콘 히트박스 (슬롯 스프라이트가 18x18)
    private static final int HITBOX_SIZE = 18;

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
            guiGraphics.fill(this.getX() - 1, this.getY() - 1,
                this.getX() + this.getWidth() + 1, this.getY() + this.getHeight() + 1, SELECT_BORDER);
            guiGraphics.fill(this.getX(), this.getY(),
                this.getX() + this.getWidth(), this.getY() + this.getHeight(), SELECT_BG);
        }

        // 슬롯 2개 + 화살표
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
            JobsPlus.getId("jobs/item_slot_1"),
            this.getX(), this.getY() + 3, 18, 18, ARGB.white(this.alpha));

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
            JobsPlus.getId("jobs/pagination_arrow_right"),
            this.getX() + 30, this.getY() + 7, 10, 10, ARGB.white(this.alpha));

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
            JobsPlus.getId("jobs/item_slot_1"),
            this.getX() + 62, this.getY() + 3, 18, 18, ARGB.white(this.alpha));

        // 입력/출력 아이템 스택
        ItemStack inStack = new ItemStack(resolveItem(this.offer.inputItemId()).orElse(Items.AIR), this.offer.inputAmount());
        ItemStack outStack = new ItemStack(resolveItem(this.offer.outputItemId()).orElse(Items.AIR), this.offer.outputAmount());

        // 아이템 렌더링 좌표(아이콘 기준)
        int inX = this.getX() + IN_ITEM_X_OFFSET;
        int inY = this.getY() + ITEM_Y_OFFSET;

        int outX = this.getX() + OUT_ITEM_X_OFFSET;
        int outY = this.getY() + ITEM_Y_OFFSET;

        // 아이템 렌더링
        guiGraphics.renderItem(inStack, inX, inY);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, inStack, inX, inY);

        guiGraphics.renderItem(outStack, outX, outY);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, outStack, outX, outY);

        // ====== 중요: 여기서는 툴팁을 직접 그리지 않는다 ======
        // 스크롤 컨테이너가 Scissor를 쓰고 있어서, 여기서 툴팁/disableScissor를 만지면 underflow로 터진다.
        // 따라서 "이름만" ShopTooltipState에 기록하고,
        // 실제 박스/표시는 JobsScreen.render(...) 마지막에서 그린다.
        if (!inStack.isEmpty() && isMouseOverSlot(mouseX, mouseY, this.getX(), this.getY() + 3))
        {
            ShopTooltipState.setHoveredName(inStack.getHoverName(), mouseX, mouseY);
        }
        else if (!outStack.isEmpty() && isMouseOverSlot(mouseX, mouseY, this.getX() + 62, this.getY() + 3))
        {
            ShopTooltipState.setHoveredName(outStack.getHoverName(), mouseX, mouseY);
        }
    }

    /**
     * Holder/Optional 기반 레지스트리 조회
     */
    private static Optional<Item> resolveItem(@NotNull ResourceLocation id)
    {
        Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.get(id);
        return holder.map(Holder.Reference::value);
    }

    /**
     * 슬롯(18x18) 기준 마우스 오버 체크
     * - 슬롯 스프라이트 좌표를 넣어야 한다 (아이콘 좌표(inX,inY)가 아니라 슬롯 좌표)
     */
    private boolean isMouseOverSlot(int mouseX, int mouseY, int slotX, int slotY)
    {
        return mouseX >= slotX && mouseX < slotX + HITBOX_SIZE
            && mouseY >= slotY && mouseY < slotY + HITBOX_SIZE;
    }
}

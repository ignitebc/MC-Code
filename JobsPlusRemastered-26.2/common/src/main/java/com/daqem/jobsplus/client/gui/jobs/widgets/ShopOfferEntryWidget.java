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

    private static final int HITBOX_SIZE = 18;

    public ShopOfferEntryWidget(int x, int y, int width, JobsScreenState state, ShopOffer offer)
    {
        super(x, y, width, 46, Component.empty(), null, button -> state.setSelectedShopOffer(offer));
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
        int inputSlotX = getX() + 5;
        int outputSlotX = getX() + 5;
        JobsTheme.sprite(guiGraphics, JobsPlus.getId("jobs/item_slot_1"), inputSlotX, getY() + 3, 18, 18);
        JobsTheme.label(guiGraphics, Component.literal("→"), getX() + getWidth() - 13, getY(), 8, getHeight(), JobsTheme.CYAN);
        JobsTheme.sprite(guiGraphics, JobsPlus.getId("jobs/item_slot_1"), outputSlotX, getY() + 24, 18, 18);

        // 입력/출력 아이템 스택
        ItemStack inStack = new ItemStack(resolveItem(this.offer.inputItemId()).orElse(Items.AIR), this.offer.inputAmount());
        ItemStack outStack = new ItemStack(resolveItem(this.offer.outputItemId()).orElse(Items.AIR), this.offer.outputAmount());

        // 아이템 렌더링 좌표(아이콘 기준)
        int inX = inputSlotX + 1;
        int inY = this.getY() + 4;

        int outX = outputSlotX + 1;
        int outY = this.getY() + 25;

        // 아이템 렌더링
        guiGraphics.item(inStack, inX, inY);


        guiGraphics.item(outStack, outX, outY);
        int textWidth = Math.max(1, getWidth() - 40);
        JobsTheme.text(guiGraphics, inStack.getHoverName(), inputSlotX + 21, getY() + 5, textWidth, JobsTheme.TEXT);
        JobsTheme.text(guiGraphics, Component.literal("필요 × " + offer.inputAmount()), inputSlotX + 21, getY() + 15, textWidth, JobsTheme.MUTED);
        JobsTheme.text(guiGraphics, outStack.getHoverName(), outputSlotX + 21, getY() + 26, textWidth, JobsTheme.TEXT);
        JobsTheme.text(guiGraphics, Component.literal("× " + offer.outputAmount()), outputSlotX + 21, getY() + 36, textWidth, JobsTheme.MUTED);

        // ====== 중요: 여기서는 툴팁을 직접 그리지 않는다 ======
        // 스크롤 컨테이너가 Scissor를 쓰고 있어서, 여기서 툴팁/disableScissor를 만지면 underflow로 터진다.
        // 따라서 "이름만" ShopTooltipState에 기록하고,
        // 실제 박스/표시는 JobsScreen.extractRenderState(...) 마지막에서 그린다.
        if (!inStack.isEmpty() && isMouseOverSlot(mouseX, mouseY, inputSlotX, this.getY() + 3))
        {
            ShopTooltipState.setHoveredName(inStack.getHoverName(), mouseX, mouseY);
        }
        else if (!outStack.isEmpty() && isMouseOverSlot(mouseX, mouseY, outputSlotX, this.getY() + 24))
        {
            ShopTooltipState.setHoveredName(outStack.getHoverName(), mouseX, mouseY);
        }
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
     * 슬롯(18x18) 기준 마우스 오버 체크
     * - 슬롯 스프라이트 좌표를 넣어야 한다 (아이콘 좌표(inX,inY)가 아니라 슬롯 좌표)
     */
    private boolean isMouseOverSlot(int mouseX, int mouseY, int slotX, int slotY)
    {
        return mouseX >= slotX && mouseX < slotX + HITBOX_SIZE
            && mouseY >= slotY && mouseY < slotY + HITBOX_SIZE;
    }
}

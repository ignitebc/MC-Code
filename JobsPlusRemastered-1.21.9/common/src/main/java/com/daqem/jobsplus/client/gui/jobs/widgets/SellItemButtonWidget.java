package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.networking.c2s.ServerboundSellItemPacket;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;

public class SellItemButtonWidget extends CustomButtonWidget
{

    private final JobsScreenState state;
    private final Item item;
    private final int requiredAmount;
    private final int rewardCoins;

    public SellItemButtonWidget(int x, int y, JobsScreenState state, Item item, int requiredAmount, int rewardCoins)
    {
        super(x, y, 98, 20, JobsPlus.translatable("gui.jobs.shop.sell", requiredAmount, item.getName(new net.minecraft.world.item.ItemStack(item)), rewardCoins), null, button -> {
            // 클릭 시 서버로 판매 요청 패킷 전송
            NetworkManager.sendToServer(new ServerboundSellItemPacket(item, requiredAmount));
        });
        this.state = state;
        this.item = item;
        this.requiredAmount = requiredAmount;
        this.rewardCoins = rewardCoins;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        // 버튼 배경 렌더링
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/tab_bottom"), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
        
        // 버튼 텍스트 렌더링
        Component message = this.getMessage();
        int textWidth = Minecraft.getInstance().font.width(message);
        int textX = this.getX() + (this.getWidth() - textWidth) / 2;
        int textY = this.getY() + (this.getHeight() - 9) / 2;
        int textColor = isHoveredOrFocused() ? 0xFF1E1410 : 0xFF1E1410;
        guiGraphics.drawString(Minecraft.getInstance().font, message, textX, textY, ARGB.color(this.alpha, textColor), false);
    }
}


package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.networking.c2s.ServerboundSellItemPacket;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

/**
 * (호환용) 판매 버튼 위젯
 *
 * 기존 코드에서 SellItemButtonWidget을 참조하는 곳이 남아있어도
 * ShopOffer 기반 패킷 구조에 맞게 컴파일/동작하도록 수정.
 *
 * 실제 판매 대상은 JobsScreenState.selectedShopOffer 사용.
 */
public class SellItemButtonWidget extends CustomButtonWidget
{
    private final JobsScreenState state;

    public SellItemButtonWidget(int x, int y, JobsScreenState state)
    {
        super(
                x, y, 98, 20,
                JobsPlus.translatable("gui.jobs.shop.sell_button"),
                null,
                button -> {
                    @Nullable ShopOffer offer = state.getSelectedShopOffer();
                    if (offer != null)
                    {
                        NetworkManager.sendToServer(new ServerboundSellItemPacket(offer));
                    }
                }
        );

        this.state = state;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        // 선택된 상품이 없으면 비활성
        this.active = (state.getSelectedShopOffer() != null);

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                JobsPlus.getId("jobs/tab_bottom"),
                this.getX(), this.getY(),
                this.getWidth(), this.getHeight(),
                ARGB.white(this.alpha)
        );

        Component message = this.getMessage();
        int textWidth = Minecraft.getInstance().font.width(message);
        int textX = this.getX() + (this.getWidth() - textWidth) / 2;
        int textY = this.getY() + (this.getHeight() - 9) / 2;
        int textColor = this.active ? 0xFF1E1410 : 0xFF6B5C53;

        guiGraphics.drawString(Minecraft.getInstance().font, message, textX, textY, textColor, false);
    }
}

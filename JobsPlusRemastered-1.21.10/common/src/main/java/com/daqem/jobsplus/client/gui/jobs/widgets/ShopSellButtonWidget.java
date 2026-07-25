package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
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
 * SHOP 탭 전용 '판매' 버튼
 *
 * - 버튼 크기는 생성 시 전달받은 width/height를 그대로 사용(스킬 버튼과 동일하게 맞추기 위함)
 * - SHOP 탭이 아닐 때는 그리지 않고(active=false) 클릭도 막는다.
 */
public class ShopSellButtonWidget extends CustomButtonWidget
{
    private final JobsScreenState state;

    public ShopSellButtonWidget(int x, int y, int width, int height, JobsScreenState state)
    {
        super(
                x, y, width, height,
                JobsPlus.translatable("gui.jobs.shop.sell_button"),
                null,
                button -> {
                    // SHOP 탭에서만 동작
                    if (state.getSelectedRightTab() != RightTab.SHOP)
                    {
                        return;
                    }

                    @Nullable ShopOffer offer = state.getSelectedShopOffer();
                    if (offer != null)
                    {
                        NetworkManager.sendToServer(new ServerboundSellItemPacket(offer));
                    }
                }
        );

        this.state = state;
        this.visible = true; // visible은 건드리지 않고 렌더에서 제어
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean isShop = (this.state.getSelectedRightTab() == RightTab.SHOP);
        boolean enabled = isShop && (this.state.getSelectedShopOffer() != null);

        this.active = enabled;

        // SHOP 탭이 아닐 때는 아예 렌더하지 않음(겹침/오작동 방지)
        if (!isShop)
        {
            return;
        }

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

        int textColor = enabled ? 0xFF1E1410 : 0xFF6B5C53;
        guiGraphics.drawString(Minecraft.getInstance().font, message, textX, textY, textColor, false);
    }
}

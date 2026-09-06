package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.networking.c2s.ServerboundSellItemPacket;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
                x, y, 98, JobsTheme.BUTTON_HEIGHT,
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
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.active = (state.getSelectedShopOffer() != null);
        JobsTheme.button(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                this.active, isHoveredOrFocused(), false, true);
        JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                this.active ? JobsTheme.TEXT : JobsTheme.DISABLED);
    }
}

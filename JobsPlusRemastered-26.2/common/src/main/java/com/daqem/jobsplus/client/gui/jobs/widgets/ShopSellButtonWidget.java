package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.networking.c2s.ServerboundSellItemPacket;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean isShop = (this.state.getSelectedRightTab() == RightTab.SHOP);
        boolean enabled = isShop && (this.state.getSelectedShopOffer() != null);
        this.active = enabled;
        if (!isShop)
        {
            return;
        }
        JobsTheme.button(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                enabled, isHoveredOrFocused(), false, true);
        JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                enabled ? JobsTheme.TEXT : JobsTheme.DISABLED);
    }
}

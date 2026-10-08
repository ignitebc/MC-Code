package com.daqem.jobsplus.client.gui.powerups.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.powerups.tab.PowerupTab;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.networking.c2s.ServerboundStartAllPowerupsPacket;
import com.daqem.jobsplus.player.job.powerup.PowerupAvailability;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** 현재 레벨에서 해금된 모든 미습득 일반스킬을 한 번에 구매하는 버튼. */
public class StartAllPowerupsButtonWidget extends CustomButtonWidget
{
    private static final Component BUTTON_MESSAGE = Component.literal("모든 일반스킬 찍기");
    private static final Component QUESTION_MESSAGE =
            Component.literal("해방된 일반스킬을 모두 찍으시겠습니까?");
    private static final Component YES_MESSAGE = Component.literal("네");
    private static final Component NO_MESSAGE = Component.literal("아니오");

    private final PowerupsScreenState state;

    public StartAllPowerupsButtonWidget(PowerupsScreenState state, int x, int y)
    {
        super(x, y, getButtonWidth(), JobsTheme.BUTTON_HEIGHT, BUTTON_MESSAGE, null,
                button -> ((StartAllPowerupsButtonWidget) button).openConfirmation());
        this.state = state;
    }

    public static int getButtonWidth()
    {
        Font font = Minecraft.getInstance().font;
        return font.width(BUTTON_MESSAGE) + 14;
    }

    private void openConfirmation()
    {
        if (this.state.getSelectedTab() != PowerupTab.NORMAL)
        {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Screen skillScreen = minecraft.gui.screen();
        if (skillScreen == null)
        {
            return;
        }

        List<PowerupInstance> powerups =
                PowerupAvailability.getBatchUnlockablePowerups(this.state.getJob());
        if (powerups.isEmpty())
        {
            openAlert(minecraft, skillScreen,
                    JobsPlus.translatable("gui.powerups.no_available_powerups"));
            return;
        }

        long totalPrice = PowerupAvailability.getTotalPrice(powerups);
        if (totalPrice > this.state.getCoins())
        {
            openAlert(minecraft, skillScreen,
                    JobsPlus.translatable("gui.powerups.not_enough_coins_for_all"));
            return;
        }

        ConfirmationScreenState confirmationState = new ConfirmationScreenState(
                QUESTION_MESSAGE,
                YES_MESSAGE,
                NO_MESSAGE,
                () -> NetworkManager.sendToServer(new ServerboundStartAllPowerupsPacket(
                        this.state.getJob().getJobInstance().getLocation()))
        );
        minecraft.gui.setScreen(new ConfirmationScreen(skillScreen, confirmationState));
    }

    private static void openAlert(Minecraft minecraft, Screen skillScreen, Component message)
    {
        ConfirmationScreenState alertState = ConfirmationScreenState.alert(
                message,
                JobsPlus.translatable("gui.confirmation.ok")
        );
        minecraft.gui.setScreen(new ConfirmationScreen(skillScreen, alertState));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        this.active = this.state.getSelectedTab() == PowerupTab.NORMAL;
        JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(),
                this.active, isHoveredOrFocused(), false, false);
        JobsTheme.label(graphics, BUTTON_MESSAGE, getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
    }
}

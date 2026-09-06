package com.daqem.jobsplus.client.gui.confimation.componentns;

import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.confimation.widgets.ConfirmationButtonWidget;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ConfirmationComponent extends EmptyComponent
{
    private final boolean alert;

    public ConfirmationComponent(ConfirmationScreenState state)
    {
        super(0, 0, 0, 0);
        this.alert = state.isAlert();

        ConfirmationTextComponent confirmationTextComponent = new ConfirmationTextComponent(state);
        int buttonWidth = 56;
        int buttonY = confirmationTextComponent.getHeight() + 8;

        ConfirmationButtonWidget cancelButton = new ConfirmationButtonWidget(
                confirmationTextComponent.getWidth() / 2 + 3,
                buttonY,
                buttonWidth,
                JobsTheme.BUTTON_HEIGHT,
                state.getCancelButtonMessage(),
                false,
                button ->
        {
            assert Minecraft.getInstance().gui.screen() != null;
            Minecraft.getInstance().gui.screen().onClose();
        });
        ConfirmationButtonWidget confirmButton = new ConfirmationButtonWidget(
                state.isAlert()
                        ? confirmationTextComponent.getWidth() / 2 - buttonWidth / 2
                        : confirmationTextComponent.getWidth() / 2 - buttonWidth - 3,
                buttonY,
                buttonWidth,
                JobsTheme.BUTTON_HEIGHT,
                state.getConfirmButtonMessage(),
                true,
                button -> {
                    if (state.isAlert())
                    {
                        assert Minecraft.getInstance().gui.screen() != null;
                        Minecraft.getInstance().gui.screen().onClose();
                        return;
                    }
                    state.getOnConfirm().onConfirm();
                });

        this.setWidth(confirmationTextComponent.getWidth());
        this.setHeight(buttonY + confirmButton.getHeight() + 10);

        this.addComponent(confirmationTextComponent);
        this.addWidget(confirmButton);
        if (!state.isAlert())
        {
            this.addWidget(cancelButton);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        JobsTheme.panel(graphics, getTotalX(), getTotalY(), getWidth(), getHeight());
        JobsTheme.text(graphics, Component.literal(this.alert ? "알림" : "확인"),
                getTotalX() + 14, getTotalY() + 9, getWidth() - 28, JobsTheme.CYAN);
        graphics.fill(getTotalX() + 14, getTotalY() + getHeight() - 29,
                getTotalX() + getWidth() - 14, getTotalY() + getHeight() - 28, JobsTheme.DIVIDER);
    }
}

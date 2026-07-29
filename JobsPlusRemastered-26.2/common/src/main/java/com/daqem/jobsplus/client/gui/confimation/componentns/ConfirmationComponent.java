package com.daqem.jobsplus.client.gui.confimation.componentns;

import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.confimation.widgets.ConfirmationButtonWidget;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.Minecraft;

public class ConfirmationComponent extends EmptyComponent
{

    public ConfirmationComponent(ConfirmationScreenState state)
    {
        super(0, 0, 0, 0);

        ConfirmationTextComponent confirmationTextComponent = new ConfirmationTextComponent(state);
        int buttonWidth = 50;

        ConfirmationButtonWidget cancelButton = new ConfirmationButtonWidget(
                confirmationTextComponent.getWidth() / 2 + 3,
                confirmationTextComponent.getHeight(),
                buttonWidth,
                30,
                state.getCancelButtonMessage(),
                button ->
        {
            assert Minecraft.getInstance().gui.screen() != null;
            Minecraft.getInstance().gui.screen().onClose();
        });
        ConfirmationButtonWidget confirmButton = new ConfirmationButtonWidget(
                state.isAlert()
                        ? confirmationTextComponent.getWidth() / 2 - buttonWidth / 2
                        : confirmationTextComponent.getWidth() / 2 - buttonWidth - 3,
                confirmationTextComponent.getHeight(),
                buttonWidth,
                30,
                state.getConfirmButtonMessage(),
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
        this.setHeight(confirmationTextComponent.getHeight() + confirmButton.getHeight() + 5);

        this.addComponent(confirmationTextComponent);
        this.addWidget(confirmButton);
        if (!state.isAlert())
        {
            this.addWidget(cancelButton);
        }
    }
}

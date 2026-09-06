package com.daqem.jobsplus.client.gui.confimation.componentns;

import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.client.Minecraft;

public class ConfirmationTextComponent extends EmptyComponent
{

    public ConfirmationTextComponent(ConfirmationScreenState state)
    {
        super(0, 0, 0, 0);

        int borderHorizontal = 14;
        int textY = 27;

        int maxWidth = Math.min(260, Minecraft.getInstance().getWindow().getGuiScaledWidth() - 48);
        int minWidth = Math.min(160, maxWidth);
        int textWidth = Math.clamp(Minecraft.getInstance().font.width(state.getMessage()), minWidth, maxWidth);
        MultiLineTextComponent textComponent = new MultiLineTextComponent(borderHorizontal, textY, textWidth, state.getMessage(), JobsTheme.TEXT);

        this.setWidth(Math.max(textComponent.getWidth(), minWidth) + borderHorizontal * 2);
        this.setHeight(textY + textComponent.getHeight() + 8);
        this.addComponent(textComponent);
    }
}

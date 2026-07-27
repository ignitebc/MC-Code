package com.daqem.jobsplus.client.gui.confimation;

import net.minecraft.network.chat.Component;

public class ConfirmationScreenState
{

    private final Component message;
    private final Component confirmButtonMessage;
    private final Component cancelButtonMessage;
    private final OnConfirm onConfirm;
    private final boolean alert;

    public ConfirmationScreenState(Component message, OnConfirm onConfirm)
    {
        this(message, Component.translatable("jobsplus.gui.confirmation.yes"),
                Component.translatable("jobsplus.gui.confirmation.cancel"), onConfirm, false);
    }

    public ConfirmationScreenState(Component message, Component confirmButtonMessage,
                                   Component cancelButtonMessage, OnConfirm onConfirm)
    {
        this(message, confirmButtonMessage, cancelButtonMessage, onConfirm, false);
    }

    private ConfirmationScreenState(Component message, Component confirmButtonMessage,
                                    Component cancelButtonMessage, OnConfirm onConfirm, boolean alert)
    {
        this.message = message;
        this.confirmButtonMessage = confirmButtonMessage;
        this.cancelButtonMessage = cancelButtonMessage;
        this.onConfirm = onConfirm;
        this.alert = alert;
    }

    public static ConfirmationScreenState alert(Component message)
    {
        return alert(message, Component.literal("닫기"));
    }

    public static ConfirmationScreenState alert(Component message, Component buttonMessage)
    {
        return new ConfirmationScreenState(message, buttonMessage,
                Component.empty(), () -> {}, true);
    }

    public Component getMessage()
    {
        return message;
    }

    public OnConfirm getOnConfirm()
    {
        return onConfirm;
    }

    public Component getConfirmButtonMessage()
    {
        return confirmButtonMessage;
    }

    public Component getCancelButtonMessage()
    {
        return cancelButtonMessage;
    }

    public boolean isAlert()
    {
        return alert;
    }
}

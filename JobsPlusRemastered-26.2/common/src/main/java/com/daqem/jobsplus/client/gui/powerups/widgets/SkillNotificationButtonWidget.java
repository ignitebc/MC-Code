package com.daqem.jobsplus.client.gui.powerups.widgets;

import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.notification.ClientSkillNotifications;
import com.daqem.jobsplus.networking.c2s.ServerboundSetSkillNotificationsPacket;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 스킬 발동 채팅 알림을 끄고 켜는 버튼.
 * <p>
 * 누르면 바로 바꾸지 않고 확인 창을 띄워 "네"를 눌렀을 때만 서버에 저장한다.
 */
public class SkillNotificationButtonWidget extends CustomButtonWidget
{
    private static final Component ENABLED_MESSAGE = Component.literal("스킬 알림 켜짐");
    private static final Component DISABLED_MESSAGE = Component.literal("스킬 알림 꺼짐");
    private static final Component DISABLE_QUESTION = Component.literal("스킬 알림을 끄시겠습니까?");
    private static final Component ENABLE_QUESTION = Component.literal("스킬 알림을 켜시겠습니까?");
    private static final Component YES_MESSAGE = Component.literal("네");
    private static final Component NO_MESSAGE = Component.literal("아니오");

    public SkillNotificationButtonWidget(int x, int y)
    {
        super(x, y, getButtonWidth(), JobsTheme.BUTTON_HEIGHT, ENABLED_MESSAGE, null,
                button -> openConfirmation());
    }

    /** 두 문구 중 긴 쪽에 맞춰 너비를 고정해 상태가 바뀌어도 버튼 위치가 움직이지 않게 한다. */
    public static int getButtonWidth()
    {
        Font font = Minecraft.getInstance().font;
        int textWidth = Math.max(font.width(ENABLED_MESSAGE), font.width(DISABLED_MESSAGE));
        return textWidth + 14;
    }

    private static void openConfirmation()
    {
        Minecraft minecraft = Minecraft.getInstance();
        Screen skillScreen = minecraft.gui.screen();
        boolean enabledNow = ClientSkillNotifications.isEnabled();
        boolean requestedEnabled = !enabledNow;

        Component question = ENABLE_QUESTION;
        if (enabledNow)
        {
            question = DISABLE_QUESTION;
        }

        ConfirmationScreenState confirmationState = new ConfirmationScreenState(question, YES_MESSAGE, NO_MESSAGE,
                () -> applySetting(minecraft, skillScreen, requestedEnabled));
        minecraft.gui.setScreen(new ConfirmationScreen(skillScreen, confirmationState));
    }

    private static void applySetting(Minecraft minecraft, Screen skillScreen, boolean requestedEnabled)
    {
        // 서버 응답을 기다리지 않고 버튼 문구를 먼저 바꾼다. 서버가 저장한 값이 다시 오면 그 값으로 맞춰진다.
        ClientSkillNotifications.setEnabled(requestedEnabled);
        NetworkManager.sendToServer(new ServerboundSetSkillNotificationsPacket(requestedEnabled));
        minecraft.gui.setScreen(skillScreen);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean enabled = ClientSkillNotifications.isEnabled();
        Component message = DISABLED_MESSAGE;
        int textColor = JobsTheme.MUTED;
        if (enabled)
        {
            message = ENABLED_MESSAGE;
            textColor = JobsTheme.TEXT;
        }

        JobsTheme.button(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                this.active, isHoveredOrFocused(), false, enabled);
        JobsTheme.label(guiGraphics, message, getX(), getY(), getWidth(), getHeight(), textColor);
    }
}

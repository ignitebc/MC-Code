package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.confimation.PendingJobSelectionAlert;
import com.daqem.jobsplus.networking.s2c.ClientboundAlertPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ClientboundAlertPacketHandler
{
    public static void handleClientSide(ClientboundAlertPacket packet, NetworkManager.PacketContext context)
    {
        // 서버가 알림을 보냈다면 직업 선택은 실패했거나 이미 결과가 나왔다.
        PendingJobSelectionAlert.clear();

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmationScreen(
                findBackgroundScreen(minecraft.gui.screen()),
                ConfirmationScreenState.alert(packet.getMessage(), packet.getButtonMessage())
        ));
    }

    /**
     * 알림을 띄울 바탕 화면을 고른다.
     * <p>
     * 알림은 대부분 확인 창에서 보낸 요청의 결과다. 그 확인 창 위에 알림을 겹쳐 두면
     * 알림을 닫았을 때 이미 처리된 확인 창이 다시 나타나므로, 확인 창은 모두 걷어내고
     * 그 아래의 직업 화면을 바탕으로 삼는다.
     */
    private static Screen findBackgroundScreen(@Nullable Screen currentScreen)
    {
        Screen backgroundScreen = currentScreen;
        while (backgroundScreen instanceof ConfirmationScreen confirmationScreen)
        {
            backgroundScreen = confirmationScreen.getPreviousScreen();
        }
        return backgroundScreen;
    }
}

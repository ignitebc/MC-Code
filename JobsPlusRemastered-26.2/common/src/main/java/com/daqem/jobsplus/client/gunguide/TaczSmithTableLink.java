package com.daqem.jobsplus.client.gunguide;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.gunguide.GunGuidePanel;
import dev.architectury.platform.Platform;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Function;

/**
 * TACZ 총기 작업대에 "총기 도감" 탭을 연결한다.
 *
 * <p>TACZ는 선택적 연동이라 빌드 의존성을 두지 않고 반사 호출로 등록한다. 작업대 쪽은 도감 칸을 만드는
 * 함수만 받아 두었다가 탭을 누르면 불러 본문 자리에 끼우므로, Jobs+가 없으면 탭 자체가 생기지 않는다.
 */
public final class TaczSmithTableLink
{
    private static final String SMITH_TABLE_SCREEN = "com.tacz.guns.client.gui.GunSmithTableScreen";

    private TaczSmithTableLink()
    {
    }

    public static void register()
    {
        if (!Platform.isModLoaded("tacz"))
        {
            return;
        }
        try
        {
            Function<ScreenRectangle, GunGuidePanel> factory = GunGuidePanel::new;
            Class.forName(SMITH_TABLE_SCREEN).getMethod("setGuidePanelFactory", Function.class).invoke(null, factory);
        }
        catch (ReflectiveOperationException | RuntimeException | LinkageError exception)
        {
            // 이 저장소의 TACZ가 아니면 훅이 없다. 작업대에 탭이 안 생길 뿐 다른 기능에는 영향이 없다.
            JobsPlus.LOGGER.warn("Could not link the gun guide to the TACZ gun smith table.", exception);
        }
    }
}

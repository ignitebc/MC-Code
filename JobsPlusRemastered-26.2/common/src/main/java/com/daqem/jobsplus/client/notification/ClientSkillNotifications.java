package com.daqem.jobsplus.client.notification;

/**
 * 서버가 알려 준 스킬 알림 설정을 보관하는 클라이언트 전용 저장소.
 * <p>
 * 실제로 알림을 막는 판단은 서버가 한다. 이 값은 스킬 화면 버튼 문구와 확인 창 질문에만 쓴다.
 */
public final class ClientSkillNotifications
{
    // 서버 값을 받기 전에는 서버 기본값과 같은 켜짐으로 표시한다.
    private static volatile boolean enabled = true;

    private ClientSkillNotifications()
    {
    }

    public static boolean isEnabled()
    {
        return enabled;
    }

    public static void setEnabled(boolean newEnabled)
    {
        enabled = newEnabled;
    }
}

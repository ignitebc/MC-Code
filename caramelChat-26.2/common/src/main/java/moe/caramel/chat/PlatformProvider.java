package moe.caramel.chat;

import moe.caramel.chat.util.ModLogger;

/**
 * 플랫폼 제공자 인터페이스
 */
public abstract class PlatformProvider {

    /**
     * 모드 id.
     */
    public static final String MOD_ID = "caramelchat";

    /**
     * 기본 제공자.
     */
    public static final PlatformProvider DEFAULT = new PlatformProvider() {
        @Override
        public String getVersion() {
            return "UNKNOWN";
        }

        @Override
        public String getPlatformName() {
            return "UNKNOWN";
        }
    };

    // ================================

    private static PlatformProvider provider = PlatformProvider.DEFAULT;

    /**
     * 플랫폼 제공자를 가져온다.
     *
     * @return 제공자
     */
    public static PlatformProvider getProvider() {
        return provider;
    }

    /**
     * 플랫폼 제공자를 설정한다.
     *
     * @param provider 제공자
     */
    public static void setProvider(final PlatformProvider provider) {
        if (PlatformProvider.provider == PlatformProvider.DEFAULT) {
            PlatformProvider.provider = provider;
            ModLogger.log("The platform provider has been loaded: {}", provider);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    // ================================

    /**
     * 현재 모드 버전을 가져온다.
     *
     * @return 모드 버전
     */
    public abstract String getVersion();

    /**
     * 현재 플랫폼 이름을 가져온다.
     *
     * @return 플랫폼 이름
     */
    public abstract String getPlatformName();

    @Override
    public String toString() {
        return "(" + getPlatformName() + " / " + getVersion() + ")";
    }
}

package com.autovw.advancednetherite.helper;

/**
 * @since 2.0.0
 * @author Autovw
 */
public interface IPlatformHelper
{
    /**
     * @return 모드가 실행 중인 플랫폼
     */
    Platform getPlatform();

    /**
     * 모드가 로드되었는지 확인한다
     * @param modId 확인할 모드 ID
     * @return 모드 로드 여부
     */
    boolean isModLoaded(String modId);

    /**
     * @return 현재 배포 환경에서 실행 중인지 여부
     */
    boolean isProduction();

    enum Platform
    {
        FABRIC;
    }
}

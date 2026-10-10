package com.tacz.guns.client.resource;

import com.tacz.guns.util.TaczThreads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 자원 백그라운드 예열기.
 *
 * <p>이 풀은 원래부터 데몬(유일한 하나)이었고, 기준을 맞추려고 공통 팩토리로 바꿨을 뿐이다 —
 * 풀 세 개가 각자 데몬 로직을 쓴 것이 지난번에 두 개를 놓친 원인이었다.
 * 데몬이어야 하는 이유는 {@link TaczThreads} 참고.
 */
public final class ClientAssetLoadDispatcher {
    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor(TaczThreads.daemonFactory("tacz-client-asset-preload"));

    private ClientAssetLoadDispatcher() {
    }

    public static ExecutorService executor() {
        return EXECUTOR;
    }
}

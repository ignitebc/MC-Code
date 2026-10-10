package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.util.TaczThreads;
import net.minecraft.client.player.LocalPlayer;

import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;

public class LocalPlayerDataHolder {
    /**
     * 총기 조작용 예약 작업 풀(총 꺼내기 효과음 지연, 연발 박자).
     *
     * <p><b>반드시 데몬 풀이어야 한다</b>: 여기서 {@code scheduleAtFixedRate} 연발 작업이 돌고,
     * 저장소 어디에도 {@code shutdown()} 호출이 없다. 예전에는 인자 없는 오버로드,
     * 곧 {@code Executors.defaultThreadFactory()}를 썼는데 — 이것은 <b>무조건</b>
     * {@code setDaemon(false)}라서 두 스레드가 JVM 종료를 계속 막았고,
     * 15초 뒤 {@code ClientShutdownWatchdog}가 크래시 보고서를 냈다.
     * 자세한 내용은 {@link com.tacz.guns.util.TaczThreads} 참고.
     */
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
            Executors.newScheduledThreadPool(2, TaczThreads.daemonFactory("tacz-gun-scheduler"));
    public long clientBaseTimestamp = -1L;
    /**
     * 직전 틱의 조준 진행도. 보간에 쓰며 범위는 0~1
     */
    public static float oldAimingProgress = 0;
    /**
     * 버튼을 누른 시각. 클라이언트에서 버튼을 누른 뒤 실수로 발사되는 것을 막는다
     */
    public static long clientClickButtonTimestamp = -1L;
    /**
     * 플레이어 객체
     */
    private final LocalPlayer player;
    /**
     * 사격 관련 변수들
     */
    public volatile long clientShootTimestamp = -1L;
    public volatile long clientLastShootTimestamp = -1L;
    public volatile boolean isShootRecorded = true;
    public float chargeProgress = 0f;
    public boolean isCharging = false;
    /**
     * 이 상태 잠금은 어느 시점이든 진행 중인 총기 조작이 하나뿐임을 나타낸다.
     * 주로 클라이언트 조작 표시가 중복 실행되는 것을 막는다.
     */
    public volatile boolean clientStateLock = false;
    /**
     * bolt가 끝났는지 표시한다. 클라이언트·서버 비동기로 데이터가 어긋나 bolt가 반복되는 것을 막는다
     */
    public boolean isBolting = false;
    /**
     * 조준 진행도. 범위는 0~1
     */
    public float clientAimingProgress = 0;
    /**
     * 조준 시각(ms)
     */
    public long clientAimingTimestamp = -1L;
    public boolean clientIsAiming = false;
    /**
     * 총기 교체 시각. 교체를 시작할 때 갱신하며 단위는 ms.
     * 클라이언트에서는 집어넣기 애니메이션 시간과 전환 시간 계산에만 쓴다.
     */
    public long clientDrawTimestamp = -1L;
    /**
     * 비동기 총기 교체
     */
    @Nullable
    public ScheduledFuture<?> drawFuture = null;
    /**
     * 잠금에 대한 서버 응답을 기다리는 데 쓴다
     */
    @Nullable
    public Predicate<IGunOperator> lockedCondition = null;
    /**
     * 잠금 응답 시간을 계산한다. 교착을 막기 위해 최대 응답 시간을 넘지 않게 한다
     */
    public long lockTimestamp = -1;

    public LocalPlayerDataHolder(LocalPlayer player) {
        this.player = player;
    }

    /**
     * 상태 잠금을 건다
     */
    public void lockState(@Nullable Predicate<IGunOperator> lockedCondition) {
        clientStateLock = true;
        lockTimestamp = System.currentTimeMillis();
        this.lockedCondition = lockedCondition;
    }

    /**
     * 틱마다 실행되며 상태 잠금을 풀어야 하는지 판단한다.
     */
    public void tickStateLock() {
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
        ReloadState reloadState = gunOperator.getSynReloadState();
        // 아직 잠금이 끝나지 않았으면 상태 잠금을 풀 수 없다
        // 잠금에 허용하는 최대 응답 시간(밀리초)
        long maxLockTime = 250;
        long lockTime = System.currentTimeMillis() - lockTimestamp;
        if (lockTime < maxLockTime && lockedCondition != null && !lockedCondition.test(gunOperator)) {
            return;
        }
        lockedCondition = null;
        if (reloadState.getStateType().isReloading()) {
            return;
        }
        long shootCoolDown = gunOperator.getSynShootCoolDown();
        if (shootCoolDown > 0) {
            return;
        }
        if (gunOperator.getSynDrawCoolDown() > 0) {
            return;
        }
        if (gunOperator.getSynIsBolting()) {
            return;
        }
        if (gunOperator.getSynMeleeCoolDown() > 0) {
            return;
        }
        // 상태 잠금을 푼다
        clientStateLock = false;
    }

    /**
     * 부활 뒤 여러 매개변수를 초기화한다
     */
    public void reset() {
        // 클라이언트 shoot 시각 초기화
        isShootRecorded = true;
        clientShootTimestamp = -1;
        chargeProgress = 0f;
        isCharging = false;
        // 클라이언트 조준 상태 초기화
        clientIsAiming = false;
        clientAimingProgress = 0;
        oldAimingProgress = 0;
        // 노리쇠 당기기 상태 초기화
        isBolting = false;
        // 상태 잠금을 연다
        clientStateLock = false;
    }
}

package com.tacz.guns.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 이 모드의 모든 백그라운드 스레드를 만드는 공통 팩토리 — <b>모두 daemon</b>이다.
 *
 * <h2>반드시 daemon이어야 하는 이유</h2>
 * JVM은 <b>마지막 non-daemon 스레드</b>가 끝나야만 종료된다. 26.2 클라이언트는 이를 위해 감시견을 두었다:
 * {@code Main#main}은 {@code Minecraft#run}과 {@code exitWorldAndClose}가 돌아온 뒤
 * {@code ClientShutdownWatchdog#startShutdownWatchdog}를 호출하고, 그것은
 * {@code CRASH_REPORT_PRELOAD_LOAD}(바이트코드 확인 = 15초) 동안 잔 뒤 프로세스가 아직 살아 있으면
 * 모든 스레드를 dump하고 "Client shutdown from ..." 충돌 보고서를 쓴 다음 {@code System.exit}한다.
 *
 * <p>즉 <b>non-daemon 스레드가 하나라도 남아 있으면 플레이어는 게임을 닫은 지 15초 뒤
 * 충돌 보고서를 받는다</b>. 게임은 이미 끝났지만 보고서는 그대로 나와 "종료하면 충돌"처럼 보인다.
 *
 * <h2>이 모드가 밟은 함정</h2>
 * 수정 전에는 스레드 풀이 세 개 있었고 그중 하나만 daemon이었다:
 * <table border="1">
 *   <caption>수정 전 스레드 풀 상태</caption>
 *   <tr><th>풀</th><th>팩토리</th><th>daemon?</th></tr>
 *   <tr><td>{@code ClientAssetLoadDispatcher.EXECUTOR}</td>
 *       <td>직접 만듦, 명시적 {@code setDaemon(true)}</td><td>예</td></tr>
 *   <tr><td>{@code LocalPlayerDataHolder.SCHEDULED_EXECUTOR_SERVICE}</td>
 *       <td>{@code Executors.defaultThreadFactory()}</td>
 *       <td><b>아니오</b> — 이 팩토리는 <b>무조건</b> {@code setDaemon(false)}</td></tr>
 *   <tr><td>{@code SecondOrderDynamics.executorService}</td>
 *       <td>{@code Thread::new}</td>
 *       <td><b>운에 달림</b> — {@code new Thread(Runnable)}는 <b>만든 스레드</b>의
 *           daemon 속성을 물려받는데, 여기는 정적 초기화 블록이라 누가 먼저 이 클래스를 건드리느냐에 따른다</td></tr>
 * </table>
 *
 * <p>뒤의 둘은 {@code shutdown()} 호출이 하나도 없어서(저장소 전체 grep 0건),
 * 그 스레드들은 프로세스가 강제로 끝날 때까지 계속 살아 있었다.
 *
 * <p>{@code SecondOrderDynamics}가 특히 심했다: 그 {@code update()}는
 * {@code while (!stop)} 무한 반복 + {@code Thread.sleep(6)}인데 {@code stop()}은
 * <b>저장소 전체에서 한 번도 호출되지 않았다</b>. 게다가 상주 인스턴스가 5개
 * ({@code WORLD_FOV} / {@code ITEM_MODEL_FOV} / {@code AIMING} /
 * {@code REFIT_OPENING} / {@code JUMPING})라 끝나지 않는 스레드가 5개인 셈이었다.
 * 그것들이 non-daemon이 되기만 하면 게임을 닫을 때 반드시 감시견이 발동했다.
 *
 * <h2>"종료할 때 스레드 풀 shutdown" 대신 daemon을 쓰는 이유</h2>
 * 둘 다 해결은 되지만 daemon은 <b>약속</b>이 아니라 <b>안전망</b>이다:
 * 어떤 종료 훅이 제대로 등록·실행되는지에 기대지 않고 종료 순서에도 영향을 받지 않는다.
 * 이 풀들이 하는 일은 모두 순수한 클라이언트 표현 층 작업(FOV 부드럽게, 효과음 타이머, 모델 예열)이라
 * 프로세스가 끝날 때 바로 버려도 아무 부작용이 없다 — 저장해야 할 상태가 없다.
 *
 * <p>덤으로 스레드에 이름을 붙였다. 수정 전에는 {@code SecondOrderDynamics}의 그 15개 스레드가
 * jstack에서 모두 {@code Thread-N}이라 불렸고, 그래서 사용자가 보낸 스레드 dump에서
 * 그것들이 이 모드의 것인지 전혀 알아볼 수 없었다.
 */
public final class TaczThreads {
    private TaczThreads() {
    }

    /**
     * daemon 스레드만 만드는 팩토리를 만든다.
     *
     * @param poolName 스레드 이름 접두사. 최종 형태는 {@code tacz-fov-smoothing-1} 같은 식이다
     */
    public static ThreadFactory daemonFactory(String poolName) {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, poolName + "-" + counter.getAndIncrement());
            // 핵심: JVM 종료를 절대 막지 않는다.
            thread.setDaemon(true);
            // 명시적으로 보통 우선순위로 낮춘다 — new Thread는 만든 스레드의 우선순위를 물려받는데,
            // 이 풀들은 렌더링 스레드(우선순위가 높은 편)에서 처음 건드려지는 일이 많다.
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        };
    }
}

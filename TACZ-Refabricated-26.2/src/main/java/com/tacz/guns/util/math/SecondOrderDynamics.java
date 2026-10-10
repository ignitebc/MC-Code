package com.tacz.guns.util.math;

import com.tacz.guns.util.TaczThreads;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SecondOrderDynamics {
    /**
     * 인스턴스마다 스레드 하나를 차지해 {@link #update()} 무한 반복을 돌리므로 풀 용량은 인스턴스 수 이상이어야 한다.
     *
     * <p><b>반드시 daemon 풀이어야 한다</b>: {@code update()}는 {@code while (!stop)} 무한 반복인데
     * {@code stop()}은 저장소 전체에서 한 번도 호출되지 않으며, 상주 인스턴스가 5개 있다.
     * 원래 쓰던 {@code Thread::new}는 <b>만든 스레드</b>의 daemon 속성을 물려받는다
     * — 정적 초기화 블록을 누가 먼저 일으키느냐에 따르므로 "게임을 닫을 때 충돌하는지"를 운에 맡긴 셈이다.
     * non-daemon이 걸리면 이 5개 스레드가 JVM 종료를 막아
     * 15초 뒤 {@code ClientShutdownWatchdog}가 충돌 보고서를 낸다.
     * 자세한 내용은 {@link TaczThreads} 참고.
     */
    public static final ScheduledExecutorService executorService =
            Executors.newScheduledThreadPool(15, TaczThreads.daemonFactory("tacz-dynamics"));

    static {
        for (int i = 0; i < 15; i++) {
            executorService.execute(() -> {
            });
        }
    }

    private final float k1;
    private final float k2;
    private final float k3;

    private float py;
    private float pyd;
    private float px;

    private float target;

    private boolean stop = false;

    /**
     * @param f  고유 진동수
     * @param z  감쇠 계수
     * @param r  초기 속도
     * @param x0 초기 위치
     */
    public SecondOrderDynamics(float f, float z, float r, float x0) {
        k1 = (float) (z / (Math.PI * f));
        k2 = (float) (1 / ((2 * Math.PI * f) * (2 * Math.PI * f)));
        k3 = (float) (r * z / (2 * Math.PI * f));

        py = px = x0;
        pyd = 0;

        target = x0;

        executorService.execute(this::update);
    }

    /**
     * @return 처리한 y 값
     */
    public float update(float x) {
        target = x;
        return get();
    }

    public float get() {
        // 드물게 생기는 NAN 오류를 고친다
        if (Float.isNaN(py)) {
            py = 0;
        }
        if (Float.isNaN(pyd)) {
            pyd = 0;
        }
        return py + 0.05f * pyd;
    }

    public void stop() {
        this.stop = true;
    }

    private void update() {
        while (!stop) {
            // 드물게 생기는 NAN 오류를 고친다
            if (Float.isNaN(py)) {
                py = 0;
            }
            if (Float.isNaN(pyd)) {
                pyd = 0;
            }

            float t = 0.05f;
            float xd = (target - px) / t;
            float y = py + t * pyd;

            pyd = pyd + t * (px + k3 * xd - py - k1 * pyd) / k2;
            px = target;
            py = y;

            try {
                Thread.sleep(6);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

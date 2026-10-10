package com.tacz.guns.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class CycleTaskHelper {
    private static final List<CycleTaskTicker> CYCLE_TASKS = new LinkedList<>();
    private static final List<CycleTaskTicker> TEMP_CYCLE_TASKS = new LinkedList<>();

    /**
     * 주어진 시간 간격으로 작업을 반복 실행한다. 곧바로 한 번 호출한다.
     *
     * @param task     반복 실행할 작업. 돌려준 boolean 값으로 다음 반복 여부를 정한다. false를 돌려주면 더는 반복하지 않는다.
     * @param periodMs 반복 호출 간격(밀리초).
     * @param cycles   최대 반복 횟수. -1이면 무한이다.
     */
    public static void addCycleTask(BooleanSupplier task, long periodMs, int cycles) {
        CycleTaskTicker ticker = new CycleTaskTicker(task, periodMs, cycles);
        if (ticker.tick()) {
            CYCLE_TASKS.add(ticker);
        }
    }

    public static void addCycleTask(BooleanSupplier task, long delayMs, long periodMs, int cycles) {
        if (delayMs <= 0) {
            addCycleTask(task, periodMs, cycles);
            return;
        }
        CycleTaskTicker ticker = new CycleTaskTicker(task, delayMs, periodMs, cycles);
        CYCLE_TASKS.add(ticker);
    }

    public static void tick() {
        TEMP_CYCLE_TASKS.addAll(CYCLE_TASKS);
        CYCLE_TASKS.clear();
        TEMP_CYCLE_TASKS.removeIf(ticker -> !ticker.tick());
        CYCLE_TASKS.addAll(TEMP_CYCLE_TASKS);
        TEMP_CYCLE_TASKS.clear();
    }

    private static class CycleTaskTicker {
        private final BooleanSupplier task;
        private final float periodS;
        private final int cycles;
        private float delayS = 0;
        private long timestamp = -1;
        private float compensation = 0;
        private int count = 0;

        private CycleTaskTicker(BooleanSupplier task, long periodMs, int cycles) {
            this.task = task;
            this.periodS = periodMs / 1000f;
            this.cycles = cycles;
        }

        private CycleTaskTicker(BooleanSupplier task, long delayMs, long periodMs, int cycles) {
            this.delayS = delayMs / 1000f;
            this.timestamp = System.currentTimeMillis();
            this.task = task;
            this.periodS = periodMs / 1000f;
            this.cycles = cycles;
        }

        private boolean tick() {
            if (timestamp == -1) {
                timestamp = System.currentTimeMillis();
                if (cycles > 0 && ++count > cycles) {
                    return false;
                }
                return task.getAsBoolean();
            }
            float duration = (System.currentTimeMillis() - timestamp) / 1000f + compensation;
            if (delayS > 0) {
                if (delayS > duration) {
                    // 지연이 아직 끝나지 않았으면 지연을 줄이고 tick을 계속한다
                    delayS = delayS - duration;
                    return true;
                } else {
                    // 지연 실행이 끝나면 지연을 0으로 둔다
                    // duration을 줄이고 period를 하나 더해, 이후 반복에서 task가 최소 한 번은 실행되게 한다.
                    delayS = 0;
                    duration = duration - delayS + periodS;
                }
            }
            if (duration > periodS) {
                compensation = duration;
                timestamp = System.currentTimeMillis();
                while (compensation > periodS) {
                    if (cycles > 0 && ++count > cycles) {
                        return false;
                    }
                    if (!task.getAsBoolean()) {
                        return false;
                    }
                    compensation -= periodS;
                }
            }
            return true;
        }
    }
}

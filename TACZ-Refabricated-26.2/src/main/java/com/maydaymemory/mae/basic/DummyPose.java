package com.maydaymemory.mae.basic;

/**
 * mae DummyPose의 대체 구현(26.2용 라이브러리가 아직 없음).
 */
public class DummyPose implements Pose {
    public static final DummyPose INSTANCE = new DummyPose();

    private DummyPose() {
    }
}

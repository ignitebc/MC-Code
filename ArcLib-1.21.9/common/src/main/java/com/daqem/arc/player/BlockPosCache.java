package com.daqem.arc.player;

import com.daqem.arc.config.ArcCommonConfig;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class BlockPosCache {
    private static final int MAX_POSITION_COUNT = 1_000;

    private final Set<Vec3i> positionSet = new HashSet<>();
    private final Queue<Vec3i> positionQueue = new LinkedList<>();

    public void add(Vec3i pos) {
        if (positionSet.contains(pos)) {
            return; // Avoid duplicate entries
        }

        int maxPositionCount = Math.min(MAX_POSITION_COUNT, Math.max(1, ArcCommonConfig.maxBlockPosCacheSize.get()));
        while (positionQueue.size() >= maxPositionCount) {
            Vec3i oldest = positionQueue.poll(); // Remove first added element
            if (oldest != null) {
                positionSet.remove(oldest);
            }
        }

        positionQueue.add(pos);
        positionSet.add(pos);
    }

    public boolean contains(Vec3i pos) {
        return positionSet.contains(pos);
    }

    public int size() {
        return positionQueue.size();
    }

    /**
     * 오래된 좌표부터 순서대로 반환한다. 저장 후 복원했을 때 제거 순서가 유지되어야 하므로
     * 큐에 들어온 순서를 그대로 넘긴다.
     */
    public List<Vec3i> getPositions() {
        return new ArrayList<>(positionQueue);
    }

    /**
     * 저장된 좌표 목록으로 캐시를 다시 채운다. 목록이 최대 개수를 넘더라도 add가 오래된 것부터
     * 밀어내므로 최신 좌표만 남는다.
     */
    public void restore(List<? extends Vec3i> positions) {
        positionQueue.clear();
        positionSet.clear();
        positions.forEach(this::add);
    }
}

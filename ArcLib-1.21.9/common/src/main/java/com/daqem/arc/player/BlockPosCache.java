package com.daqem.arc.player;

import com.daqem.arc.config.ArcCommonConfig;
import net.minecraft.core.Vec3i;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BlockPosCache {
    private final Set<Vec3i> positionSet = new HashSet<>();
    private final Queue<Vec3i> positionQueue = new LinkedList<>();

    public void add(Vec3i pos) {
        if (positionSet.contains(pos)) {
            return; // Avoid duplicate entries
        }

        if (positionQueue.size() >= ArcCommonConfig.maxBlockPosCacheSize.get()) {
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
}

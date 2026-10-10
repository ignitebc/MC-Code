package com.mcserver.serverutilities.monster;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 벡스와 그것을 물려받은 몹(illagerinvasion의 항복한 자)이 블록을 통과하지 않고 뚫린 공중으로만 날게 하는 규칙.
 * <p>
 * 바닐라 벡스는 매 틱 충돌을 꺼 벽·나뭇잎·땅을 통과하고, 보이지 않는 곳에서 나타나 공격한다. 이 규칙이 켜져 있으면
 * 충돌을 유지하고 비행 길찾기로 쫓아오며({@link VexPathChaseGoal}), 벽 속에 들어가 있으면 가까운 빈 공간으로 옮긴다.
 */
public final class VexFlightRules {
    /** 벽 속에 있을 때 빈 공간을 찾는 반경(블록) */
    private static final int FREE_SEARCH_RADIUS = 3;
    /** 가까운 칸부터 살피도록 거리순으로 정렬한 탐색 오프셋 */
    private static final List<BlockPos> SEARCH_OFFSETS = sortedOffsets();

    private VexFlightRules() { }

    public static boolean enabled() {
        return ServerUtilities.config().vexCollision();
    }

    /**
     * 벽 속에 있으면 반경 {@link #FREE_SEARCH_RADIUS} 안의 가장 가까운 빈 공간으로, 없으면 소환자 머리 위로 옮긴다.
     * 둘 다 막혀 있으면 벽 속에서 쓸모없이 질식하므로 없앤다. 소환 직후 벽 속에 생긴 경우도 첫 틱에 여기서 처리한다.
     *
     * @return 없앴으면 true
     */
    public static boolean freeIfStuck(Vex vex) {
        if (vex.level().noCollision(vex, vex.getBoundingBox())) return false;
        Vec3 free = findFreeSpot(vex);
        if (free == null) free = aboveOwner(vex);
        if (free == null) {
            vex.discard();
            return true;
        }
        vex.snapTo(free.x, free.y, free.z, vex.getYRot(), vex.getXRot());
        vex.setDeltaMovement(Vec3.ZERO);
        // 떠돌 때 기준점도 옮겨, 다시 벽 쪽 지점을 고르지 않게 한다.
        vex.setBoundOrigin(BlockPos.containing(free));
        return false;
    }

    private static Vec3 findFreeSpot(Vex vex) {
        BlockPos origin = vex.blockPosition();
        for (BlockPos offset : SEARCH_OFFSETS) {
            BlockPos cell = origin.offset(offset);
            Vec3 feet = Vec3.atBottomCenterOf(cell);
            if (fits(vex, feet)) return feet;
        }
        return null;
    }

    private static Vec3 aboveOwner(Vex vex) {
        LivingEntity owner = vex.getOwner();
        if (owner == null || !owner.isAlive()) return null;
        Vec3 feet = owner.position().add(0.0, owner.getBbHeight(), 0.0);
        if (fits(vex, feet)) return feet;
        return null;
    }

    private static boolean fits(Vex vex, Vec3 feet) {
        AABB body = vex.getDimensions(vex.getPose()).makeBoundingBox(feet);
        return vex.level().noCollision(vex, body);
    }

    private static List<BlockPos> sortedOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -FREE_SEARCH_RADIUS; x <= FREE_SEARCH_RADIUS; x++) {
            for (int y = -FREE_SEARCH_RADIUS; y <= FREE_SEARCH_RADIUS; y++) {
                for (int z = -FREE_SEARCH_RADIUS; z <= FREE_SEARCH_RADIUS; z++) {
                    offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        offsets.sort(Comparator.comparingDouble(offset -> offset.distSqr(BlockPos.ZERO)));
        return List.copyOf(offsets);
    }
}

package com.mcserver.serverutilities.client;

import net.minecraft.world.phys.Vec3;

/** 엔티티 렌더 상태에 이번 프레임에 그릴 몬스터 레벨을 담는다. */
public interface MonsterLevelRenderState {
    /** 그릴 레벨. 그리지 않으면 0 */
    int serverutilities$labelLevel();

    /** 그릴 레벨의 위험 단계. 글자 색을 정한다. 그리지 않으면 0 */
    int serverutilities$labelStage();

    /** 이름표 기준점. 바닐라 이름표와 같은 위치에 그리기 위해 쓴다. */
    Vec3 serverutilities$labelAttachment();

    void serverutilities$setLabel(int level, int stage, Vec3 attachment);
}

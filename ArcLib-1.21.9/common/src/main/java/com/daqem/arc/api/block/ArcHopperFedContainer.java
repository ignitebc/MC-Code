package com.daqem.arc.api.block;

/**
 * 호퍼나 발사기처럼 플레이어가 아닌 수단으로 재료가 들어온 적이 있는지 기록하는 보관함.
 *
 * 화로와 양조대는 호퍼로 재료를 넣고 결과만 받아 가는 자동화가 가능하다.
 * 이 표시가 붙은 동안에는 직업 보상을 지급하지 않아, 사람이 직접 넣고 뺀 경우만 인정한다.
 */
public interface ArcHopperFedContainer
{

    boolean arc$isHopperFed();

    void arc$setHopperFed(boolean hopperFed);
}

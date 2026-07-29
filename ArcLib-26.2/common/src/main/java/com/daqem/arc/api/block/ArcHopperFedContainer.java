package com.daqem.arc.api.block;

/**
 * 호퍼를 통해 자동으로 투입된 처리 재료의 남은 수량을 기록하는 보관함.
 *
 * 화로와 양조대는 호퍼로 재료를 넣고 결과만 받아 가는 자동화가 가능하다.
 * 자동 투입된 수량이 모두 처리될 때까지 직업 보상을 지급하지 않아 사람이 직접 넣은 분량만 인정한다.
 */
public interface ArcHopperFedContainer
{

    void arc$recordHopperInsertion(int slot, int amount);

    int arc$consumeHopperFedItems(int amount);
}

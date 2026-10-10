package com.tacz.guns.client.animation.statemachine;

import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;

public class ItemAnimationStateContext extends AnimationStateContext {
    private float putAwayTime = 0f;
    protected float partialTicks = 0f;

    /**
     * 아이템 집어넣기 애니메이션의 권장 시간을 얻는다. 계산 결과일 뿐이며 실제 적용 방식은 상태 기계 구현에 달려 있다.
     *
     * @return 아이템 집어넣기 애니메이션의 권장 시간
     */
    public float getPutAwayTime() {
        return putAwayTime;
    }

    /**
     * 상태 기계 스크립트에서 호출하지 않는다. 아이템 애니메이션의 권장 시간을 설정하는 데 쓴다
     */
    public void setPutAwayTime(float putAwayTime) {
        this.putAwayTime = putAwayTime;
    }

    /**
     * 마지막 갱신 때의 partialTicks를 얻는다
     *
     * @return 상태 기계가 마지막으로 갱신될 때의 partialTicks
     */
    public float getPartialTicks() {
        return partialTicks;
    }

    /**
     * 상태 기계 스크립트에서 호출하지 않는다. 상태 기계를 갱신할 때 partialTicks를 설정하는 데 쓴다.
     */
    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}

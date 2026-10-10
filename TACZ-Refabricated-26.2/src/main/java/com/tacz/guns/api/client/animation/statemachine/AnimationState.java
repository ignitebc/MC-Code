package com.tacz.guns.api.client.animation.statemachine;

public interface AnimationState<T extends AnimationStateContext> {
    /**
     * 매 프레임 모델을 그리기 전에 호출된다.
     *
     * @param context 상태 문맥. 상태 동작에 필요한 여러 매개변수를 담는다.
     */
    void update(T context);

    /**
     * 상태 전이로 이 상태에 들어올 때 호출된다.
     *
     * @param context 상태 문맥. 상태 동작에 필요한 여러 매개변수를 담는다.
     * @see AnimationStateMachine#trigger(String)
     */
    void entryAction(T context);

    /**
     * 상태 전이로 이 상태를 나갈 때 호출된다.
     *
     * @param context 상태 문맥. 상태 동작에 필요한 여러 매개변수를 담는다.
     * @see AnimationStateMachine#trigger(String)
     */
    void exitAction(T context);

    /**
     * 상태 기계가 입력을 받을 때마다 호출된다.
     *
     * @param context   상태 문맥. 상태 동작에 필요한 여러 매개변수를 담는다
     * @param condition 상태 기계가 받은 입력
     * @return 전이한 뒤의 상태. 전이할 필요가 없으면 null
     * @see AnimationStateMachine#trigger(String)
     */
    AnimationState<T> transition(T context, String condition);
}

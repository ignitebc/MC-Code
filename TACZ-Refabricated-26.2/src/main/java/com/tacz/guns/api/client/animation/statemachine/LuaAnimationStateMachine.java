package com.tacz.guns.api.client.animation.statemachine;

import com.tacz.guns.api.client.animation.AnimationController;

import java.util.function.Consumer;

public class LuaAnimationStateMachine<T extends AnimationStateContext> extends AnimationStateMachine<T> {
    Consumer<T> initializeFunc;
    Consumer<T> exitFunc;

    /**
     * 직접 호출하지 말고 팩토리로 인스턴스를 만든다
     *
     * @param animationController 애니메이션 상태 기계가 조종하는 애니메이션 컨트롤러
     * @see LuaStateMachineFactory
     */
    LuaAnimationStateMachine(AnimationController animationController) {
        super(animationController);
    }

    @Override
    public void initialize() {
        this.initializeFunc.accept(this.context);
        super.initialize();
    }

    @Override
    public void exit() {
        this.exitFunc.accept(this.context);
        super.exit();
    }
}

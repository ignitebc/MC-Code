package com.tacz.guns.api.client.animation.statemachine;

import com.tacz.guns.api.client.animation.AnimationController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 무한 애니메이션 상태 기계 구현.
 *
 * @param <T> 상태 기계 문맥 타입
 */
public class AnimationStateMachine<T extends AnimationStateContext> {
    /**
     * 상태 기계의 현재 상태 목록
     */
    private List<AnimationState<T>> currentStates;

    /**
     * 상태 기계 문맥. 애니메이션 상태 갱신에 필요한 여러 매개변수를 담는다
     */
    protected T context;

    /**
     * 초기 상태 Supplier
     */
    private Supplier<Iterable<? extends AnimationState<T>>> statesSupplier;

    /**
     * 상태 기계가 조종하는 애니메이션 컨트롤러
     */
    private final @Nonnull AnimationController animationController;

    protected long exitingTime = -1;

    /**
     * @param animationController 애니메이션 상태 기계가 조종하는 애니메이션 컨트롤러
     */
    public AnimationStateMachine(@Nonnull AnimationController animationController) {
        this.animationController = Objects.requireNonNull(animationController);
    }

    /**
     * 모델을 그리기 전마다 호출된다.
     * 상태 목록의 모든 상태와 애니메이션 컨트롤러를 함께 갱신한다.
     *
     * @see AnimationState#update(AnimationStateContext)
     * @see AnimationController#update()
     */
    public void update() {
        if (context != null && currentStates != null) {
            currentStates.forEach(state -> state.update(context));
        }
        animationController.update();
    }

    /**
     * 1인칭이 아닌 렌더링에서 호출된다. 애니메이션 데이터를 모델에 쓰지는 않지만 상태는 갱신하고
     * 소리도 재생한다
     *
     * @see AnimationState#update(AnimationStateContext)
     * @see AnimationController#updateSoundOnly()
     */
    public void visualUpdate() {
        if (context != null && currentStates != null) {
            currentStates.forEach(state -> state.update(context));
        }
        animationController.updateSoundOnly();
    }

    /**
     * 상태 기계에 입력을 한 번 넣는다. 상태 전이가 일어날 수 있다.
     *
     * @param condition 입력
     */
    public void trigger(String condition) {
        if (context == null || currentStates == null) {
            return;
        }
        // 상태 목록을 돌면서 전이가 필요하면 전이한 상태로 목록을 바꾼다
        ListIterator<AnimationState<T>> iterator = currentStates.listIterator();
        while (iterator.hasNext()) {
            AnimationState<T> state = iterator.next();
            AnimationState<T> nextState = state.transition(context, condition);
            if (nextState != null) {
                state.exitAction(context);
                iterator.set(nextState);
                nextState.entryAction(context);
            }
        }
    }

    /**
     * 상태 기계를 초기화한다. 상태의 entry action이 실행된다.<p>
     * 호출하기 전에 다음 조건을 만족해야 한다:<p>
     * 1. context가 초기화되어 있다<p>
     * 2. 상태 기계가 초기화되지 않은 상태다(처음 만들었거나 exit를 호출하면 이 상태가 된다)
     *
     * @see AnimationState#entryAction(AnimationStateContext)
     */
    public void initialize() {
        if (context == null) {
            throw new IllegalStateException("Context must not be null before initialization");
        }
        if (currentStates != null) {
            throw new IllegalStateException("State machine is already initialized");
        }
        this.currentStates = new LinkedList<>();
        // 주어진 초기 상태를 상태 목록에 넣고 각각의 entryAction을 호출한다.
        Optional.ofNullable(statesSupplier)
                .map(Supplier::get)
                .ifPresent(list -> list.forEach(state -> {
                    currentStates.add(state);
                    state.entryAction(context);
                }));
    }

    /**
     * 상태 기계를 종료한다. 상태의 exit action이 실행된다.
     *
     * @see AnimationState#exitAction(AnimationStateContext)
     */
    public void exit() {
        checkNullPointer();
        // 상태 목록에 있는 모든 상태의 exit action을 호출한다.
        currentStates.forEach(state -> state.exitAction(context));
        this.currentStates = null;
    }

    /**
     * 상태 기계의 권장 종료 시간(밀리초)을 설정한다.<br/>
     * 같은 아이템으로 바꿀 때 애니메이션이 끝나도록 상태 기계 재초기화를 늦추는 데 쓴다.
     */
    public void setExitingTime(long keepTime) {
        this.exitingTime = System.currentTimeMillis() + keepTime;
    }

    /**
     * 상태 기계의 권장 종료 시간(밀리초)을 얻는다.<br/>
     * 같은 아이템으로 바꿀 때 애니메이션이 끝나도록 상태 기계 재초기화를 늦추는 데 쓴다.<br/>
     *
     * @return 권장 종료 시간
     */
    public long getExitingTime() {
        return exitingTime;
    }

    /**
     * @return 상태 기계가 조종하는 애니메이션 컨트롤러
     */
    public @Nonnull AnimationController getAnimationController() {
        return animationController;
    }

    public boolean isInitialized() {
        return currentStates != null;
    }

    /**
     * @return 현재 상태 문맥
     */
    public @Nullable T getContext() {
        return context;
    }

    public void processContextIfExist(Consumer<T> consumer) {
        if (context != null) {
            consumer.accept(context);
        }
    }

    /**
     * 상태 기계의 문맥을 설정한다. 상태 기계로 다른 일을 하기 전에 반드시 이 메서드로 context를 초기화한다.
     * 상태 기계 initialize 뒤에는 이 메서드를 쓸 수 없고, 먼저 exit로 상태 기계를 종료해야 한다.
     * 한 실행 주기 동안 상태 기계가 쓰는 context가 하나뿐이도록 하기 위해서다.
     */
    public void setContext(@Nonnull T context) {
        AnimationStateMachine<?> stateMachine = context.getStateMachine();
        if (stateMachine != null && stateMachine != this) {
            throw new IllegalStateException("Context is already used");
        }
        if (currentStates != null) {
            throw new IllegalStateException("State machine is already initialized, call exit() first");
        }
        if (this.context != null) {
            this.context.setStateMachine(null);
        }
        context.setStateMachine(this);
        this.context = context;
    }

    /**
     * 상태 기계를 초기화할 때 호출되며, 주어진 상태를 현재 상태 목록에 초기 상태로 넣는다.
     * 이 상태들의 entryAction이 호출된다는 점에 주의한다.
     *
     * @param statesSupplier 초기 상태 목록 Supplier
     */
    public void setStatesSupplier(Supplier<Iterable<? extends AnimationState<T>>> statesSupplier) {
        this.statesSupplier = statesSupplier;
    }

    private void checkNullPointer() {
        if (context == null) {
            throw new IllegalStateException("Context has not been initialized");
        }
        if (currentStates == null) {
            throw new IllegalStateException("State machine has not been initialized");
        }
    }
}

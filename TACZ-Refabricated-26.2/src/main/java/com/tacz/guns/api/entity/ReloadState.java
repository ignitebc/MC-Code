package com.tacz.guns.api.entity;

public class ReloadState {
    /**
     * 재장전하지 않을 때 카운트다운은 -1이다
     */
    public static final int NOT_RELOADING_COUNTDOWN = -1;
    /**
     * 재장전 상태
     */
    protected StateType stateType;
    /**
     * 재장전 상태의 남은 시간(밀리초)
     */
    protected long countDown;

    public ReloadState() {
        stateType = StateType.NOT_RELOADING;
        countDown = NOT_RELOADING_COUNTDOWN;
    }

    public ReloadState(ReloadState src) {
        stateType = src.stateType;
        countDown = src.countDown;
    }

    /**
     * @return 현재 재장전 상태 종류. 재장전 중인지, 재장전이 어느 단계인지 판단하는 데 쓴다.
     */
    public StateType getStateType() {
        return stateType;
    }

    public void setStateType(StateType stateType) {
        this.stateType = stateType;
    }

    /**
     * @return StateType이 NOT_RELOADING이면 NOT_RELOADING_COUNTDOWN(= -1), 아니면 현재 상태의 남은 시간(ms).
     */
    public long getCountDown() {
        if (stateType == StateType.NOT_RELOADING) {
            return NOT_RELOADING_COUNTDOWN;
        }
        return countDown;
    }

    public void setCountDown(long countDown) {
        this.countDown = countDown;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ReloadState reloadState) {
            return reloadState.stateType.equals(stateType) && reloadState.countDown == countDown;
        } else {
            return false;
        }
    }

    public enum StateType {
        /**
         * 플레이어가 재장전하지 않는 상태.
         */
        NOT_RELOADING,
        /**
         * 빈 탄창 재장전 중이며 탄약을 채우는 단계.
         */
        EMPTY_RELOAD_FEEDING,
        /**
         * 빈 탄창 재장전 중이며 마무리 단계.
         */
        EMPTY_RELOAD_FINISHING,
        /**
         * 전술 빠른 재장전 중이며 탄약을 채우는 단계.
         */
        TACTICAL_RELOAD_FEEDING,
        /**
         * 전술 빠른 재장전 중이며 마무리 단계.
         */
        TACTICAL_RELOAD_FINISHING;

        /**
         * 이 상태가 빈 탄창 재장전 과정의 한 단계인지 판단한다. 마무리 단계도 포함한다.
         */
        public boolean isReloadingEmpty() {
            return this == EMPTY_RELOAD_FEEDING || this == EMPTY_RELOAD_FINISHING;
        }

        /**
         * 이 상태가 전술 재장전 과정의 한 단계인지 판단한다. 마무리 단계도 포함한다.
         */
        public boolean isReloadingTactical() {
            return this == TACTICAL_RELOAD_FEEDING || this == TACTICAL_RELOAD_FINISHING;
        }

        /**
         * 이 상태가 어떤 재장전 과정의 한 단계인지 판단한다. 마무리 단계도 포함한다.
         */
        public boolean isReloading() {
            return isReloadingEmpty() || isReloadingTactical();
        }

        /**
         * 이 상태가 어떤 재장전 과정의 마무리 단계인지 판단한다.
         */
        public boolean isReloadFinishing() {
            return this == StateType.EMPTY_RELOAD_FINISHING || this == StateType.TACTICAL_RELOAD_FINISHING;
        }
    }
}

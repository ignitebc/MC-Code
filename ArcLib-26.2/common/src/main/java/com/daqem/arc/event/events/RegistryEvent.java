package com.daqem.arc.event.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

/**
 * 레지스트리에 여러 종류를 등록할 때 쓰는 이벤트 모음.
 */
public interface RegistryEvent {

    /**
     * @see RegisterActionType#registerActionType()
     */
    Event<RegisterActionType> REGISTER_ACTION_TYPE = EventFactory.createLoop();
    /**
     * @see RegisterRewardType#registerRewardType()
     */
    Event<RegisterRewardType> REGISTER_REWARD_TYPE = EventFactory.createLoop();
    /**
     * @see RegisterConditionType#registerConditionType()
     */
    Event<RegisterConditionType> REGISTER_CONDITION_TYPE = EventFactory.createLoop();
    /**
     * @see RegisterActionHolderType#registerActionHolderType()
     */
    Event<RegisterActionHolderType> REGISTER_ACTION_HOLDER_TYPE = EventFactory.createLoop();

    interface RegisterActionType {
        /**
         * 액션 종류를 등록할 때 호출된다.
         */
        void registerActionType();
    }

    interface RegisterRewardType {
        /**
         * 보상 종류를 등록할 때 호출된다.
         */
        void registerRewardType();
    }

    interface RegisterConditionType {
        /**
         * 조건 종류를 등록할 때 호출된다.
         */
        void registerConditionType();
    }

    interface RegisterActionHolderType {
        /**
         * 액션 홀더 종류를 등록할 때 호출된다.
         */
        void registerActionHolderType();
    }
}

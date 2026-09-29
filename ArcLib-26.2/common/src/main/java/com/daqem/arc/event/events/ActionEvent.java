package com.daqem.arc.event.events;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;

public interface ActionEvent {

    /**
     * @see ActionEvent.BeforeAction#registerBeforeAction(ActionData)
     */
    Event<ActionEvent.BeforeAction> BEFORE_ACTION = EventFactory.createLoop();

    /**
     * @see ActionEvent.BeforeRewards#registerBeforeRewards(ActionData)
     */
    Event<ActionEvent.BeforeRewards> BEFORE_REWARDS = EventFactory.createLoop();

    /**
     * @see ActionEvent.BeforeConditions#registerBeforeConditions(ActionData)
     */
    Event<ActionEvent.BeforeConditions> BEFORE_CONDITIONS = EventFactory.createLoop();

    /**
     * @see ActionEvent.RewardsApplying#onRewardsApplying(IAction, ActionData)
     */
    Event<ActionEvent.RewardsApplying> REWARDS_APPLYING = EventFactory.createLoop();

    /**
     * @see ActionEvent.RewardsApplied#onRewardsApplied(IAction, ActionData, ActionResult)
     */
    Event<ActionEvent.RewardsApplied> REWARDS_APPLIED = EventFactory.createLoop();

    interface BeforeAction {
        /**
         * Invoked before an action is sent.
         *
         * @return The event result.
         */
        EventResult registerBeforeAction(ActionData actionData);
    }

    interface BeforeRewards {
        /**
         * Invoked before rewards are given.
         *
         * @param actionData The action data.
         * @return The event result.
         */
        EventResult registerBeforeRewards(ActionData actionData);
    }

    interface BeforeConditions {
        /**
         * Invoked before conditions are checked.
         *
         * @param actionData The action data.
         * @return The event result.
         */
        EventResult registerBeforeConditions(ActionData actionData);
    }

    interface RewardsApplying {
        /**
         * 조건을 모두 통과해 이 액션의 보상 적용을 시작하기 직전에 호출된다.
         * 같은 ActionData가 여러 액션에 차례로 전달되므로, 어떤 액션이 실행됐는지는 action 인자로 구분한다.
         * 보상 안에서 다른 액션이 실행되면 이 이벤트가 중첩되어 호출된다.
         *
         * @param action     보상을 적용할 액션
         * @param actionData 액션 데이터
         */
        void onRewardsApplying(IAction action, ActionData actionData);
    }

    interface RewardsApplied {
        /**
         * {@link RewardsApplying} 이후 보상 적용이 끝나면 항상 호출된다. 보상 중 예외가 나도 호출된다.
         *
         * @param action     보상을 적용한 액션
         * @param actionData 액션 데이터
         * @param result     보상 적용 결과. 예외로 끝났으면 빈 결과
         */
        void onRewardsApplied(IAction action, ActionData actionData, ActionResult result);
    }
}

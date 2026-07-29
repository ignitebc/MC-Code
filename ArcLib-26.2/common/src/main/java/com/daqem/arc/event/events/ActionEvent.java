package com.daqem.arc.event.events;

import com.daqem.arc.api.action.data.ActionData;
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
}

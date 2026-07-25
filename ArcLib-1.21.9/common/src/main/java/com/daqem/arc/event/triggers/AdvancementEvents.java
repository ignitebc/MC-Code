package com.daqem.arc.event.triggers;

import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import dev.architectury.event.events.common.PlayerEvent;

public class AdvancementEvents {

    public static void registerEvents() {
        PlayerEvent.PLAYER_ADVANCEMENT.register((player, advancement) -> {
            if (player instanceof ArcServerPlayer arcServerPlayer) {
                new ActionDataBuilder(arcServerPlayer, ActionType.ADVANCEMENT)
                        .withData(ActionDataType.ADVANCEMENT, advancement.value())
                        .build()
                        .sendToAction();
            }
        });
    }
}

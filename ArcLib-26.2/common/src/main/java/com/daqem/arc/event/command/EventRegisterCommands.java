package com.daqem.arc.event.command;

import com.daqem.arc.command.ArcCommand;
import dev.architectury.event.events.common.CommandRegistrationEvent;

public class EventRegisterCommands {

    public static void registerEvent() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> ArcCommand.registerCommand(dispatcher));
    }
}

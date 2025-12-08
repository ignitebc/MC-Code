package com.daqem.arc.command;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.command.argument.ActionArgument;
import com.daqem.arc.networking.ClientboundActionHoldersScreenPacket;
import com.daqem.arc.networking.ClientboundActionScreenPacket;
import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.networking.NetworkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ArcCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("arc").requires(source -> source.hasPermission(2))
                .then(Commands.literal("screen")
                        .then(Commands.literal("action")
                                .then(Commands.argument("action", ActionArgument.action())
                                        .executes(context -> {
                                            IAction action = ActionArgument.getAction(context, "action");
                                            return openActionScreen(context.getSource(), action);
                                        })
                                )
                                .executes(context -> {
                                    List<IAction> actions = ActionHolderManager.getInstance().getActions();
                                    if (actions.isEmpty()) {
                                        context.getSource().sendFailure(Component.literal("No actions found"));
                                        return 1;
                                    } else {
                                        return openActionScreen(context.getSource(), actions.get(0));
                                    }
                                }))
                        .then(Commands.literal("holders")
                                .executes(context -> {
                                    if (context.getSource().getPlayer() instanceof ArcServerPlayer arcServerPlayer) {
                                        List<IActionHolder> actionHolders = arcServerPlayer.arc$getActionHolders();
                                        if (actionHolders.isEmpty()) {
                                            context.getSource().sendFailure(Component.literal("No action holders found"));
                                            return 1;
                                        } else {
                                            return openActionHoldersScreen(context.getSource(), actionHolders);
                                        }
                                    }
                                    return 0;
                                })
                        )
                )
        );
    }

    private static int openActionScreen(CommandSourceStack source, IAction action) {
        if (action == null) {
            source.sendFailure(Component.literal("Unknown action"));
            return 0;
        }
        if (source.getPlayer() != null) {
            NetworkManager.sendToPlayer(source.getPlayer(), new ClientboundActionScreenPacket(action));
        }
        return 1;
    }

    private static int openActionHoldersScreen(CommandSourceStack source, List<IActionHolder> actionHolders) {
        if (source.getPlayer() != null) {
            NetworkManager.sendToPlayer(source.getPlayer(), new ClientboundActionHoldersScreenPacket(actionHolders));
        }
        return 1;
    }
}

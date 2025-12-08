package com.daqem.arc.command.argument;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.data.ActionManager;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class ActionArgument implements ArgumentType<IAction> {

    private final ActionHolderManager actionHolderManager;

    public ActionArgument() {
        this.actionHolderManager = ActionHolderManager.getInstance();
    }

    public static ActionArgument action() {
        return new ActionArgument();
    }

    @Override
    public IAction parse(StringReader reader) throws CommandSyntaxException {
        return actionHolderManager.getAction(ResourceLocation.read(reader)).orElseThrow(() -> {
            reader.setCursor(reader.getRemainingLength());
            return new CommandSyntaxException(null, Component.literal("Unknown action location: " + reader.getString()), reader.getString(), reader.getCursor());
        });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(actionHolderManager.getActionLocationStrings(), builder);
    }

    public static IAction getAction(CommandContext<?> context, String name) {
        return context.getArgument(name, IAction.class);
    }
}

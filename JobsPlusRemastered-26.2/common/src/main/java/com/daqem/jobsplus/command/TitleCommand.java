package com.daqem.jobsplus.command;

import com.daqem.jobsplus.player.title.TitleLedger;
import com.daqem.jobsplus.player.title.TitleManager;
import com.daqem.jobsplus.player.title.TitleType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Arrays;
import java.util.Optional;

/**
 * 운영자용 칭호 명령어. {@code /job title list|grant|revoke}
 *
 * <p>칭호 기능을 넣기 전에 이미 위더나 엔더 드래곤을 잡은 사람이 있거나,
 * 판정이 잘못된 경우 운영자가 바로잡을 수 있게 한다.
 * {@link JobCommand}와 같은 {@code job} 아래에 붙으며 Brigadier가 두 등록을 하나로 합친다.
 */
public final class TitleCommand
{
    private static final SuggestionProvider<CommandSourceStack> TITLE_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(TitleType.values()).map(TitleType::getId), builder);

    private TitleCommand()
    {
    }

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("job")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("title")
                        .then(Commands.literal("list")
                                .executes(context -> list(context.getSource())))
                        .then(Commands.literal("grant")
                                .then(Commands.argument("title", StringArgumentType.word())
                                        .suggests(TITLE_SUGGESTIONS)
                                        .then(Commands.argument("target_player", EntityArgument.player())
                                                .executes(context -> grant(context.getSource(),
                                                        StringArgumentType.getString(context, "title"),
                                                        EntityArgument.getPlayer(context, "target_player"))))))
                        .then(Commands.literal("revoke")
                                .then(Commands.argument("title", StringArgumentType.word())
                                        .suggests(TITLE_SUGGESTIONS)
                                        .executes(context -> revoke(context.getSource(),
                                                StringArgumentType.getString(context, "title")))))));
    }

    private static int list(CommandSourceStack source)
    {
        TitleLedger ledger = TitleLedger.get(source.getServer());
        for (TitleType type : TitleType.values())
        {
            String holderName = ledger.getHolder(type).map(TitleLedger.Holder::playerName).orElse("보유자 없음");
            Component line = Component.literal(type.getId() + " ").append(type.getBadge())
                    .append(Component.literal(" " + holderName));
            source.sendSuccess(() -> line, false);
        }
        return TitleType.values().length;
    }

    private static int grant(CommandSourceStack source, String titleId, ServerPlayer target)
    {
        Optional<TitleType> type = TitleType.byId(titleId);
        if (type.isEmpty())
        {
            source.sendFailure(Component.literal("알 수 없는 칭호입니다: " + titleId));
            return 0;
        }
        MinecraftServer server = source.getServer();
        TitleManager.grant(server, type.get(), target);
        String targetName = target.getScoreboardName();
        source.sendSuccess(() -> Component.literal(targetName + "에게 " + type.get().getDisplayName()
                + " 칭호를 지급했습니다."), true);
        return 1;
    }

    private static int revoke(CommandSourceStack source, String titleId)
    {
        Optional<TitleType> type = TitleType.byId(titleId);
        if (type.isEmpty())
        {
            source.sendFailure(Component.literal("알 수 없는 칭호입니다: " + titleId));
            return 0;
        }
        Optional<TitleLedger.Holder> removed = TitleManager.revoke(source.getServer(), type.get());
        if (removed.isEmpty())
        {
            source.sendFailure(Component.literal(type.get().getDisplayName() + " 칭호는 보유자가 없습니다."));
            return 0;
        }
        String holderName = removed.get().playerName();
        source.sendSuccess(() -> Component.literal(holderName + "의 " + type.get().getDisplayName()
                + " 칭호를 회수했습니다."), true);
        return 1;
    }
}

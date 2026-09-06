package com.mcserver.serverutilities;

import com.mcserver.serverutilities.config.UtilitiesConfig;
import com.mcserver.serverutilities.sleep.SleepRuleManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public final class ServerUtilities implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("serverutilities");
    private static volatile UtilitiesConfig config = UtilitiesConfig.DEFAULT;

    public static UtilitiesConfig config() { return config; }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                reload(server);
            } catch (IOException | IllegalArgumentException exception) {
                throw new IllegalStateException("Server Utilities 설정 또는 수면 복원 기록을 확인하세요.", exception);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> config = UtilitiesConfig.DEFAULT);
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) ->
                dispatcher.register(Commands.literal("serverutilities")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("status").executes(ctx -> {
                            var settings = config();
                            int percentage = ctx.getSource().getServer().getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "한 명 수면=" + settings.singlePlayerSleep() + " (현재 비율=" + percentage + ")"
                                    + ", 겉날개=" + settings.combatElytra() + ", 철골렘=" + settings.combatGolems()
                                    + ", 전투 범위=" + settings.combatRange()
                                    + ", 크리퍼=" + settings.creeperDamage() + " ×" + settings.creeperMultiplier()
                                    + ", 피로도=" + settings.hunger() + " ×" + settings.hungerMultiplier()
                                    + ", 사망 손실=" + settings.deathPenalty() + ", 보존권=" + settings.deathProtection()), false);
                            return 1;
                        }))
                        .then(Commands.literal("reload").executes(ctx -> {
                            try {
                                reload(ctx.getSource().getServer());
                                ctx.getSource().sendSuccess(() -> Component.literal("서버 공통 설정을 다시 적용했습니다."), true);
                                return 1;
                            } catch (IOException | IllegalArgumentException exception) {
                                LOGGER.error("Server Utilities 설정 재적용 실패", exception);
                                ctx.getSource().sendFailure(Component.literal("설정을 적용하지 못했습니다. 설정 파일과 서버 로그를 확인하세요."));
                                return 0;
                            }
                        }))));
    }

    private static void reload(MinecraftServer server) throws IOException {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("serverutilities.properties");
        UtilitiesConfig candidate = UtilitiesConfig.load(configPath);
        Path state = server.getWorldPath(LevelResource.ROOT).resolve("serverutilities-sleep.properties");
        SleepRuleManager.apply(state, candidate.singlePlayerSleep(),
                () -> server.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE),
                value -> server.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, value, server));
        config = candidate;
    }

    private static void rejectLegacyModule(String modId, String legacyClass) {
        FabricLoader.getInstance().getModContainer(modId).ifPresent(container -> {
            if (container.findPath(legacyClass).isPresent()) {
                throw new IllegalStateException(modId + "의 기존 서버 규칙과 중복됩니다. Server Utilities와 함께 제공된 새 JAR로 교체하세요.");
            }
        });
    }
}

package com.daqem.arc.player;

import com.daqem.arc.api.entity.ArcFishingHook;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.config.ArcCommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

/**
 * 우클릭 자동화(압력판과 상호작용 블록을 섞어 우클릭을 대신 소비하는 회로)로 돌아가는
 * 낚시에 직업 보상이 나가지 않게 막는다.
 * <p>
 * 판정 근거는 두 가지다.
 * <ul>
 *     <li>대기 중 블록 우클릭 횟수. 정상 낚시는 던지고 기다리는 동안 블록을 누르지 않는다.
 *         회로는 우클릭을 블록이 대신 먹게 만드는 것이 전제라 반드시 수십 번 발생한다.</li>
 *     <li>대기 중 시선과 위치 변화. 방치 낚시는 입력이 없다. 정상 플레이어가 한 번쯤
 *         가만히 있을 수 있으므로 연속으로 기준에 걸릴 때만 차단한다.</li>
 * </ul>
 */
public final class FishingAutomationGuard {

    private static final long NOTICE_INTERVAL_TICKS = 200L;

    private FishingAutomationGuard() {
    }

    /** 이번 어획에 직업 보상을 줄지 판정한다. */
    public static boolean shouldReward(ServerPlayer serverPlayer, FishingHook fishingHook) {
        if (!ArcCommonConfig.blockAutomatedFishing.get()) {
            return true;
        }
        if (!(serverPlayer instanceof ArcServerPlayer arcServerPlayer)) {
            return true;
        }
        if (!(fishingHook instanceof ArcFishingHook hook)) {
            return true;
        }
        if (hook.arc$getTrackedTicks() < ArcCommonConfig.fishingMinTrackedTicks.get()) {
            return true;
        }

        boolean clickConsumedByBlocks =
                hook.arc$getBlockInteractionCount() >= ArcCommonConfig.fishingBlockInteractionLimit.get();

        boolean noInput = hook.arc$getMaxYawDelta() < ArcCommonConfig.fishingMinLookDegrees.get()
                && hook.arc$getMaxPitchDelta() < ArcCommonConfig.fishingMinLookDegrees.get()
                && hook.arc$getMaxPositionDelta() < ArcCommonConfig.fishingMinMoveBlocks.get();

        if (!clickConsumedByBlocks && !noInput) {
            arcServerPlayer.arc$setAutomatedFishingStreak(0);
            return true;
        }

        int streak = arcServerPlayer.arc$getAutomatedFishingStreak() + 1;
        arcServerPlayer.arc$setAutomatedFishingStreak(streak);
        if (streak < ArcCommonConfig.fishingAutomationStreak.get()) {
            return true;
        }

        notify(serverPlayer, arcServerPlayer);
        return false;
    }

    private static void notify(ServerPlayer serverPlayer, ArcServerPlayer arcServerPlayer) {
        long now = serverPlayer.level().getGameTime();
        if (now - arcServerPlayer.arc$getLastFishingNoticeTick() < NOTICE_INTERVAL_TICKS) {
            return;
        }
        arcServerPlayer.arc$setLastFishingNoticeTick(now);
        serverPlayer.displayClientMessage(
                Component.literal("자동 낚시로 판정되어 직업 경험치와 비트코인이 지급되지 않습니다."), true);
    }
}

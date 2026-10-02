package com.daqem.arc.event.triggers;

import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * 불·독처럼 공격자가 없는 피해로 죽은 생물의 처치를 직전에 피해를 준 플레이어에게 인정한다.
 * <p>
 * 불화살·화염 인챈트·소이탄으로 붙은 불과 독은 피해 원인에 공격자를 남기지 않는다. 바닐라는 플레이어가 준
 * 피해를 5초만 기억하므로, 맹독 불화살처럼 그보다 오래 타다 죽으면 처치 보상이 빠진다. 그래서 플레이어가
 * 마지막으로 피해를 준 시각을 따로 기록해 두고 더 긴 시간 동안 처치를 인정한다.
 */
public class KillCreditTracker {

    /**
     * 공격자 없는 피해로 죽었을 때 처치를 인정하는 시간(틱).
     * <p>
     * 사냥꾼 맹독 불화살 X의 화염 지속시간 15초에 바닐라 처치 기여 기억 시간 5초를 여유로 더했다.
     */
    private static final long CREDIT_TICKS = 20 * 20;

    // 생물이 사라지면 기록도 함께 정리되도록 약한 참조로 둔다.
    private static final Map<LivingEntity, PlayerHit> LAST_PLAYER_HITS = new WeakHashMap<>();

    private KillCreditTracker() {
    }

    public static void recordHit(LivingEntity victim, ArcServerPlayer attacker) {
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        UUID playerId = attacker.arc$getServerPlayer().getUUID();
        LAST_PLAYER_HITS.put(victim, new PlayerHit(playerId, level.getGameTime()));
    }

    /**
     * 이 죽음을 처치로 인정받을 플레이어.
     *
     * @return 공격자가 플레이어면 그 플레이어. 공격자 없는 피해로 죽었으면 제한 시간 안에 마지막으로 피해를 준
     * 플레이어. 그 밖에는 null
     */
    public static ArcServerPlayer findKiller(LivingEntity victim, DamageSource source) {
        if (!(victim.level() instanceof ServerLevel level)) {
            return null;
        }
        // 실제 사망 완료 후 업적도 같은 공로를 확인한다. 조회에서 소모하지 않고 개체 GC 시 정리한다.
        PlayerHit lastHit = LAST_PLAYER_HITS.get(victim);
        Entity attacker = source.getEntity();
        if (attacker instanceof ArcServerPlayer player) {
            return player;
        }
        // 다른 생물이 죽였으면 그 생물의 처치다. 플레이어가 먼저 때렸어도 가져오지 않는다.
        boolean diedWithoutAttacker = attacker == null;
        if (diedWithoutAttacker && lastHit != null) {
            return findRecentAttacker(level, lastHit);
        }
        return null;
    }

    private static ArcServerPlayer findRecentAttacker(ServerLevel level, PlayerHit lastHit) {
        long elapsedTicks = level.getGameTime() - lastHit.gameTime();
        if (elapsedTicks > CREDIT_TICKS) {
            return null;
        }
        // 사망 후 부활하면 플레이어 개체가 바뀌므로 UUID로 현재 개체를 찾는다.
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(lastHit.playerId());
        if (player instanceof ArcServerPlayer arcPlayer) {
            return arcPlayer;
        }
        return null;
    }

    private record PlayerHit(UUID playerId, long gameTime) {
    }
}

package com.daqem.jobsplus.player.job.powerup;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.networking.s2c.ClientboundFirepowerBoostPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import dev.architectury.networking.NetworkManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 사냥꾼 일반스킬 화력 증강의 장탄 수 보너스.
 * <p>
 * 화살 추가는 ArcLib 행동으로 처리하고, 장탄 수는 TACZ가 사수 기준 최대 장탄을 계산할 때 이 값을 더한다.
 * TACZ 클라이언트도 재장전 가능 여부를 직접 판정하므로, 서버가 계산한 값을 본인 클라이언트에 보낸다.
 */
public final class FirepowerBoost
{
    private static final Identifier HUNTER = JobsPlus.getId("hunter");
    private static final String SKILL_PREFIX = "hunter/multiple_arrows_";
    private static final List<String> TIERS = List.of("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x");
    private static final String GUN_NAMESPACE = "tacz";
    private static final String PISTOL = "pistol";
    private static final String RIFLE = "rifle";
    /**
     * rifle 분류 가운데 돌격소총만 적용한다.
     * 같은 분류의 지정사수소총(dragunov, mk14, sks_tactical, slr, vss, spr15hb, mini14, mk12)은 저격 성격이라 제외한다.
     */
    private static final Set<String> ASSAULT_RIFLES = Set.of(
            "ace32", "ak47", "aug", "beryl_m762", "famas", "fn_fal", "g36k", "groza", "hk_g3", "k2",
            "m16a1", "m16a4", "m416", "m4a1", "mk47_mutant", "qbz_191", "qbz_95", "scar_h", "scar_l", "type_81");

    /** 서버가 보낸 클라이언트 본인의 보너스. 서버 판정에는 쓰지 않는다. */
    private static volatile UUID clientPlayerId;
    private static volatile int clientExtraRounds;

    private FirepowerBoost()
    {
    }

    /** TACZ 최대 장탄 계산에서 호출된다. 서버는 직업 데이터로 계산하고, 클라이언트는 받은 값을 쓴다. */
    public static int extraRounds(LivingEntity shooter, Identifier gunId, String gunType)
    {
        if (!isEligibleGun(gunId, gunType))
        {
            return 0;
        }
        if (shooter.level().isClientSide())
        {
            boolean localPlayer = shooter.getUUID().equals(clientPlayerId);
            if (localPlayer)
            {
                return clientExtraRounds;
            }
            return 0;
        }
        if (shooter instanceof JobsServerPlayer jobsServerPlayer)
        {
            return extraRoundsFor(jobsServerPlayer);
        }
        return 0;
    }

    /** 권총 분류 전체와 돌격소총. 저격총·지정사수소총·산탄총·기관단총·기관총·발사기는 제외한다. */
    private static boolean isEligibleGun(Identifier gunId, String gunType)
    {
        if (PISTOL.equals(gunType))
        {
            return true;
        }
        boolean assaultRifle = GUN_NAMESPACE.equals(gunId.getNamespace()) && ASSAULT_RIFLES.contains(gunId.getPath());
        return RIFLE.equals(gunType) && assaultRifle;
    }

    /** 켜 둔 화력 증강의 가장 높은 단계로 정한다. I·II +1, III·IV +2, V·VI +3, VII·VIII +4, IX·X +5 */
    public static int extraRoundsFor(JobsServerPlayer player)
    {
        Job hunter = player.jobsplus$getJob(HUNTER);
        if (hunter == null)
        {
            return 0;
        }

        int highestTier = 0;
        for (Powerup powerup : hunter.getPowerupManager().getAllPowerups())
        {
            if (powerup.getState() == PowerupState.ACTIVE)
            {
                highestTier = Math.max(highestTier, tierOf(powerup.getPowerupLocation()));
            }
        }
        if (highestTier == 0)
        {
            return 0;
        }
        return (highestTier + 1) / 2;
    }

    /** 화력 증강 단계(1~10). 다른 스킬이면 0 */
    private static int tierOf(Identifier powerupLocation)
    {
        String path = powerupLocation.getPath();
        boolean firepowerBoost = JobsPlus.MOD_ID.equals(powerupLocation.getNamespace()) && path.startsWith(SKILL_PREFIX);
        if (!firepowerBoost)
        {
            return 0;
        }
        return TIERS.indexOf(path.substring(SKILL_PREFIX.length())) + 1;
    }

    /** 본인 클라이언트에 현재 보너스를 알린다. 접속 직후와 직업·스킬 상태가 바뀔 때 호출한다. */
    public static void sync(ServerPlayer player)
    {
        // 저장 데이터를 읽는 중에는 아직 연결이 없다. 접속 이벤트에서 다시 보낸다.
        if (player.connection == null || !(player instanceof JobsServerPlayer jobsServerPlayer))
        {
            return;
        }
        NetworkManager.sendToPlayer(player, new ClientboundFirepowerBoostPacket(extraRoundsFor(jobsServerPlayer)));
    }

    /** 클라이언트가 받은 값을 보관한다. */
    public static void setClientState(UUID playerId, int extraRounds)
    {
        clientPlayerId = playerId;
        clientExtraRounds = Math.max(0, extraRounds);
    }
}

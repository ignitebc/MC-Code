package com.daqem.jobsplus.player.job.powerup;

import com.daqem.jobsplus.JobsPlus;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 삭제하고 다른 스킬로 대체한 일반스킬의 이전 ID를 새 ID로 바꿔 읽는다.
 * <p>
 * 삭제한 스킬 ID는 저장 데이터에 효과 없이 남아 구매에 쓴 직업코인만 사라지므로,
 * 같은 자리를 이어받은 새 스킬의 같은 단계로 구매 상태와 ON/OFF를 그대로 옮긴다.
 */
public final class PowerupAliases
{
    private static final List<String> TIERS = List.of("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x");
    private static final Map<Identifier, Identifier> ALIASES = createAliases();

    private PowerupAliases()
    {
    }

    /** 대체된 스킬이면 새 ID를, 아니면 받은 ID를 그대로 돌려준다. */
    public static Identifier resolve(Identifier powerupLocation)
    {
        return ALIASES.getOrDefault(powerupLocation, powerupLocation);
    }

    private static Map<Identifier, Identifier> createAliases()
    {
        Map<Identifier, Identifier> aliases = new HashMap<>();
        // 사냥꾼 연격 I~X는 화약충전 I~X로 대체했다. 요구 레벨·가격·선행 관계가 같아 같은 단계로 잇는다.
        for (String tier : TIERS)
        {
            aliases.put(JobsPlus.getId("hunter/attack_speed_" + tier), JobsPlus.getId("hunter/gunpowder_charge_" + tier));
        }
        return Map.copyOf(aliases);
    }
}

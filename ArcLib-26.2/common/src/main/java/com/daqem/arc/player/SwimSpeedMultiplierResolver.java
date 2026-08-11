package com.daqem.arc.player;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.reward.player.SwimSpeedMultiplierReward;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 활성 홀더가 가진 물속 이동 속도 배율 보상을 모아 최종 배율을 계산한다.
 * <p>
 * 이동은 클라이언트가 계산하므로 이 값은 서버와 클라이언트에서 같은 방식으로 구해야 한다.
 * 클라이언트에서는 서버 전용 조건(JobsPlayer 기반 등)을 평가할 수 없어 조건 검사 없이
 * 활성 홀더 목록만 사용한다. 활성 홀더 목록은 파워업 활성 상태가 바뀔 때마다 서버가
 * 클라이언트로 동기화하므로 양쪽 결과가 일치한다.
 * <p>
 * 중첩 규칙: 같은 스킬 줄(홀더 위치에서 단계 접미사를 뺀 값이 같은 스킬)은 최고 단계
 * 하나만 적용되고, 서로 다른 스킬 줄의 증가량은 합연산으로 더해진다.
 * 예를 들어 +50% 스킬 두 개를 함께 켜면 1 + 0.5 + 0.5 = 2.0배가 된다.
 * <p>
 * 수영 중에는 매 이동 틱마다 배율이 필요하므로 여기서 직접 계산하지 않고,
 * 홀더 구성이 바뀔 때 {@link PlayerActionCache}가 이 계산을 호출해 결과를 캐시한다.
 */
public final class SwimSpeedMultiplierResolver
{

    private SwimSpeedMultiplierResolver()
    {
    }

    public static float computeMultiplier(Collection<IActionHolder> actionHolders)
    {
        Map<String, Float> highestBonusBySkillLine = new HashMap<>();
        for (IActionHolder holder : actionHolders)
        {
            if (holder == null)
            {
                continue;
            }

            float highestBonus = 0.0F;
            for (IAction action : holder.getActions())
            {
                for (IReward reward : action.getRewards())
                {
                    if (reward instanceof SwimSpeedMultiplierReward swimSpeedReward)
                    {
                        highestBonus = Math.max(highestBonus, swimSpeedReward.getMultiplier() - 1.0F);
                    }
                }
            }

            if (highestBonus <= 0.0F)
            {
                continue;
            }
            highestBonusBySkillLine.merge(SkillLineKey.of(holder.getLocation()), highestBonus, Math::max);
        }

        float totalBonus = 0.0F;
        for (float bonus : highestBonusBySkillLine.values())
        {
            totalBonus += bonus;
        }
        return 1.0F + totalBonus;
    }
}

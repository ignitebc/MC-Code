package com.daqem.arc.player;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.reward.player.FishingWaitTimeMultiplierReward;

import java.util.HashMap;
import java.util.Map;

/**
 * 활성 홀더가 가진 낚시 대기시간 배율 보상을 모아 최종 배율을 돌려준다.
 * <p>
 * 중첩 규칙은 다른 스킬과 동일하다. 같은 스킬 줄은 최고 단계(가장 큰 감소) 하나만 적용되고,
 * 서로 다른 스킬 줄의 감소율은 합연산으로 더해진다. 대기시간이 음수가 되지 않도록
 * 총 감소율은 95%까지만 반영한다. 낚시 타이머는 서버에서만 계산되므로 조건 평가 없이
 * 활성 홀더 목록만으로 충분하다.
 */
public final class FishingWaitTimeMultiplierResolver
{

    private static final float MAX_TOTAL_REDUCTION = 0.95F;

    private FishingWaitTimeMultiplierResolver()
    {
    }

    public static float getMultiplier(ArcPlayer arcPlayer)
    {
        Map<String, Float> highestReductionBySkillLine = new HashMap<>();
        for (IActionHolder holder : arcPlayer.arc$getActionHolders())
        {
            if (holder == null)
            {
                continue;
            }

            float highestReduction = 0.0F;
            for (IAction action : holder.getActions())
            {
                for (IReward reward : action.getRewards())
                {
                    if (reward instanceof FishingWaitTimeMultiplierReward waitTimeReward)
                    {
                        highestReduction = Math.max(highestReduction, 1.0F - waitTimeReward.getMultiplier());
                    }
                }
            }

            if (highestReduction <= 0.0F)
            {
                continue;
            }
            highestReductionBySkillLine.merge(SkillLineKey.of(holder.getLocation()), highestReduction, Math::max);
        }

        float totalReduction = 0.0F;
        for (float reduction : highestReductionBySkillLine.values())
        {
            totalReduction += reduction;
        }
        return 1.0F - Math.min(totalReduction, MAX_TOTAL_REDUCTION);
    }
}

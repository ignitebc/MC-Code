package com.daqem.arc.player;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.reward.IReward;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 플레이어의 활성 홀더가 가진 액션을 타입별로 정렬해 들고 있는 캐시.
 * <p>
 * 걷기·달리기·수영 같은 액션은 매 틱 발생하는데, 그때마다 모든 홀더의 액션을
 * 복사·필터·정렬하면 단기 객체가 대량으로 생겨 GC 부담이 커진다. 액션 구성은
 * 직업/파워업 변경이나 데이터팩 reload 때만 바뀌므로 그 시점에만 다시 만든다.
 * <p>
 * 홀더 추가/제거는 플레이어 쪽에서 {@link #invalidate()}로 알려주고, 데이터팩
 * reload 는 플레이어가 참조 중인 홀더 객체의 내용물을 제자리에서 바꾸므로
 * {@link ActionHolderManager}의 데이터 세대 번호 비교로 감지한다.
 */
public class PlayerActionCache {

    public record ActionEntry(IAction action, IActionHolder holder) {
    }

    private final Map<IActionType<?>, List<ActionEntry>> actionsByType = new HashMap<>();
    private float swimSpeedMultiplier = 1.0F;
    private boolean built = false;
    private int builtDataGeneration = ActionHolderManager.getInstance().getDataGeneration();

    public boolean isUpToDate() {
        return built && !isDataGenerationOutdated();
    }

    public boolean isDataGenerationOutdated() {
        return builtDataGeneration != ActionHolderManager.getInstance().getDataGeneration();
    }

    public void invalidate() {
        this.built = false;
    }

    public void rebuild(Collection<IActionHolder> actionHolders) {
        this.builtDataGeneration = ActionHolderManager.getInstance().getDataGeneration();
        this.actionsByType.clear();
        for (IActionHolder actionHolder : actionHolders) {
            if (actionHolder == null) {
                continue;
            }
            for (IAction action : actionHolder.getActions()) {
                this.actionsByType.computeIfAbsent(action.getType(), type -> new ArrayList<>())
                        .add(new ActionEntry(action, actionHolder));
            }
        }

        for (List<ActionEntry> entries : this.actionsByType.values()) {
            entries.sort((entry1, entry2) -> Integer.compare(
                    getHighestRewardPriority(entry2.action()),
                    getHighestRewardPriority(entry1.action())));
        }

        this.swimSpeedMultiplier = SwimSpeedMultiplierResolver.computeMultiplier(actionHolders);
        this.built = true;
    }

    public List<ActionEntry> getActionsOfType(IActionType<?> actionType) {
        List<ActionEntry> entries = this.actionsByType.get(actionType);
        if (entries == null) {
            return List.of();
        }
        return entries;
    }

    public float getSwimSpeedMultiplier() {
        return swimSpeedMultiplier;
    }

    private static int getHighestRewardPriority(IAction action) {
        List<IReward> rewards = action.getRewards();
        if (rewards.isEmpty()) {
            return 0;
        }
        int highestPriority = Integer.MIN_VALUE;
        for (IReward reward : rewards) {
            highestPriority = Math.max(highestPriority, reward.getPriority());
        }
        return highestPriority;
    }
}

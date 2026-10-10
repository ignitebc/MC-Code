package com.autovw.advancednetherite.common.randombox;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 랜덤 상자를 연 플레이어에게 줄 수 없는 보상을 후보에서 빼는 규칙 모음.
 *
 * <p>Advanced Netherite는 직업 정보를 모르므로, 최대 직업 수가 상한에 닿은 플레이어의 직업선택권처럼
 * 플레이어 상태에 따라 쓸모없는 보상은 그 상태를 아는 모드가 규칙을 등록한다. 제외된 보상은 처음부터 후보가
 * 아니므로, 뽑힌 뒤 다시 돌리는 것과 같은 결과가 된다.
 */
public final class RandomBoxRewardFilters {

    /** 보상 하나를 이 플레이어의 후보에서 뺄지 정한다. */
    @FunctionalInterface
    public interface Filter {
        boolean excludes(Player player, Identifier rewardItemId);
    }

    // 서버 시작 시 등록하고 상자를 열 때마다 읽으므로 읽기에 강한 목록을 쓴다.
    private static final List<Filter> FILTERS = new CopyOnWriteArrayList<>();

    private RandomBoxRewardFilters() {
    }

    public static void register(Filter filter) {
        FILTERS.add(filter);
    }

    /** 등록된 규칙 중 하나라도 이 보상을 빼라고 하면 true */
    public static boolean isExcluded(Player player, Identifier rewardItemId) {
        for (Filter filter : FILTERS) {
            if (filter.excludes(player, rewardItemId)) {
                return true;
            }
        }
        return false;
    }
}

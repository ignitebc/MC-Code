package com.daqem.arc.event.triggers;

import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class StatEvents {

    // 이전에는 플레이어별로 모든 Stat 총량을 별도 목록에 복제해 두고 매 award 마다
    // 선형 탐색했다. 실제로 필요한 값은 수영/겉날개 세션 누적 거리뿐이므로
    // 이미 있는 플레이어 필드에 증가분만 더하는 방식으로 대체했다.
    public static void onAwardStat(ArcServerPlayer player, Stat<?> stat, int amount) {
        onAwardSwimStat(player, stat, amount);
        onAwardUseStat(player, stat);
        onAwardElytraFlyingStat(player, stat, amount);
    }

    private static void onAwardSwimStat(ArcServerPlayer player, Stat<?> stat, int amount) {
        if (stat.equals(Stats.CUSTOM.get(Stats.SWIM_ONE_CM))) {
            player.arc$setSwimmingDistanceInCm(player.arc$getSwimmingDistanceInCm() + amount);
        }
    }

    private static void onAwardUseStat(ArcServerPlayer player, Stat<?> stat) {
        if (stat.getType() == Stats.ITEM_USED) {
            ItemEvents.onUseItem(player, (Item) stat.getValue());
        }
    }

    private static void onAwardElytraFlyingStat(ArcServerPlayer player, Stat<?> stat, int amount) {
        if (stat == Stats.CUSTOM.get(Stats.AVIATE_ONE_CM)) {
            player.arc$setElytraFlyingDistanceInCm(player.arc$getElytraFlyingDistance() + amount);
        }
    }
}

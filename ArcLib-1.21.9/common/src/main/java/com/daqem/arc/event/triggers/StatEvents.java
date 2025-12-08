package com.daqem.arc.event.triggers;

import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class StatEvents {

    public static void onAwardStat(ArcServerPlayer player, Stat<?> stat, int previousAmount, int newAmount) {
        onAwardSwimStat(player, stat, newAmount);
        onAwardUseStat(player, stat);
        onAwardElytraFlyingStat(player, stat, newAmount);
    }

    private static void onAwardSwimStat(ArcServerPlayer player, Stat<?> stat, int newAmount) {
        if (stat.equals(Stats.CUSTOM.get(Stats.SWIM_ONE_CM))) {
            player.arc$setSwimmingDistanceInCm(newAmount);
        }
    }

    private static void onAwardUseStat(ArcServerPlayer player, Stat<?> stat) {
        if (stat.getType() == Stats.ITEM_USED) {
            ItemEvents.onUseItem(player, (Item) stat.getValue());
        }
    }

    private static void onAwardElytraFlyingStat(ArcServerPlayer player, Stat<?> stat, int newAmount) {
        if (stat == Stats.CUSTOM.get(Stats.AVIATE_ONE_CM)) {
            player.arc$setElytraFlyingDistanceInCm(newAmount);
        }
    }
}

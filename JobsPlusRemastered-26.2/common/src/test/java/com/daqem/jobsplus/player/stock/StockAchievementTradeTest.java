package com.daqem.jobsplus.player.stock;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockAchievementTradeTest
{
    @Test
    void oldHoldingsAreExcludedBeforeSeasonBuysAreSold()
    {
        StockAchievementTrade trade = new StockAchievementTrade(5, 0, 0, false).buy(1);
        trade = trade.sell(5);
        assertFalse(trade.completed());
        assertEquals(1, trade.remainingAmount());
        trade = trade.sell(0.5);
        assertFalse(trade.completed());
        trade = trade.sell(0.5);
        assertTrue(trade.completed());
    }

    @Test
    void multipleSmallCompletedTradesAccumulateActualBuyPrincipal()
    {
        StockAchievementTrade trade = new StockAchievementTrade(0, 0, 0, false);
        trade = trade.buy(0.5).sell(0.5);
        assertFalse(trade.completed());
        trade = trade.buy(0.5).sell(0.25);
        assertFalse(trade.completed());
        trade = trade.sell(0.25);
        assertTrue(trade.completed());
    }

    @Test
    void liquidationDoesNotCountAsSaleOrEraseAlreadyCompletedTrades()
    {
        StockAchievementTrade open = new StockAchievementTrade(0, 0, 0, false).buy(2);
        StockAchievementTrade liquidated = open.liquidate();
        assertFalse(liquidated.completed());
        assertEquals(0, liquidated.boughtAmount());
        assertFalse(liquidated.buy(0.5).sell(0.5).completed());
        StockAchievementTrade completed = open.sell(2);
        assertEquals(completed, completed.liquidate());
    }

    @Test
    void decimalBuysDoNotMissTheOneBitcoinThreshold()
    {
        StockAchievementTrade trade = new StockAchievementTrade(0, 0, 0, false);
        for (int count = 0; count < 10; count++)
        {
            trade = trade.buy(0.1).sell(0.1);
        }
        assertEquals(1.0, trade.boughtAmount());
        assertTrue(trade.completed());
    }

    @Test
    void liquidatingANewCyclePreservesPreviouslyClosedPrincipal()
    {
        StockAchievementTrade trade = new StockAchievementTrade(0, 0, 0, false);
        trade = trade.buy(0.5).sell(0.5);
        trade = trade.buy(2).sell(1).liquidate();
        assertEquals(0.5, trade.boughtAmount());
        assertFalse(trade.completed());
        assertTrue(trade.buy(0.5).sell(0.5).completed());
    }

    @Test
    void accountUpdatesAndCodecPreserveThePermanentTradeRecord()
    {
        StockAchievementTrade trade = new StockAchievementTrade(0, 1, 0, true);
        StockAccount account = new StockAccount(10, List.of(), List.of(), Map.of("season3/apple", trade));
        account = account.deposit(5).withdraw(2, 0).reserveBuy(1).cancelReservedBuy(1);
        var encoded = StockAccount.CODEC.encodeStart(JsonOps.INSTANCE, account).getOrThrow();
        StockAccount loaded = StockAccount.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(Map.of("season3/apple", trade), loaded.achievementTrades());
        assertEquals(13, loaded.balance());
    }
}

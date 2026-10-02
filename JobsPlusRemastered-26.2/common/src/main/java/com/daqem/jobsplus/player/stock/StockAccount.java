package com.daqem.jobsplus.player.stock;

import com.daqem.jobsplus.achievement.AchievementStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public record StockAccount(double balance, List<StockPosition> positions, List<StockTransaction> transactions,
                           Map<String, StockAchievementTrade> achievementTrades)
{
    public static final StockAccount EMPTY = new StockAccount(0, List.of(), List.of());
    public static final Codec<StockAccount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("balance", 0D).forGetter(StockAccount::balance),
            StockPosition.CODEC.listOf().optionalFieldOf("positions", List.of()).forGetter(StockAccount::positions),
            StockTransaction.CODEC.listOf().optionalFieldOf("transactions", List.of()).forGetter(StockAccount::transactions),
            Codec.unboundedMap(Codec.STRING, StockAchievementTrade.CODEC).optionalFieldOf("achievement_trades", Map.of()).forGetter(StockAccount::achievementTrades)
    ).apply(instance, StockAccount::new));

    public StockAccount
    {
        achievementTrades = Map.copyOf(achievementTrades);
        balance = StockDecimal.truncate(balance);
        positions = positions.stream()
                .map(position -> new StockPosition(
                        position.stockId(),
                        0,
                        StockDecimal.truncate(position.costBasis()),
                        StockDecimal.truncate(position.investedAmount()),
                        StockDecimal.truncate(position.getAverageEntryPrice()),
                        position.side(),
                        position.leverage()
                ))
                .toList();
        transactions = transactions.stream()
                .map(transaction -> new StockTransaction(
                        transaction.type(),
                        transaction.stockId(),
                        StockDecimal.truncate(transaction.amount()),
                        StockDecimal.truncate(transaction.returnRate()),
                        transaction.returnRateRecorded(),
                        transaction.timestamp()
                ))
                .toList();
    }

    /** 이전 저장 데이터와 클라이언트 계좌 패킷은 업적 원장 없이도 읽는다. */
    public StockAccount(double balance, List<StockPosition> positions, List<StockTransaction> transactions)
    {
        this(balance, positions, transactions, Map.of());
    }

    public StockPosition getPosition(String stockId)
    {
        return this.positions.stream()
                .filter(position -> position.stockId().equals(stockId))
                .findFirst()
                .orElse(null);
    }

    public StockAccount deposit(double amount)
    {
        return new StockAccount(this.balance + amount, this.positions, addTransaction("DEPOSIT", "", amount), this.achievementTrades);
    }

    public StockAccount withdraw(double amount, double taxAmount)
    {
        return new StockAccount(this.balance - amount - taxAmount, this.positions,
                addTransaction("WITHDRAW", "", amount), this.achievementTrades);
    }

    public StockAccount reserveBuy(double amount)
    {
        return new StockAccount(this.balance - amount, this.positions, this.transactions, this.achievementTrades);
    }

    public StockAccount fillReservedBuy(String stockId, double amount, double currentPrice,
                                        StockPositionSide side, int leverage, long filledAt)
    {
        List<StockPosition> updatedPositions = this.addInvestment(
                stockId,
                amount,
                currentPrice,
                side,
                leverage
        );
        if (updatedPositions == null)
        {
            return this.cancelReservedBuy(amount);
        }

        return new StockAccount(
                this.balance,
                updatedPositions,
                addTransaction("BUY", stockId, amount, 0, false, filledAt),
                trackAchievementBuy(stockId, amount, filledAt)
        );
    }

    public StockAccount cancelReservedBuy(double amount)
    {
        return new StockAccount(this.balance + amount, this.positions, this.transactions, this.achievementTrades);
    }

    public StockAccount sell(String stockId, double amount, double currentPrice, double feeRate)
    {
        StockPosition oldPosition = getPosition(stockId);
        if (oldPosition == null)
        {
            return this;
        }

        if (oldPosition.isLiquidated(currentPrice))
        {
            return this;
        }

        double soldRatio = 1;
        if (oldPosition.investedAmount() > 0)
        {
            soldRatio = amount / oldPosition.investedAmount();
        }
        double soldCostBasis = Math.max(0, oldPosition.costBasis() * soldRatio);
        double saleAmount = oldPosition.getCurrentValue(currentPrice) * soldRatio;
        double leveragedFeeRate = feeRate * oldPosition.leverage();
        double feeAmount = StockDecimal.truncate(saleAmount * leveragedFeeRate);
        double remainingInvestedAmount = Math.max(0, oldPosition.investedAmount() - amount);
        double remainingCostBasis = Math.max(0, oldPosition.costBasis() - soldCostBasis);
        double returnRate = 0;
        if (soldCostBasis > 0)
        {
            returnRate = ((saleAmount - feeAmount) / soldCostBasis - 1) * 100;
        }
        List<StockPosition> updatedPositions = new ArrayList<>(this.positions);
        updatedPositions.remove(oldPosition);
        if (remainingInvestedAmount > 0.00000001)
        {
            updatedPositions.add(new StockPosition(
                    stockId, 0, remainingCostBasis, remainingInvestedAmount,
                    oldPosition.getAverageEntryPrice(), oldPosition.side(), oldPosition.leverage()));
        }
        return new StockAccount(this.balance + saleAmount - feeAmount, List.copyOf(updatedPositions),
                addTransaction("SELL", stockId, amount, returnRate, true), trackAchievementSell(stockId, amount));
    }

    public StockAccount liquidate(String stockId)
    {
        return this.liquidate(stockId, System.currentTimeMillis());
    }

    public StockAccount liquidate(String stockId, long liquidatedAt)
    {
        StockPosition liquidatedPosition = getPosition(stockId);
        if (liquidatedPosition == null)
        {
            return this;
        }

        List<StockPosition> updatedPositions = new ArrayList<>(this.positions);
        updatedPositions.remove(liquidatedPosition);
        return new StockAccount(
                this.balance,
                List.copyOf(updatedPositions),
                addTransaction(
                        "LIQUIDATION",
                        stockId,
                        liquidatedPosition.investedAmount(),
                        -100,
                        true,
                        liquidatedAt
                ),
                trackAchievementLiquidation(stockId)
        );
    }

    private Map<String, StockAchievementTrade> trackAchievementBuy(String stockId, double amount, long filledAt)
    {
        if (!AchievementStorage.isLoaded() || filledAt < AchievementStorage.seasonStartedAt())
        {
            return this.achievementTrades;
        }
        String key = AchievementStorage.season() + "/" + stockId;
        Map<String, StockAchievementTrade> updated = new HashMap<>(this.achievementTrades);
        StockAchievementTrade trade = updated.get(key);
        if (trade == null)
        {
            double previous = 0;
            StockPosition position = getPosition(stockId);
            if (position != null)
            {
                previous = position.investedAmount();
            }
            trade = new StockAchievementTrade(previous, 0, 0, false);
        }
        updated.put(key, trade.buy(amount));
        return Map.copyOf(updated);
    }

    private Map<String, StockAchievementTrade> trackAchievementSell(String stockId, double amount)
    {
        Map<String, StockAchievementTrade> updated = new HashMap<>(this.achievementTrades);
        // 이전 시즌에 산 미매도분도 실제 매도에 맞춰 정리한다. 현 시즌 판정은 Manager에서 구분한다.
        for (Map.Entry<String, StockAchievementTrade> entry : this.achievementTrades.entrySet())
        {
            if (entry.getKey().endsWith("/" + stockId))
            {
                updated.put(entry.getKey(), entry.getValue().sell(amount));
            }
        }
        return Map.copyOf(updated);
    }

    private Map<String, StockAchievementTrade> trackAchievementLiquidation(String stockId)
    {
        Map<String, StockAchievementTrade> updated = new HashMap<>(this.achievementTrades);
        for (Map.Entry<String, StockAchievementTrade> entry : this.achievementTrades.entrySet())
        {
            if (entry.getKey().endsWith("/" + stockId))
            {
                updated.put(entry.getKey(), entry.getValue().liquidate());
            }
        }
        return Map.copyOf(updated);
    }

    private List<StockTransaction> addTransaction(String type, String stockId, double amount)
    {
        return addTransaction(type, stockId, amount, 0, false);
    }

    private List<StockTransaction> addTransaction(String type, String stockId, double amount, double returnRate,
                                                  boolean returnRateRecorded)
    {
        return addTransaction(type, stockId, amount, returnRate, returnRateRecorded, System.currentTimeMillis());
    }

    private List<StockTransaction> addTransaction(String type, String stockId, double amount, double returnRate,
                                                  boolean returnRateRecorded, long timestamp)
    {
        List<StockTransaction> updatedTransactions = new ArrayList<>(this.transactions);
        updatedTransactions.add(0,
                new StockTransaction(type, stockId, amount, returnRate, returnRateRecorded,
                        timestamp));
        if (updatedTransactions.size() > 50)
        {
            updatedTransactions = new ArrayList<>(updatedTransactions.subList(0, 50));
        }
        return List.copyOf(updatedTransactions);
    }

    private List<StockPosition> addInvestment(String stockId, double amount, double currentPrice,
                                              StockPositionSide side, int leverage)
    {
        List<StockPosition> updatedPositions = new ArrayList<>(this.positions);
        StockPosition oldPosition = getPosition(stockId);
        if (oldPosition == null)
        {
            updatedPositions.add(new StockPosition(stockId, 0, amount, amount, currentPrice, side, leverage));
            return List.copyOf(updatedPositions);
        }
        if (oldPosition.side() != side || oldPosition.leverage() != leverage)
        {
            return null;
        }

        updatedPositions.remove(oldPosition);
        updatedPositions.add(oldPosition.addInvestment(amount, currentPrice));
        return List.copyOf(updatedPositions);
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeDouble(this.balance);
        buffer.writeCollection(this.positions, (buf, position) -> {
            buf.writeUtf(position.stockId());
            buf.writeDouble(position.units());
            buf.writeDouble(position.costBasis());
            buf.writeDouble(position.investedAmount());
            buf.writeDouble(position.getAverageEntryPrice());
            buf.writeEnum(position.side());
            buf.writeVarInt(position.leverage());
        });
        buffer.writeCollection(this.transactions, (buf, transaction) -> {
            buf.writeUtf(transaction.type());
            buf.writeUtf(transaction.stockId());
            buf.writeDouble(transaction.amount());
            buf.writeDouble(transaction.returnRate());
            buf.writeBoolean(transaction.returnRateRecorded());
            buf.writeLong(transaction.timestamp());
        });
    }

    public static StockAccount fromNetwork(RegistryFriendlyByteBuf buffer)
    {
        double balance = buffer.readDouble();
        List<StockPosition> positions = buffer.readList(buf -> new StockPosition(
                buf.readUtf(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readEnum(StockPositionSide.class),
                buf.readVarInt()
        ));
        List<StockTransaction> transactions = buffer.readList(buf -> new StockTransaction(
                buf.readUtf(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readBoolean(),
                buf.readLong()
        ));
        return new StockAccount(balance, positions, transactions);
    }
}

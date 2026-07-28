package com.daqem.jobsplus.player.stock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record StockAccount(double balance, List<StockPosition> positions, List<StockTransaction> transactions)
{
    public static final StockAccount EMPTY = new StockAccount(0, List.of(), List.of());
    public static final Codec<StockAccount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("balance", 0D).forGetter(StockAccount::balance),
            StockPosition.CODEC.listOf().optionalFieldOf("positions", List.of()).forGetter(StockAccount::positions),
            StockTransaction.CODEC.listOf().optionalFieldOf("transactions", List.of()).forGetter(StockAccount::transactions)
    ).apply(instance, StockAccount::new));

    public StockAccount
    {
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

    public StockPosition getPosition(String stockId)
    {
        return this.positions.stream()
                .filter(position -> position.stockId().equals(stockId))
                .findFirst()
                .orElse(null);
    }

    public StockAccount deposit(double amount)
    {
        return new StockAccount(this.balance + amount, this.positions, addTransaction("DEPOSIT", "", amount));
    }

    public StockAccount withdraw(double amount, double taxAmount)
    {
        return new StockAccount(this.balance - amount - taxAmount, this.positions,
                addTransaction("WITHDRAW", "", amount));
    }

    public StockAccount buy(String stockId, double amount, double currentPrice, StockPositionSide side, int leverage)
    {
        List<StockPosition> updatedPositions = new ArrayList<>(this.positions);
        StockPosition oldPosition = getPosition(stockId);
        if (oldPosition == null)
        {
            updatedPositions.add(new StockPosition(stockId, 0, amount, amount, currentPrice, side, leverage));
        }
        else
        {
            if (oldPosition.side() != side || oldPosition.leverage() != leverage)
            {
                return this;
            }

            double oldUnits = oldPosition.getUnits();
            double addedUnits = amount / currentPrice;
            double updatedCostBasis = oldPosition.costBasis() + amount;
            double totalUnits = oldUnits + addedUnits;
            double updatedAverageEntryPrice = 0;
            if (totalUnits > 0)
            {
                updatedAverageEntryPrice = updatedCostBasis / totalUnits;
            }
            updatedPositions.remove(oldPosition);
            updatedPositions.add(new StockPosition(
                    stockId,
                    0,
                    updatedCostBasis,
                    oldPosition.investedAmount() + amount,
                    updatedAverageEntryPrice,
                    side,
                    leverage
            ));
        }
        return new StockAccount(this.balance - amount, List.copyOf(updatedPositions),
                addTransaction("BUY", stockId, amount));
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
                addTransaction("SELL", stockId, amount, returnRate, true));
    }

    public StockAccount liquidate(String stockId)
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
                addTransaction("LIQUIDATION", stockId, liquidatedPosition.investedAmount(), -100, true)
        );
    }

    private List<StockTransaction> addTransaction(String type, String stockId, double amount)
    {
        return addTransaction(type, stockId, amount, 0, false);
    }

    private List<StockTransaction> addTransaction(String type, String stockId, double amount, double returnRate,
                                                  boolean returnRateRecorded)
    {
        List<StockTransaction> updatedTransactions = new ArrayList<>(this.transactions);
        updatedTransactions.add(0,
                new StockTransaction(type, stockId, amount, returnRate, returnRateRecorded,
                        System.currentTimeMillis()));
        if (updatedTransactions.size() > 50)
        {
            updatedTransactions = new ArrayList<>(updatedTransactions.subList(0, 50));
        }
        return List.copyOf(updatedTransactions);
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

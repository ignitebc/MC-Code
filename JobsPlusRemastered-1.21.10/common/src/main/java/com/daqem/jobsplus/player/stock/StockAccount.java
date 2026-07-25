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
                        StockDecimal.truncate(position.quantity()),
                        StockDecimal.truncate(position.getAverageEntryPrice())
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

    public StockAccount buy(String stockId, double amount, double currentPrice)
    {
        List<StockPosition> updatedPositions = new ArrayList<>(this.positions);
        StockPosition oldPosition = getPosition(stockId);
        if (oldPosition == null)
        {
            updatedPositions.add(new StockPosition(stockId, 0, amount, amount, currentPrice));
        }
        else
        {
            double oldUnits = oldPosition.getAverageEntryPrice() <= 0
                    ? 0
                    : oldPosition.costBasis() / oldPosition.getAverageEntryPrice();
            double addedUnits = amount / currentPrice;
            double updatedCostBasis = oldPosition.costBasis() + amount;
            double updatedAverageEntryPrice = oldUnits + addedUnits <= 0
                    ? 0
                    : updatedCostBasis / (oldUnits + addedUnits);
            updatedPositions.remove(oldPosition);
            updatedPositions.add(new StockPosition(
                    stockId,
                    0,
                    updatedCostBasis,
                    oldPosition.quantity() + amount,
                    updatedAverageEntryPrice
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

        double quantityReductionRatio = oldPosition.quantity() <= 0 ? 1 : amount / oldPosition.quantity();
        double soldCostBasis = Math.max(0, oldPosition.costBasis() * quantityReductionRatio);
        double saleAmount = oldPosition.getAverageEntryPrice() <= 0
                ? 0
                : soldCostBasis / oldPosition.getAverageEntryPrice() * currentPrice;
        double feeAmount = StockDecimal.truncate(saleAmount * feeRate);
        double remainingQuantity = Math.max(0, oldPosition.quantity() - amount);
        double remainingCostBasis = Math.max(0, oldPosition.costBasis() - soldCostBasis);
        double returnRate = soldCostBasis <= 0
                ? 0
                : ((saleAmount - feeAmount) / soldCostBasis - 1) * 100;
        List<StockPosition> updatedPositions = new ArrayList<>(this.positions);
        updatedPositions.remove(oldPosition);
        if (remainingQuantity > 0.00000001)
        {
            updatedPositions.add(new StockPosition(
                    stockId, 0, remainingCostBasis, remainingQuantity,
                    oldPosition.getAverageEntryPrice()));
        }
        return new StockAccount(this.balance + saleAmount - feeAmount, List.copyOf(updatedPositions),
                addTransaction("SELL", stockId, amount, returnRate, true));
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
            buf.writeDouble(position.quantity());
            buf.writeDouble(position.getAverageEntryPrice());
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
                buf.readDouble()
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

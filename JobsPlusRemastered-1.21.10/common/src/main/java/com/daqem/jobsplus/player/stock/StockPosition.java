package com.daqem.jobsplus.player.stock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StockPosition(String stockId, double units, double costBasis, double quantity,
                            double averageEntryPrice)
{
    public static final Codec<StockPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stock_id").forGetter(StockPosition::stockId),
            Codec.DOUBLE.fieldOf("units").forGetter(StockPosition::units),
            Codec.DOUBLE.fieldOf("cost_basis").forGetter(StockPosition::costBasis),
            Codec.DOUBLE.optionalFieldOf("quantity", -1D).forGetter(StockPosition::quantity),
            Codec.DOUBLE.optionalFieldOf("average_entry_price", -1D)
                    .forGetter(StockPosition::averageEntryPrice)
    ).apply(instance, StockPosition::new));

    public StockPosition
    {
        if (quantity < 0)
        {
            quantity = costBasis;
        }
        if (averageEntryPrice < 0)
        {
            averageEntryPrice = units <= 0 ? 0 : costBasis / units;
        }
    }

    public double getAverageEntryPrice()
    {
        return this.averageEntryPrice;
    }

    public double getCurrentValue(double currentPrice)
    {
        return this.averageEntryPrice <= 0 ? 0 : this.costBasis / this.averageEntryPrice * currentPrice;
    }
}

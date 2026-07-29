package com.daqem.jobsplus.player.stock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StockTransaction(String type, String stockId, double amount, double returnRate,
                               boolean returnRateRecorded, long timestamp)
{
    public static final Codec<StockTransaction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(StockTransaction::type),
            Codec.STRING.optionalFieldOf("stock_id", "").forGetter(StockTransaction::stockId),
            Codec.DOUBLE.fieldOf("amount").forGetter(StockTransaction::amount),
            Codec.DOUBLE.optionalFieldOf("return_rate", 0D).forGetter(StockTransaction::returnRate),
            Codec.BOOL.optionalFieldOf("return_rate_recorded", false)
                    .forGetter(StockTransaction::returnRateRecorded),
            Codec.LONG.fieldOf("timestamp").forGetter(StockTransaction::timestamp)
    ).apply(instance, StockTransaction::new));
}

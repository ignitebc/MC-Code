package com.daqem.jobsplus.player.stock;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class StockDecimal
{
    private static final int SCALE = 8;

    private StockDecimal()
    {
    }

    public static double truncate(double value)
    {
        return BigDecimal.valueOf(value)
                .setScale(SCALE, RoundingMode.DOWN)
                .doubleValue();
    }
}

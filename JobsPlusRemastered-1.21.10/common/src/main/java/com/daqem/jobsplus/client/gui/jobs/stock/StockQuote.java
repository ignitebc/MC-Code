package com.daqem.jobsplus.client.gui.jobs.stock;

public record StockQuote(String id, String name, double priceKrw, double percentChange, boolean available)
{
    public static StockQuote loading(String id, String name)
    {
        return new StockQuote(id, name, 0, 0, false);
    }
}

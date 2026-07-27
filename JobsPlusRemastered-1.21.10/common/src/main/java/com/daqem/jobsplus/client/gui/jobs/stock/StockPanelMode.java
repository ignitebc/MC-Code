package com.daqem.jobsplus.client.gui.jobs.stock;

public enum StockPanelMode
{
    BUY("구매"),
    SELL("판매"),
    TRANSFER("입출금"),
    HISTORY("거래내역");

    private final String name;

    StockPanelMode(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}

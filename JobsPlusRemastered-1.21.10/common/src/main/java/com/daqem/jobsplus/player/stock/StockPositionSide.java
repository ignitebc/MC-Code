package com.daqem.jobsplus.player.stock;

public enum StockPositionSide
{
    LONG("LONG", "롱", 1),
    SHORT("SHORT", "숏", -1);

    private final String serializedName;
    private final String displayName;
    private final int returnDirection;

    StockPositionSide(String serializedName, String displayName, int returnDirection)
    {
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.returnDirection = returnDirection;
    }

    public String getSerializedName()
    {
        return this.serializedName;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public int getReturnDirection()
    {
        return this.returnDirection;
    }

    public static StockPositionSide fromSerializedName(String serializedName)
    {
        for (StockPositionSide side : values())
        {
            if (side.serializedName.equals(serializedName))
            {
                return side;
            }
        }
        return LONG;
    }
}

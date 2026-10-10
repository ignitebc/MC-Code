package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public enum FeedType {
    /**
     * 탄창 급탄
     */
    @SerializedName("magazine")
    MAGAZINE,
    /**
     * 수동 급탄
     */
    @SerializedName("manual")
    MANUAL,
    /**
     * 연료 급탄(아이템 하나를 소모해 탄약을 가득 채움)
     */
    @SerializedName("fuel")
    FUEL,
    /**
     * 인벤토리 직접 장전(인벤토리 안 탄약을 바로 소모)
     */
    @SerializedName("inventory")
    INVENTORY
}

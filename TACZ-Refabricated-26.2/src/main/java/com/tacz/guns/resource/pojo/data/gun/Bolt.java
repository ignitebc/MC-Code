package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public enum Bolt {
    /**
     * 개방 노리쇠 대기
     */
    @SerializedName("open_bolt")
    OPEN_BOLT,
    /**
     * 폐쇄 노리쇠 대기
     */
    @SerializedName("closed_bolt")
    CLOSED_BOLT,
    /**
     * 수동 장전
     */
    @SerializedName("manual_action")
    MANUAL_ACTION
}

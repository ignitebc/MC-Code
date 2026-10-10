package com.tacz.guns.api.item.gun;

import com.google.gson.annotations.SerializedName;

public enum FireMode {
    /**
     * 자동
     */
    @SerializedName("auto")
    AUTO,
    /**
     * 반자동
     */
    @SerializedName("semi")
    SEMI,
    /**
     * 점사
     */
    @SerializedName("burst")
    BURST,
    /**
     * 알 수 없는 그 밖의 경우
     */
    @SerializedName("unknown")
    UNKNOWN
}

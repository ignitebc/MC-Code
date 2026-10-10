package com.tacz.guns.api.item.attachment;

import com.google.gson.annotations.SerializedName;

public enum AttachmentType {
    /**
     * 조준경
     */
    @SerializedName("scope")
    SCOPE,
    /**
     * 총구 부품
     */
    @SerializedName("muzzle")
    MUZZLE,
    /**
     * 개머리판
     */
    @SerializedName("stock")
    STOCK,
    /**
     * 손잡이
     */
    @SerializedName("grip")
    GRIP,
    /**
     * 레이저 지시기
     */
    @SerializedName("laser")
    LASER,
    /**
     * 확장 탄창
     */
    @SerializedName("extended_mag")
    EXTENDED_MAG,
    /**
     * 아이템이 부착물이 아님을 나타낸다.
     */
    NONE
}

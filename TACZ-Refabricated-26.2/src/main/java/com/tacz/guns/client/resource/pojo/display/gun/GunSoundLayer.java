package com.tacz.guns.client.resource.pojo.display.gun;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

/**
 * 이름 있는 총소리에 섞는 추가 층 하나.
 *
 * <p>층은 기존 TaCZ OGG 자원을 가리킨다. 음량과 음높이는
 * 기본 총소리 설정 위에 곱하는 상대 배율이다.
 */
public class GunSoundLayer {
    @Nullable
    @SerializedName("sound")
    private Identifier sound;

    @SerializedName("volume")
    private float volume = 1.0F;

    @SerializedName("pitch")
    private float pitch = 1.0F;

    @Nullable
    public Identifier getSound() {
        return sound;
    }

    public float getVolume() {
        return Math.max(0.0F, Math.min(2.0F, volume));
    }

    public float getPitch() {
        return Math.max(0.25F, Math.min(2.0F, pitch));
    }
}

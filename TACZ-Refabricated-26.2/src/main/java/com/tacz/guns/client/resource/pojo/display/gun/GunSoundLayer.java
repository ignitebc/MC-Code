package com.tacz.guns.client.resource.pojo.display.gun;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

/**
 * One additional layer mixed with a named gun sound.
 *
 * <p>The layer points at an existing TaCZ OGG resource. Volume and pitch are
 * relative multipliers applied on top of the base gun sound settings.
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

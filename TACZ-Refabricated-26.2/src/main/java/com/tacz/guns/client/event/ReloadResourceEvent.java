package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.event.TextureStitchEvent;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.client.sound.SoundPlayManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class ReloadResourceEvent {
    public static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");

    public static void onTextureStitchEventPost(TextureStitchEvent.Post event) {
        if (BLOCK_ATLAS_TEXTURE.equals(event.getAtlas().location())) {
            // InternalAssetLoader는 기본 애니메이션·모델을 불러와야 하므로 총기 팩보다 먼저 불러온다.
            InternalAssetLoader.onResourceReload();
            SoundPlayManager.clearSoundResourceCache();
        }
    }
}

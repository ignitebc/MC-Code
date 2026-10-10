package cn.sh1rocu.tacz.util.forge;

import cn.sh1rocu.tacz.api.event.ClientPlayerNetworkEvent;
import cn.sh1rocu.tacz.api.event.ComputeFovModifierEvent;
import cn.sh1rocu.tacz.api.event.TextureStitchEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ClientHooks {
    public static float getFieldOfViewModifier(Player entity, float fovModifier) {
        ComputeFovModifierEvent fovModifierEvent = new ComputeFovModifierEvent(entity, fovModifier);
        ComputeFovModifierEvent.CALLBACK.invoker().post(fovModifierEvent);
        return fovModifierEvent.getNewFovModifier();
    }

    public static void firePlayerLogout(@Nullable MultiPlayerGameMode pc, @Nullable LocalPlayer player) {
        ClientPlayerNetworkEvent.LOGGING_OUT.invoker().post(new ClientPlayerNetworkEvent.LoggingOut(pc, player, player != null ? player.connection != null ? player.connection.getConnection() : null : null));
    }

    // [r42] firePlayerRespawn은 삭제했다. 유일한 호출자인 ClientPacketListenerMixin이
    // ClientLevel#addPlayer 주입 지점에 의존했는데, 26.2에는 그 메서드가 없어 해당 mixin을 지웠다.
    // 부활 뒤 부착물 캐시 새로 고침은 RefreshClonePlayerDataEvent#onClientTick이
    // Minecraft#player 인스턴스 변화를 감지해 처리하므로 이 훅은 더 필요 없다.

    public static void onTextureStitchedPost(TextureAtlas map) {
        TextureStitchEvent.POST.invoker().post(new TextureStitchEvent.Post(map));
    }
}

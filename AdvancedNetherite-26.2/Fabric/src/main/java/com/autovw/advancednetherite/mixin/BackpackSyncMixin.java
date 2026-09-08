package com.autovw.advancednetherite.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class BackpackSyncMixin
{
    @Inject(method = "tick", at = @At("TAIL"))
    private void advancednetherite$syncInventory(CallbackInfo ci)
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.containerMenu != player.inventoryMenu)
        {
            // 상자나 제작대를 열어 둔 동안 자동 획득·재료 소비로 바뀐 확장 칸도 동기화한다.
            player.inventoryMenu.broadcastChanges();
        }
    }
}

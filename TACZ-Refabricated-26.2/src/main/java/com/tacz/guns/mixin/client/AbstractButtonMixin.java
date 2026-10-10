package com.tacz.guns.mixin.client;

import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public class AbstractButtonMixin {
    /**
     * 버튼을 클릭한 시간을 기록해, 이후 사격 대기 시간을 주어 버튼 클릭 뒤 실수로 발사되는 것을 막는다
     */
    @Inject(method = "onClick(Lnet/minecraft/client/input/MouseButtonEvent;Z)V", at = @At("HEAD"))
    public void onClickHead(MouseButtonEvent event, boolean doubleClick, CallbackInfo ci) {
        LocalPlayerDataHolder.clientClickButtonTimestamp = System.currentTimeMillis();
    }
}

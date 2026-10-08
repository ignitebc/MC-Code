package com.mcserver.serverutilities.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 피격 없이 최대 체력만 줄어 체력이 잘렸을 때는 화면 흔들림과 하트 깜빡임 없이 체력만 바꾼다.
 *
 * <p>장비 등급 체력이 붙은 장비를 바꾸면 서버는 줄어든 최대 체력에 맞춰 체력을 잘라 체력 패킷을 보낸다. 최대 체력
 * 패킷은 다음 틱에 오므로 클라이언트는 최대 체력이 그대로인 채 체력 감소만 받고, 바닐라는 이를 피격으로 처리한다.
 * 실제 피격은 체력 패킷보다 피해 이벤트 패킷이 먼저 와서 hurtTime이 이미 켜져 있다. 그래서 hurtTime이 꺼진 상태의
 * 체력 감소는 장비·직업·효과 종료로 최대 체력이 줄어든 것으로 보고 연출을 생략한다.
 */
@Mixin(LocalPlayer.class)
abstract class LocalPlayerHealthClipMixin {
    @Shadow private boolean flashOnSetHealth;

    @Inject(method = "hurtTo", at = @At("HEAD"), cancellable = true)
    private void serverutilities$applyHealthWithoutHurt(float health, CallbackInfo ci) {
        // 접속·부활 직후 첫 체력은 바닐라가 연출 없이 반영하므로 그대로 둔다.
        if (!flashOnSetHealth) return;

        LocalPlayer player = (LocalPlayer) (Object) this;
        boolean decreased = health < player.getHealth();
        boolean hurtAnimationPlaying = player.hurtTime > 0;
        if (decreased && !hurtAnimationPlaying) {
            player.setHealth(health);
            ci.cancel();
        }
    }
}

package com.mcserver.serverutilities.mixin;

import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 주민과 떠돌이 상인의 거래 횟수 제한을 없앤다.
 *
 * <p>바닐라는 거래할 때마다 사용 횟수를 올리고 최대 횟수에 닿으면 품절시킨다. 주민은 직업 블록에서
 * 하루 두 번까지만 재입고하며, 재입고 때 쌓인 사용 횟수만큼 수요를 올려 가격을 인상한다.
 * 떠돌이 상인은 재입고 자체가 없다. 두 상인 모두 이 클래스를 거치므로 한 곳에서 처리한다.
 */
@Mixin(MerchantOffer.class)
public abstract class MerchantTradeLimitMixin {
    // 사용 횟수가 쌓이지 않아야 품절, 재입고 대기, 수요에 따른 가격 인상이 함께 사라진다.
    // 거래 창을 연 클라이언트도 이 메서드로 횟수를 세므로, 설치된 클라이언트는 표시가 서버와 일치한다.
    @Inject(method = "increaseUses", at = @At("HEAD"), cancellable = true)
    private void serverutilities$keepUses(CallbackInfo ci) {
        ci.cancel();
    }

    // 적용 전에 이미 품절된 거래를 재입고까지 기다리지 않고 바로 연다.
    // 서버가 거래 목록을 보낼 때 이 결과를 품절 여부로 함께 전송한다.
    @Inject(method = "isOutOfStock", at = @At("HEAD"), cancellable = true)
    private void serverutilities$neverOutOfStock(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}

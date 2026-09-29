package com.tacz.guns.mixin.common;

import com.tacz.guns.util.StarterGunKit;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Server Utilities의 시작 장비에 무작위 총기 1정과 탄약 한 탄창을 넣는다. Server Utilities 없이도 로드된다. */
@Pseudo
@Mixin(targets = "com.mcserver.serverutilities.starter.StarterKitRules", remap = false)
public abstract class StarterKitGunMixin {
    @Inject(method = "createStarterGunKit", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tacz$createStarterGunKit(RandomSource random, CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> kit = StarterGunKit.create(random);
        if (kit.isEmpty()) return;
        cir.setReturnValue(kit);
    }

    /** 지급 안내에 총기별 이름을 쓴다. */
    @Inject(method = "gunName", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tacz$gunName(ItemStack gun, CallbackInfoReturnable<Component> cir) {
        StarterGunKit.displayName(gun).ifPresent(cir::setReturnValue);
    }
}

package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public class LocalPlayerInspect {
    private final LocalPlayerDataHolder data;
    private final LocalPlayer player;

    public LocalPlayerInspect(LocalPlayerDataHolder data, LocalPlayer player) {
        this.data = data;
        this.player = player;
    }

    public void inspect() {
        // 우선 주 손만 살펴볼 수 있다
        ItemStack mainHandItem = player.getMainHandItem();

        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            if (BuiltinItemRendererRegistry.INSTANCE.get(mainHandItem.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
                renderer.triggerAnimation(mainHandItem, GunAnimationConstant.INPUT_INSPECT);
            }
            return;
        }
        // 상태 잠금 확인
        if (data.clientStateLock) {
            return;
        }
        GunData gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).map(ClientGunIndex::getGunData).orElse(null);
        if (gunData == null) {
            return;
        }
        TimelessAPI.getGunDisplay(mainHandItem).ifPresent(gunIndex -> {
            Bolt boltType = gunData.getBolt();
            boolean noAmmo;
            if (boltType == Bolt.OPEN_BOLT) {
                noAmmo = iGun.getCurrentAmmoCount(mainHandItem) <= 0;
            } else {
                noAmmo = !iGun.hasBulletInBarrel(mainHandItem);
            }
            // inspect를 일으키고 소리 재생을 멈춘다
            SoundPlayManager.stopPlayGunSound();
            SoundPlayManager.playInspectSound(player, gunIndex, noAmmo);
            var animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine != null) {
                animationStateMachine.trigger(GunAnimationConstant.INPUT_INSPECT);
            }
        });
    }
}

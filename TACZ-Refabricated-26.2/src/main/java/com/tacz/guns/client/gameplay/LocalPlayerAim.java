package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.network.message.ClientMessagePlayerAim;
import com.tacz.guns.resource.modifier.custom.AdsModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class LocalPlayerAim {
    private final LocalPlayerDataHolder data;
    private final LocalPlayer player;

    public LocalPlayerAim(LocalPlayerDataHolder data, LocalPlayer player) {
        this.data = data;
        this.player = player;
    }

    public void aim(boolean isAim) {
        // 우선 주 손으로 정한다
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        Identifier gunId = iGun.getGunId(mainHandItem);
        TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
            data.clientIsAiming = isAim;
            // 발사 모드 전환 패킷을 보내 서버에 알린다
            ClientPlayNetworking.send(new ClientMessagePlayerAim(isAim));
        });
    }

    public float getClientAimingProgress(float partialTicks) {
        return Mth.lerp(partialTicks, LocalPlayerDataHolder.oldAimingProgress, data.clientAimingProgress);
    }

    public boolean isAim() {
        return data.clientIsAiming;
    }

    public void tickAimingProgress() {
        ItemStack mainHandItem = player.getMainHandItem();
        // 주 손 아이템이 총기가 아니면 조준 상태를 끄고 aimingProgress를 0으로 만든 뒤 돌아간다.
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            data.clientAimingProgress = 0;
            LocalPlayerDataHolder.oldAimingProgress = 0;
            return;
        }
        // 총을 집어넣는 중이면 조준할 수 없다
        if (System.currentTimeMillis() - data.clientDrawTimestamp < 0) {
            data.clientIsAiming = false;
        }
        Identifier gunId = iGun.getGunId(mainHandItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresentOrElse(index -> {
            float alphaProgress = this.getAlphaProgress(index.getGunData());
            this.aimProgressCalculate(alphaProgress);
        }, () -> {
            data.clientAimingProgress = 0;
            LocalPlayerDataHolder.oldAimingProgress = 0;
        });
    }

    private void aimProgressCalculate(float alphaProgress) {
        LocalPlayerDataHolder.oldAimingProgress = data.clientAimingProgress;
        if (data.clientIsAiming) {
            // 조준하는 중이므로 aimingProgress를 늘린다
            data.clientAimingProgress += alphaProgress;
            if (data.clientAimingProgress > 1) {
                data.clientAimingProgress = 1;
            }
        } else {
            // 조준을 푸는 중이므로 aimingProgress를 줄인다
            data.clientAimingProgress -= alphaProgress;
            if (data.clientAimingProgress < 0) {
                data.clientAimingProgress = 0;
            }
        }
        data.clientAimingTimestamp = System.currentTimeMillis();
    }

    private float getAlphaProgress(GunData gunData) {
        float aimTime = gunData.getAimTime();
        IGunOperator operator = IGunOperator.fromLivingEntity(this.player);
        if (operator.getCacheProperty() != null) {
            aimTime = operator.getCacheProperty().<Float>getCache(AdsModifier.ID);
        }
        aimTime = Math.max(0, aimTime);
        return (System.currentTimeMillis() - data.clientAimingTimestamp + 1) / (aimTime * 1000);
    }
}

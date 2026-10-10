package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.event.common.GunFireSelectEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerFireSelect;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public class LocalPlayerFireSelect {
    private final LocalPlayerDataHolder data;
    private final LocalPlayer player;

    public LocalPlayerFireSelect(LocalPlayerDataHolder data, LocalPlayer player) {
        this.data = data;
        this.player = player;
    }

    public void fireSelect() {
        // 상태 잠금 확인
        if (data.clientStateLock) {
            return;
        }
        // 우선 주 손으로 정한다
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        GunFireSelectEvent fireSelectEvent = new GunFireSelectEvent(player, player.getMainHandItem(), LogicalSide.CLIENT);
        GunFireSelectEvent.CALLBACK.invoker().post(fireSelectEvent);
        if (fireSelectEvent.isCanceled()) {
            return;
        }

        TimelessAPI.getGunDisplay(mainHandItem).ifPresent(gunIndex -> {
            // 효과음 재생
            SoundPlayManager.playFireSelectSound(player, gunIndex);
            // 발사 모드 전환 패킷을 보내 서버에 알린다
            ClientPlayNetworking.send(new ClientMessagePlayerFireSelect());
            // 클라이언트 발사 모드 전환
            if (iGun instanceof AbstractGunItem logicGun) {
                logicGun.fireSelect(null, mainHandItem);
            }
            AttachmentPropertyManager.postChangeEvent(player, mainHandItem);
            // 애니메이션 상태 기계 상태 전이
            AnimationStateMachine<?> animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine != null) {
                animationStateMachine.trigger(GunAnimationConstant.INPUT_FIRE_SELECT);
            }
        });
    }
}

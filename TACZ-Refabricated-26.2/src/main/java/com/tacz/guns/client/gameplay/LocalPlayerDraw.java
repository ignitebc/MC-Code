package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.mixin.accessor.BlockableEventLoopAccessor;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunDrawEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerDrawGun;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.TimeUnit;

public class LocalPlayerDraw {
    private final LocalPlayerDataHolder data;
    private final LocalPlayer player;
    public boolean readyToDraw = false;

    public LocalPlayerDraw(LocalPlayerDataHolder data, LocalPlayer player) {
        this.data = data;
        this.player = player;
    }

    public void draw(ItemStack lastItem) {
        // 여러 매개변수 초기화
        this.resetData();

        // 여러 데이터 얻기
        ItemStack currentItem = player.getMainHandItem();
        long drawTime = System.currentTimeMillis() - data.clientDrawTimestamp;
        IGun currentGun = IGun.getIGunOrNull(currentItem);
        IGun lastGun = IGun.getIGunOrNull(lastItem);

        // draw 시간과 putAway 시간을 계산한다
        if (drawTime >= 0) {
            drawTime = getDrawTime(lastItem, lastGun, drawTime);
        }
        long putAwayTime = Math.abs(drawTime);

        // 패킷을 보내 서버에 알린다
        if (Minecraft.getInstance().gameMode != null) {
            Minecraft.getInstance().gameMode.ensureHasSentCarriedItem();
        }
        ClientPlayNetworking.send(new ClientMessagePlayerDrawGun());
        GunDrawEvent.CALLBACK.invoker().post(new GunDrawEvent(player, lastItem, currentItem, LogicalSide.CLIENT));

        // 집어넣는 중이 아닐 때만 집어넣을 수 있다
        if (drawTime >= 0) {
            doPutAway(lastItem, putAwayTime);
        }

        // 총 드는 애니메이션을 비동기로 재생한다
        if (currentGun != null) {
            doDraw(currentItem, putAwayTime);
            // 부착물 데이터 새로 고침
            AttachmentPropertyManager.postChangeEvent(player, currentItem);
        }
    }

    private void doDraw(ItemStack currentItem, long putAwayTime) {
        TimelessAPI.getGunDisplay(currentItem).ifPresent(display -> {
            // 예약된 draw 동작 취소
            if (data.drawFuture != null) {
                data.drawFuture.cancel(false);
            }
            // put away 시간에 맞춰 draw 동작을 예약한다(효과음만 재생하며, 일관성을 위해 상태 기계 초기화는 옮겼다)
            data.drawFuture = LocalPlayerDataHolder.SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
                ((BlockableEventLoopAccessor) Minecraft.getInstance()).tacz$submitAsync(() -> {
                    SoundPlayManager.stopPlayGunSound();
                    SoundPlayManager.playDrawSound(player, display);
                });
            }, putAwayTime, TimeUnit.MILLISECONDS);
        });
    }

    private void doPutAway(ItemStack lastItem, long putAwayTime) {
        if (BuiltinItemRendererRegistry.INSTANCE.get(lastItem.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
            renderer.tryExit(lastItem, putAwayTime);
        }
        TimelessAPI.getGunDisplay(lastItem).ifPresent(display -> {
            ((BlockableEventLoopAccessor) Minecraft.getInstance()).tacz$submitAsync(() -> {
                // 총 집어넣기 효과음 재생
                SoundPlayManager.stopPlayGunSound();
                SoundPlayManager.playPutAwaySound(player, display);
            });
        });
    }

    private long getDrawTime(ItemStack lastItem, IGun lastGun, long drawTime) {
        if (BuiltinItemRendererRegistry.INSTANCE.get(lastItem.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
            long putAwayTime = renderer.getPutAwayTime(lastItem);
            if (drawTime > putAwayTime) {
                drawTime = putAwayTime;
            }
            data.clientDrawTimestamp = System.currentTimeMillis() + drawTime;
        } else {
            drawTime = 0;
            data.clientDrawTimestamp = System.currentTimeMillis();
        }
        return drawTime;
    }

    private void resetData() {
        // 상태 잠금을 건다
        data.lockState(operator -> operator.getSynDrawCoolDown() > 0);
        // 클라이언트 shoot 시각 초기화
        data.isShootRecorded = true;
        data.clientShootTimestamp = -1;
        data.chargeProgress = 0;
        // 클라이언트 조준 상태 초기화
        data.clientIsAiming = false;
        data.clientAimingProgress = 0;
        LocalPlayerDataHolder.oldAimingProgress = 0;
        // 노리쇠 당기기 상태 초기화
        data.isBolting = false;
        // 총기 교체 시각 갱신
        if (data.clientDrawTimestamp == -1) {
            data.clientDrawTimestamp = System.currentTimeMillis();
        }
    }
}

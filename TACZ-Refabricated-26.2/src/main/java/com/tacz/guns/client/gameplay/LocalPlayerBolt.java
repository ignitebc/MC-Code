package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerBoltGun;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public class LocalPlayerBolt {
    private final LocalPlayerDataHolder data;
    private final LocalPlayer player;

    public LocalPlayerBolt(LocalPlayerDataHolder data, LocalPlayer player) {
        this.data = data;
        this.player = player;
    }

    public void bolt() {
        // 상태 잠금 확인
        if (data.clientStateLock) {
            return;
        }
        if (data.isBolting) {
            return;
        }
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        GunData gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).map(ClientGunIndex::getGunData).orElse(null);
        if (gunData == null) {
            return;
        }

        TimelessAPI.getGunDisplay(mainHandItem).ifPresent(display -> {
            IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
            // bolt 종류가 수동 장전(manual action)인지 확인한다
            Bolt boltType = gunData.getBolt();
            // 인벤토리 급탄인지
            boolean useInventoryAmmo = iGun.useInventoryAmmo(mainHandItem);
            // 약실에 탄이 있는지
            boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(mainHandItem) && boltType != Bolt.OPEN_BOLT;
            // 인벤토리에 탄이 남아 있는지(크리에이티브에서 인벤토리 예비 탄약을 소모하는지)
            boolean hasInventoryAmmo = iGun.hasInventoryAmmo(player, mainHandItem, gunOperator.needCheckAmmo());
            // 탄이 없다고 볼 조건(인벤토리 급탄이면서 인벤토리에 탄 없음 / 인벤토리 급탄이 아니면서 탄창 탄 수 < 1)
            boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo ||
                    !useInventoryAmmo && iGun.getCurrentAmmoCount(mainHandItem) < 1;
            if (boltType != Bolt.MANUAL_ACTION) {
                return;
            }
            // 약실에 탄약이 있는지 확인한다
            if (hasAmmoInBarrel) {
                return;
            }
            // 탄창에 탄이 있는지 확인한다
            if (noAmmo) {
                return;
            }
            // 상태 잠금을 건다
            data.lockState(IGunOperator::getSynIsBolting);
            data.isBolting = true;
            // 패킷을 보내 서버에 알린다
            ClientPlayNetworking.send(new ClientMessagePlayerBoltGun());
            // 애니메이션과 효과음 재생
            AnimationStateMachine<?> animationStateMachine = display.getAnimationStateMachine();
            if (animationStateMachine != null) {
                SoundPlayManager.playBoltSound(player, display);
                animationStateMachine.trigger(GunAnimationConstant.INPUT_BOLT);
            }
        });
    }

    public void tickAutoBolt() {
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            data.isBolting = false;
            return;
        }
        bolt();
        if (data.isBolting) {
            // 클라이언트에서는 약실에 탄약이 채워진 상태가 동기화되는 순간 bolt 과정이 완전히 끝난다
            if (iGun.hasBulletInBarrel(mainHandItem)) {
                data.isBolting = false;
            }
        }
    }
}

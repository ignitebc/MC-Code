package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.custom.AdsModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class LivingEntityAim {
    private final LivingEntity shooter;
    private final ShooterDataHolder data;

    public LivingEntityAim(LivingEntity shooter, ShooterDataHolder data) {
        this.shooter = shooter;
        this.data = data;
    }

    public void aim(boolean isAim) {
        data.isAiming = isAim;
    }

    public void zoom() {
        if (data.currentGunItem == null) {
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof IGun iGun)) {
            return;
        }
        Identifier scopeId = iGun.getAttachmentId(currentGunItem, AttachmentType.SCOPE);
        CompoundTag scopeTag = iGun.getAttachmentTag(currentGunItem, AttachmentType.SCOPE);
        if (!DefaultAssets.isEmptyAttachmentId(scopeId) && scopeTag != null) {
            TimelessAPI.getCommonAttachmentIndex(scopeId).ifPresent(index -> {
                int zoomNumber = AttachmentItemDataAccessor.getZoomNumberFromTag(scopeTag);
                ++zoomNumber;
                // 넘쳐서 음수가 되지 않게 한다
                zoomNumber = zoomNumber % (Integer.MAX_VALUE - 1);
                AttachmentItemDataAccessor.setZoomNumberToTag(scopeTag, zoomNumber);
            });
            // 18차 수정: 바꾼 tag를 <b>총기 NBT에 다시 써야</b> 한다.
            //
            // getAttachmentTag()가 돌려주는 것은 CustomData.copyTag()의 <b>사본</b>이라,
            // 위의 setZoomNumberToTag는 이 사본만 바꾼다. 다시 쓰지 않으면 아무것도 하지 않은 셈이다 —
            // 그래서 "가변 배율 조준경의 배율 전환 키가 전혀 반응하지 않음"으로 나타났다.
            //
            // 원본 1.21.1의 LivingEntityAim#zoom 52번째 줄에 이 문장이 있었는데, 이식할 때 빠뜨렸다.
            // setAttachmentTag 자체도 16차에야 되살렸고(그때도 통째로 사라져 있었다),
            // 되살린 뒤로 호출하는 곳이 없었는데, 여기가 유일한 쓰임새다.
            iGun.setAttachmentTag(currentGunItem, AttachmentType.SCOPE, scopeTag);
        }
    }

    public void tickAimingProgress() {
        // currentGunItem이 null이면 조준 상태를 취소하고 aimingProgress를 0으로 만든다.
        if (data.currentGunItem == null || !(data.currentGunItem.get().getItem() instanceof IGun iGun)) {
            data.aimingProgress = 0;
            data.aimingTimestamp = System.currentTimeMillis();
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        // gunIndex를 가져올 수 없으면 조준 상태를 취소하고 aimingProgress를 0으로 만든 뒤 돌아간다.
        Identifier gunId = iGun.getGunId(currentGunItem);
        Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
        if (gunIndexOptional.isEmpty()) {
            data.aimingProgress = 0;
            return;
        }
        GunData gunData = gunIndexOptional.get().getGunData();
        float aimTime = gunData.getAimTime();
        if (this.data.cacheProperty != null) {
            aimTime = this.data.cacheProperty.<Float>getCache(AdsModifier.ID);
        }
        aimTime = Math.max(0, aimTime);
        float alphaProgress = (System.currentTimeMillis() - data.aimingTimestamp + 1) / (aimTime * 1000);
        if (data.isAiming) {
            // 조준하는 중이므로 aimingProgress를 늘린다
            data.aimingProgress += alphaProgress;
            if (data.aimingProgress > 1) {
                data.aimingProgress = 1;
            }
        } else {
            // 조준을 푸는 중이므로 aimingProgress를 줄인다
            data.aimingProgress -= alphaProgress;
            if (data.aimingProgress < 0) {
                data.aimingProgress = 0;
            }
        }
        data.aimingTimestamp = System.currentTimeMillis();
    }

    public void tickSprint() {
        IGunOperator operator = IGunOperator.fromLivingEntity(shooter);
        ReloadState reloadState = operator.getSynReloadState();
        if (data.isAiming || (reloadState.getStateType().isReloading() && !reloadState.getStateType().isReloadFinishing())) {
            shooter.setSprinting(false);
        }
        if (data.sprintTimestamp == -1) {
            data.sprintTimestamp = System.currentTimeMillis();
        }
        if (data.currentGunItem == null) {
            return;
        }
        ItemStack gunItem = data.currentGunItem.get();
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return;
        }
        TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem)).ifPresentOrElse(gunIndex -> {
            float gunSprintTime = gunIndex.getGunData().getSprintTime();
            if (shooter.isSprinting() && !shooter.isCrouching()) {
                data.lastSprintTimestamp = System.currentTimeMillis();
                data.sprintTimeS += (System.currentTimeMillis() - data.sprintTimestamp) / 1000f;
                if (data.sprintTimeS > gunSprintTime) {
                    data.sprintTimeS = gunSprintTime;
                }
            } else {
                data.sprintTimeS -= (System.currentTimeMillis() - data.sprintTimestamp) / 1000f;
                if (data.sprintTimeS < 0) {
                    data.sprintTimeS = 0;
                }
            }
        }, () -> data.sprintTimeS = 0);
        data.sprintTimestamp = System.currentTimeMillis();
    }
}

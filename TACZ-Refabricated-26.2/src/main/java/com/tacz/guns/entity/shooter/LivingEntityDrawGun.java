package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunDrawEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunDraw;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Supplier;

public class LivingEntityDrawGun {
    private final LivingEntity shooter;
    private final ShooterDataHolder data;

    public LivingEntityDrawGun(LivingEntity shooter, ShooterDataHolder data) {
        this.shooter = shooter;
        this.data = data;
    }

    public void draw(Supplier<ItemStack> gunItemSupplier) {
        // 각 상태 초기화
        data.initialData();

        // 총기 교체 시각 갱신
        if (data.drawTimestamp == -1) {
            data.drawTimestamp = System.currentTimeMillis();
        }
        if (data.heatTimestamp == -1) {
            data.heatTimestamp = System.currentTimeMillis();
        }
        long drawTime = System.currentTimeMillis() - data.drawTimestamp;
        if (drawTime >= 0) {
            // 총을 집어넣는 상태가 아니면 집어넣기 시간을 계산해야 한다
            if (drawTime < data.currentPutAwayTimeS * 1000) {
                // 총 바꾸기 시작부터 지금까지 총을 든 시간이 집어넣기에 필요한 시간보다 짧으면 든 시간으로 계산한다.
                data.drawTimestamp = System.currentTimeMillis() + drawTime;
            } else {
                // 총 바꾸기 시작부터 지금까지 총을 든 시간이 집어넣기에 필요한 시간보다 길면 집어넣기 시간으로 계산한다.
                data.drawTimestamp = System.currentTimeMillis() + (long) (data.currentPutAwayTimeS * 1000);
            }
        }
        ItemStack lastItem = data.currentGunItem == null ? ItemStack.EMPTY : data.currentGunItem.get();
        GunDrawEvent.CALLBACK.invoker().post(new GunDrawEvent(shooter, lastItem, gunItemSupplier.get(), LogicalSide.SERVER));
        NetworkHandler.sendToTrackingEntity(new ServerMessageGunDraw(shooter.getId(), lastItem, gunItemSupplier.get()), shooter);
        data.currentGunItem = gunItemSupplier;
        // 부착물 데이터 새로 고침
        AttachmentPropertyManager.postChangeEvent(shooter, gunItemSupplier.get());
        updatePutAwayTime();
    }

    public long getDrawCoolDown() {
        if (data.currentGunItem == null) {
            return 0;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof IGun iGun)) {
            return 0;
        }
        Identifier gunId = iGun.getGunId(currentGunItem);
        Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
        return gunIndex.map(index -> {
            long coolDown = (long) (index.getGunData().getDrawTime() * 1000) - (System.currentTimeMillis() - data.drawTimestamp);
            // 지연을 상쇄하려고 5 ms 여유 시간을 준다
            coolDown = coolDown - 5;
            if (coolDown < 0) {
                return 0L;
            }
            return coolDown;
        }).orElse(-1L);
    }

    private void updatePutAwayTime() {
        ItemStack gunItem = data.currentGunItem == null ? ItemStack.EMPTY : data.currentGunItem.get();
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun != null) {
            Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem));
            data.currentPutAwayTimeS = gunIndex.map(index -> index.getGunData().getPutAwayTime()).orElse(0F);
        } else {
            data.currentPutAwayTimeS = 0;
        }
    }
}

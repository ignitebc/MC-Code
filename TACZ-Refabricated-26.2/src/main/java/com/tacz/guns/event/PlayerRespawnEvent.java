package com.tacz.guns.event;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.pojo.data.gun.FeedType;
import net.minecraft.server.level.ServerPlayer;

public class PlayerRespawnEvent {
    public static void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        // 부활 시 자동 재장전
        if (!GunConfig.AUTO_RELOAD_WHEN_RESPAWN.get()) return;

        newPlayer.getInventory().forEach(itemStack -> {
            if (!(itemStack.getItem() instanceof IGun)) return;

            var api = new ModernKineticGunScriptAPI();
            api.setItemStack(itemStack);
            api.setShooter(newPlayer);

            // getGunIndex()가 null일 수 있으므로(총기 팩 미로드/ID 불일치) 반드시 방어한다
            var gunIndex = api.getGunIndex();
            if (gunIndex == null) return;

            // 인벤토리 직접 장전 방식 특수 처리
            var reloadType = gunIndex.getGunData().getReloadData().getType();
            var useInventoryAmmo = reloadType == FeedType.INVENTORY;
            // 인벤토리 직접 장전 방식이면 재장전하지 않는다
            if (useInventoryAmmo) {
                return;
            }

            // 연료 종류 특수 처리
            var isFuel = reloadType == FeedType.FUEL;
            int needAmmoCount = api.getNeededAmmoAmount();

            if (newPlayer.isCreative()) {
                api.putAmmoInMagazine(needAmmoCount);
            } else {
                int consumedAmount = api.consumeAmmoFromPlayer(isFuel ? 1 : needAmmoCount);
                api.putAmmoInMagazine(isFuel ? (needAmmoCount * consumedAmount) : consumedAmount);
            }
        });
    }
}

package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.mixin.ForcePoseInjection;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LivingEntityCrawl {
    private final LivingEntity shooter;
    private final ShooterDataHolder data;

    public LivingEntityCrawl(LivingEntity shooter, ShooterDataHolder data) {
        this.shooter = shooter;
        this.data = data;
    }

    public void crawl(boolean isCrawl) {
        data.isCrawling = isCrawl;
    }

    public void tickCrawling() {
        // currentGunItem이 null이면 엎드린 상태를 취소한다
        if (data.currentGunItem == null || !(data.currentGunItem.get().getItem() instanceof IGun iGun)) {
            data.isCrawling = false;
            this.setCrawlPose();
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        // 엎드릴 수 없는 무기면 엎드린 상태를 푼다
        if (!iGun.isCanCrawl(currentGunItem)) {
            data.isCrawling = false;
            this.setCrawlPose();
            return;
        }
        // gunIndex를 얻지 못하면 엎드린 상태를 푼다
        Identifier gunId = iGun.getGunId(currentGunItem);
        if (TimelessAPI.getCommonGunIndex(gunId).isEmpty()) {
            data.isCrawling = false;
            this.setCrawlPose();
            return;
        }
        // 관전자 모드, 탑승, 점프, 수영 중이거나 땅에 있지 않으면 취소한다
        if (shooter.isSpectator() || shooter.isPassenger() || shooter.jumping || shooter.isSwimming() || !shooter.onGround()) {
            data.isCrawling = false;
            this.setCrawlPose();
            return;
        }
        this.setCrawlPose();
    }

    private void setCrawlPose() {
        if (data.isCrawling) {
            if (shooter instanceof Player player) {
                ((ForcePoseInjection) player).tacz$setForcedPose(Pose.SWIMMING);
            } else {
                this.shooter.setPose(Pose.SWIMMING);
            }
        } else {
            if (shooter instanceof Player player) {
                ((ForcePoseInjection) player).tacz$setForcedPose(null);
            }
        }
    }
}

package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.mixin.ForcePoseInjection;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.network.message.ClientMessagePlayerCrawl;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;

public class LocalPlayerCrawl {
    /**
     * 대기 시간은 10틱
     */
    private static final int COOLDOWN_TICKS = 10;
    private final LocalPlayer player;
    private boolean isCrawling = false;
    private int crawCooldownTicks = 0;

    public LocalPlayerCrawl(LocalPlayer player) {
        this.player = player;
    }

    public void crawl(boolean isCrawl) {
        // 총을 들고 있어야 엎드리기 키가 동작한다
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        // 엎드릴 수 없는 무기
        if (!iGun.isCanCrawl(mainHandItem)) {
            return;
        }
        // 대기 시간이 지나지 않았으면 실행하지 않는다
        if (crawCooldownTicks > 0) {
            return;
        }
        if (player.isSpectator() || player.isPassenger() || !player.onGround()) {
            return;
        }
        Identifier gunId = iGun.getGunId(mainHandItem);
        TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
            this.isCrawling = isCrawl;
            this.crawCooldownTicks = COOLDOWN_TICKS;
            ClientPlayNetworking.send(new ClientMessagePlayerCrawl(isCrawl));
        });
    }

    public void tickCrawl() {
        if (crawCooldownTicks > 0) {
            crawCooldownTicks--;
        }
        // 총을 들고 있어야 엎드리기 키가 동작한다
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            isCrawling = false;
            this.setCrawlPose();
            return;
        }
        // 엎드릴 수 없는 무기면 엎드린 상태를 푼다
        if (!iGun.isCanCrawl(mainHandItem)) {
            isCrawling = false;
            this.setCrawlPose();
            return;
        }
        // gunIndex를 얻지 못하면 엎드린 상태를 푼다
        Identifier gunId = iGun.getGunId(mainHandItem);
        if (TimelessAPI.getCommonGunIndex(gunId).isEmpty()) {
            isCrawling = false;
            this.setCrawlPose();
            return;
        }
        // 플레이어가 관전자 모드이거나 탈것에 타고 있거나 점프·수영 중이거나 땅에 없으면 취소한다
        if (player.isSpectator() || player.isPassenger() || player.jumping || player.isSwimming() || !player.onGround()) {
            isCrawling = false;
            this.setCrawlPose();
            return;
        }
        this.setCrawlPose();
    }

    public boolean isCrawling() {
        return isCrawling;
    }

    private void setCrawlPose() {
        if (isCrawling) {
            ((ForcePoseInjection) player).tacz$setForcedPose(Pose.SWIMMING);
        } else {
            ((ForcePoseInjection) player).tacz$setForcedPose(null);
        }
    }
}

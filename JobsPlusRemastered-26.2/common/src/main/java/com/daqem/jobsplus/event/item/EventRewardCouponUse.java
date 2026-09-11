package com.daqem.jobsplus.event.item;

import com.daqem.jobsplus.event.player.EventRewardCouponEffectSync;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.coupon.RewardCouponLedger;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public final class EventRewardCouponUse
{
    private static final Identifier EXPERIENCE_DOUBLE_COUPON = Identifier.fromNamespaceAndPath("advancednetherite", "experience_double_coupon");
    private static final Identifier EXPERIENCE_TRIPLE_COUPON = Identifier.fromNamespaceAndPath("advancednetherite", "experience_triple_coupon");
    private static final Identifier BITCOIN_DOUBLE_COUPON = Identifier.fromNamespaceAndPath("advancednetherite", "bitcoin_double_coupon");
    private static final Identifier BITCOIN_TRIPLE_COUPON = Identifier.fromNamespaceAndPath("advancednetherite", "bitcoin_triple_coupon");

    private EventRewardCouponUse()
    {
    }

    public static void registerEvent()
    {
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (!(player instanceof ServerPlayer serverPlayer)
                    || !(serverPlayer instanceof JobsServerPlayer))
            {
                return EventResult.pass();
            }

            ItemStack stack = serverPlayer.getItemInHand(hand);
            if (stack.isEmpty())
            {
                return EventResult.pass();
            }

            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            boolean experienceTripleCoupon = EXPERIENCE_TRIPLE_COUPON.equals(itemId);
            boolean experienceCoupon = EXPERIENCE_DOUBLE_COUPON.equals(itemId) || experienceTripleCoupon;
            boolean bitcoinTripleCoupon = BITCOIN_TRIPLE_COUPON.equals(itemId);
            boolean bitcoinCoupon = BITCOIN_DOUBLE_COUPON.equals(itemId) || bitcoinTripleCoupon;
            if (!experienceCoupon && !bitcoinCoupon)
            {
                return EventResult.pass();
            }

            if (serverPlayer.getCooldowns().isOnCooldown(stack))
            {
                return EventResult.fromMinecraft(InteractionResult.CONSUME);
            }

            MinecraftServer server = serverPlayer.level().getServer();
            if (server == null)
            {
                return EventResult.pass();
            }

            RewardCouponLedger ledger = RewardCouponLedger.get(server);
            long expiresAt;
            String couponName;
            if (experienceCoupon)
            {
                int multiplier = experienceTripleCoupon ? 3 : 2;
                expiresAt = experienceTripleCoupon
                        ? ledger.activateExperienceTriple(serverPlayer.getUUID())
                        : ledger.activateExperienceDouble(serverPlayer.getUUID());
                couponName = "직업 경험치 " + multiplier + "배 쿠폰";
                if (expiresAt == 0L)
                {
                    serverPlayer.sendSystemMessage(Component.literal("다른 배율의 경험치 쿠폰 효과가 끝난 후 사용해 주세요."), false);
                    serverPlayer.getCooldowns().addCooldown(stack, 20);
                    return EventResult.fromMinecraft(InteractionResult.CONSUME);
                }
            }
            else
            {
                int multiplier = bitcoinTripleCoupon ? 3 : 2;
                expiresAt = bitcoinTripleCoupon
                        ? ledger.activateBitcoinTriple(serverPlayer.getUUID())
                        : ledger.activateBitcoinDouble(serverPlayer.getUUID());
                couponName = "비트코인 획득 확률 " + multiplier + "배 쿠폰";
                if (expiresAt == 0L)
                {
                    serverPlayer.sendSystemMessage(Component.literal("다른 배율의 비트코인 쿠폰 효과가 끝난 후 사용해 주세요."), false);
                    serverPlayer.getCooldowns().addCooldown(stack, 20);
                    return EventResult.fromMinecraft(InteractionResult.CONSUME);
                }
            }

            // 사용 즉시 HUD에 상태 효과와 남은 시간을 표시한다.
            EventRewardCouponEffectSync.sync(serverPlayer);

            // 같은 스택을 누르고 있는 동안 중복 사용되지 않도록 먼저 쿨다운을 건다.
            serverPlayer.getCooldowns().addCooldown(stack, 20);
            stack.shrink(1);
            if (stack.isEmpty())
            {
                serverPlayer.setItemInHand(hand, ItemStack.EMPTY);
            }
            else
            {
                serverPlayer.setItemInHand(hand, stack);
            }
            serverPlayer.getInventory().setChanged();
            serverPlayer.containerMenu.broadcastChanges();

            long remainingSeconds = Math.max(0L, (expiresAt - System.currentTimeMillis() + 999L) / 1000L);
            long minutes = remainingSeconds / 60L;
            long seconds = remainingSeconds % 60L;
            serverPlayer.sendSystemMessage(Component.literal(
                    couponName + "이 활성화되었습니다. 남은 시간: " + minutes + "분 " + seconds + "초"), false);

            return EventResult.fromMinecraft(InteractionResult.CONSUME);
        });
    }
}

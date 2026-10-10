package com.daqem.jobsplus.event.item;

import com.autovw.advancednetherite.common.randombox.RandomBoxRewardFilters;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.metrics.MetricsEvent;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.EventResult;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public final class EventJobSelectTicketUse {

    private static final Identifier JOB_SELECT_TICKET_ID = Identifier.fromNamespaceAndPath("advancednetherite", "job_select_ticket");

    private EventJobSelectTicketUse() 
    {
    }

    public static void registerEvent() 
    {
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) 
            {
                return EventResult.pass();
            }

            ItemStack stack = serverPlayer.getItemInHand(hand);
            if (stack.isEmpty()) 
            {
                return EventResult.pass();
            }

            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!JOB_SELECT_TICKET_ID.equals(id)) 
            {
                return EventResult.pass();
            }

            if (!(serverPlayer instanceof JobsServerPlayer jobsServerPlayer)) 
            {
                return EventResult.pass();
            }

            // 우클릭 홀드로 연속 발동 방지(1초)
            if (serverPlayer.getCooldowns().isOnCooldown(stack)) 
            {
                return EventResult.fromMinecraft(InteractionResult.CONSUME);
            }
            serverPlayer.getCooldowns().addCooldown(stack, 20);

            // 상한 도달 시 추가 불가(소모도 안 함)
            if (isJobSlotCapReached(jobsServerPlayer))
            {
                serverPlayer.sendSystemMessage(JobsPlus.translatable("error.max_jobs_reached"), false);
                return EventResult.fromMinecraft(InteractionResult.CONSUME);
            }

            // 슬롯 +1
            int maxJobsBefore = jobsServerPlayer.jobsplus$getEffectiveMaxJobs();
            jobsServerPlayer.jobsplus$addExtraJobSlots(1);
            MetricsEvent.of("JOB_SLOT_ADD")
                    .player(serverPlayer)
                    .before(maxJobsBefore)
                    .after(jobsServerPlayer.jobsplus$getEffectiveMaxJobs())
                    .record();

            // 무조건 1개 소모
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

            // UI 즉시 갱신
            NetworkManager.sendToPlayer(
                    serverPlayer,
                    new ClientboundOpenJobsScreenPacket(
                            Stream.concat(jobsServerPlayer.jobsplus$getJobs().stream(),jobsServerPlayer.jobsplus$getInactiveJobs().stream()).toList(),
                            jobsServerPlayer.jobsplus$getCoins(),
                            jobsServerPlayer.jobsplus$getEffectiveMaxJobs(),
                            jobsServerPlayer.jobsplus$getStockAccount()
                    )
            );

            serverPlayer.sendSystemMessage(Component.literal("직업선택권 사용: 최대 직업 수 +1 (현재 최대: " + jobsServerPlayer.jobsplus$getEffectiveMaxJobs() + ")"),false);
            return EventResult.fromMinecraft(InteractionResult.CONSUME);
        });

        // 최대 직업 수가 상한에 닿아 쓸 수 없는 직업선택권은 랜덤 상자 보상 후보에서 뺀다.
        RandomBoxRewardFilters.register(EventJobSelectTicketUse::excludesFromRandomBox);
    }

    /**
     * 직업선택권으로 최대 직업 수를 더 늘릴 수 없는지.
     * 무료 직업 수와 직업선택권으로 늘린 칸의 합이 상한(max_jobs)에 닿았으면 true다.
     */
    public static boolean isJobSlotCapReached(JobsServerPlayer player)
    {
        int cap = Math.max(0, JobsPlusConfig.maxJobs.get());
        return player.jobsplus$getEffectiveMaxJobs() >= cap;
    }

    private static boolean excludesFromRandomBox(Player player, Identifier rewardItemId)
    {
        if (!JOB_SELECT_TICKET_ID.equals(rewardItemId))
        {
            return false;
        }
        if (!(player instanceof JobsServerPlayer jobsServerPlayer))
        {
            return false;
        }
        return isJobSlotCapReached(jobsServerPlayer);
    }
}

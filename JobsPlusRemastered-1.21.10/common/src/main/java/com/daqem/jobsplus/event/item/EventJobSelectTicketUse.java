package com.daqem.jobsplus.event.item;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public final class EventJobSelectTicketUse {

    private static final ResourceLocation JOB_SELECT_TICKET_ID = ResourceLocation.fromNamespaceAndPath("advancednetherite", "job_select_ticket");

    private EventJobSelectTicketUse() 
    {
    }

    public static void registerEvent() 
    {
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) 
            {
                return InteractionResult.PASS;
            }

            ItemStack stack = serverPlayer.getItemInHand(hand);
            if (stack.isEmpty()) 
            {
                return InteractionResult.PASS;
            }

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!JOB_SELECT_TICKET_ID.equals(id)) 
            {
                return InteractionResult.PASS;
            }

            if (!(serverPlayer instanceof JobsServerPlayer jobsServerPlayer)) 
            {
                return InteractionResult.PASS;
            }

            // 우클릭 홀드로 연속 발동 방지(1초)
            if (serverPlayer.getCooldowns().isOnCooldown(stack)) 
            {
                return InteractionResult.CONSUME;
            }
            serverPlayer.getCooldowns().addCooldown(stack, 20);

            // 상한 도달 시 추가 불가(소모도 안 함)
            int base = Math.max(0, JobsPlusConfig.amountOfFreeJobs.get());
            int cap = Math.max(0, JobsPlusConfig.maxJobs.get());
            int extra = Math.max(0, jobsServerPlayer.jobsplus$getExtraJobSlots());
            int currentMax = (int) Math.min((long) cap, (long) base + (long) extra);
            if (currentMax >= cap) 
            {
                serverPlayer.sendSystemMessage(JobsPlus.translatable("error.max_jobs_reached"), false);
                return InteractionResult.CONSUME;
            }

            // 슬롯 +1
            jobsServerPlayer.jobsplus$addExtraJobSlots(1);

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
                            jobsServerPlayer.jobsplus$getEffectiveMaxJobs()
                    )
            );

            serverPlayer.sendSystemMessage(Component.literal("직업선택권 사용: 최대 직업 수 +1 (현재 최대: " + jobsServerPlayer.jobsplus$getEffectiveMaxJobs() + ")"),false);
            return InteractionResult.CONSUME;
        });
    }
}

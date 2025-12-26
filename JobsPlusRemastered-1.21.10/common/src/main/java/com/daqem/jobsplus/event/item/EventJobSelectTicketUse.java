package com.daqem.jobsplus.event.item;

import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/**
 * AdvancedNetherite의 job_select_ticket 사용 시
 * - 플레이어별 extra_job_slots를 +1 (상한 없음)
 * - 아이템 1개 소모(크리에이티브 제외)
 */
public final class EventJobSelectTicketUse {
    private static final ResourceLocation JOB_SELECT_TICKET_ID = ResourceLocation
            .fromNamespaceAndPath("advancednetherite", "job_select_ticket");

    private EventJobSelectTicketUse() 
    {
    }

    public static void registerEvent() {
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            // 클라/레벨 필드 접근 이슈 피하려고 ServerPlayer로만 처리
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

            if (serverPlayer instanceof JobsServerPlayer jobsServerPlayer) 
            {
                jobsServerPlayer.jobsplus$addExtraJobSlots(1);

                if (!serverPlayer.getAbilities().instabuild) 
                {
                    stack.shrink(1);
                }

                // 안내 메시지 (원하면 번역키로 바꿔도 됨)
                serverPlayer.sendSystemMessage(
                        Component.literal("직업추가권 사용: 최대 직업 수 +1 (현재 최대: " + jobsServerPlayer.jobsplus$getEffectiveMaxJobs() + ")"), false);
            }

            return InteractionResult.SUCCESS;
        });
    }
}

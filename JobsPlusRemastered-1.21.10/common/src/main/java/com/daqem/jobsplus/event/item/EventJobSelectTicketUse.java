package com.daqem.jobsplus.event.item;

import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/**
 * AdvancedNetherite의 "직업선택권(advancednetherite:job_select_ticket)" 사용 처리.
 *
 * 요구사항:
 * - 플레이어가 티켓을 든 채로 우클릭하면, 플레이어별 최대 직업 수를 +1 한다.
 * - 전역 설정(JobsPlusConfig.maxJobs)은 건드리지 않고, 플레이어 NBT에 저장되는 추가 슬롯으로 확장한다.
 *
 * 의존성 주의:
 * - AdvancedNetherite 모드에 대한 컴파일 의존을 추가하지 않기 위해, 아이템은 레지스트리 ID 문자열로만 판별한다.
 */
public final class EventJobSelectTicketUse {

    private static final ResourceLocation JOB_SELECT_TICKET_ID = ResourceLocation
            .fromNamespaceAndPath("advancednetherite", "job_select_ticket");

    private EventJobSelectTicketUse() {
    }

    public static void registerEvent() {
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            // Level#isClientSide 접근 이슈를 피하기 위해, 서버 플레이어로만 한정한다.
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = serverPlayer.getItemInHand(hand);
            if (stack.isEmpty()) {
                return InteractionResult.PASS;
            }

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!JOB_SELECT_TICKET_ID.equals(id)) {
                return InteractionResult.PASS;
            }

            if (serverPlayer instanceof JobsServerPlayer jobsServerPlayer) {
                jobsServerPlayer.jobsplus$addExtraJobSlots(1);

                if (!serverPlayer.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                serverPlayer.displayClientMessage(
                        Component.literal("직업선택권을 사용했습니다. 최대 직업 수가 1 증가했습니다. (현재 최대: "
                                + jobsServerPlayer.jobsplus$getEffectiveMaxJobs() + ")"),
                        false);
            }

            return InteractionResult.SUCCESS;
        });
    }
}

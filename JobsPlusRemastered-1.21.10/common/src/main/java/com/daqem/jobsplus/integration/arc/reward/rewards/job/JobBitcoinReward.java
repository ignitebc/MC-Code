package com.daqem.jobsplus.integration.arc.reward.rewards.job;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class JobBitcoinReward extends AbstractReward
{

    // 지급할 비트코인 개수
    private final int amount;

    public JobBitcoinReward(double chance, int priority, int amount)
    {
        // chance: 발동 확률(%), priority: 리워드 우선순위
        super(chance, priority);
        this.amount = amount;
    }

    @Override
    public IRewardType<?> getType()
    {
        // Reward 타입 등록 키 (jobsplus:bitcoin_reward)
        return JobsPlusRewardType.BITCOIN_REWARD;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        // Arc 쪽 플레이어 래퍼 가져오기
        ArcPlayer arcPlayer = actionData.getPlayer();
        if (!(arcPlayer instanceof JobsServerPlayer jobsServerPlayer))
        {
            // 서버 플레이어가 아니면 아무 것도 안 함
            return new ActionResult();
        }

        // 실제 Minecraft 서버 플레이어 객체
        ServerPlayer serverPlayer = jobsServerPlayer.jobsplus$getServerPlayer();
        if (serverPlayer == null)
        {
            return new ActionResult();
        }

        // 1) 비트코인 아이템 지급 (AdvancedNetherite 모드 아이템)
        // - 1.21 계열에서는 ResourceLocation.parse 사용
        ResourceLocation bitcoinId = ResourceLocation.parse("advancednetherite:bitcoin");

        // - BuiltInRegistries.ITEM.get(...) 은 Optional<Holder.Reference<Item>> 를 반환
        Optional<Holder.Reference<Item>> optionalHolder = BuiltInRegistries.ITEM.get(bitcoinId);
        if (optionalHolder.isEmpty())
        {
            // 비트코인 아이템이 레지스트리에 없으면 그냥 종료
            return new ActionResult();
        }

        // Holder.Reference<Item> 에서 실제 Item 인스턴스 꺼내기
        Item bitcoinItem = optionalHolder.get().value();

        // 지급할 ItemStack 생성
        ItemStack stack = new ItemStack(bitcoinItem, this.amount);

        // 인벤토리에 추가 시도
        boolean added = serverPlayer.addItem(stack);
        if (!added)
        {
            // 인벤토리가 가득 차면 바닥에 드랍
            serverPlayer.drop(stack, false);
        }

        // 2) 서버 전체 브로드캐스트 메시지
        if (serverPlayer.level().getServer() != null)
        {
            serverPlayer.level().getServer().getPlayerList().broadcastSystemMessage(
                    // jobsplus.bitcoin.obtained: "%s님이 비트코인을 획득했습니다!"
                    JobsPlus.translatable("bitcoin.obtained", serverPlayer.getName().copy()
                            // 플레이어 이름을 골드색으로 표시
                            .withStyle(style -> style.withColor(0xFFD700))),
                    false // 액션바가 아니라 일반 채팅으로 브로드캐스트
            );
        }

        return new ActionResult();
    }


    public static class Serializer implements IRewardSerializer<JobBitcoinReward>
    {

        @Override
        public JobBitcoinReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            // JSON에서 amount 읽기 (기본값 1)
            int amount = GsonHelper.getAsInt(jsonObject, "amount", 1);
            return new JobBitcoinReward(chance, priority, amount);
        }

        @Override
        public JobBitcoinReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority)
        {
            // 네트워크 패킷에서 amount 복원
            int amount = friendlyByteBuf.readInt();
            return new JobBitcoinReward(chance, priority, amount);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, JobBitcoinReward type)
        {
            // 기본 필드(chance, priority)는 super.toNetwork 에서 처리
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            // 커스텀 필드 amount 전송
            friendlyByteBuf.writeInt(type.amount);
        }
    }
}

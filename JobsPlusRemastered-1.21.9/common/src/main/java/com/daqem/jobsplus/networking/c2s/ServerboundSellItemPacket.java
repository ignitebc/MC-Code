package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ServerboundSellItemPacket implements CustomPacketPayload
{

    private final Item item;
    private final int requiredAmount;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSellItemPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ServerboundSellItemPacket decode(RegistryFriendlyByteBuf buf)
        {
            return new ServerboundSellItemPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerboundSellItemPacket packet)
        {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(packet.item));
            buf.writeInt(packet.requiredAmount);
        }
    };

    public ServerboundSellItemPacket(Item item, int requiredAmount)
    {
        this.item = item;
        this.requiredAmount = requiredAmount;
    }

    public ServerboundSellItemPacket(RegistryFriendlyByteBuf friendlyByteBuf)
    {
        ResourceLocation itemId = friendlyByteBuf.readResourceLocation();
        this.item = BuiltInRegistries.ITEM.get(itemId);
        this.requiredAmount = friendlyByteBuf.readInt();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.SERVERBOUND_SELL_ITEM;
    }

    public static void handleServerSide(ServerboundSellItemPacket packet, NetworkManager.PacketContext context)
    {
        if (context.getPlayer() instanceof JobsServerPlayer serverPlayer)
        {
            ServerPlayer player = serverPlayer.jobsplus$getServerPlayer();
            
            // 플레이어 인벤토리에서 해당 아이템 개수 확인
            int itemCount = 0;
            for (ItemStack stack : player.getInventory().items)
            {
                if (stack.getItem() == packet.item)
                {
                    itemCount += stack.getCount();
                }
            }

            // 필요한 개수만큼 있는지 확인
            if (itemCount < packet.requiredAmount)
            {
                player.sendSystemMessage(JobsPlus.translatable("error.not_enough_items", packet.item.getName(new ItemStack(packet.item)), packet.requiredAmount));
                return;
            }

            // 인벤토리에서 아이템 제거
            int remainingToRemove = packet.requiredAmount;
            for (int i = 0; i < player.getInventory().items.size() && remainingToRemove > 0; i++)
            {
                ItemStack stack = player.getInventory().items.get(i);
                if (stack.getItem() == packet.item)
                {
                    int removeAmount = Math.min(stack.getCount(), remainingToRemove);
                    stack.shrink(removeAmount);
                    remainingToRemove -= removeAmount;
                }
            }

            // 코인 지급 (1개)
            serverPlayer.jobsplus$setCoins(serverPlayer.jobsplus$getCoins() + 1);

            // 화면 업데이트
            NetworkManager.sendToPlayer(player, new ClientboundOpenJobsScreenPacket(
                    Stream.concat(serverPlayer.jobsplus$getJobs().stream(), serverPlayer.jobsplus$getInactiveJobs().stream()).toList(),
                    serverPlayer.jobsplus$getCoins()
            ));

            player.sendSystemMessage(JobsPlus.translatable("gui.jobs.shop.sold", packet.requiredAmount, packet.item.getName(new ItemStack(packet.item)), 1));
        }
    }
}


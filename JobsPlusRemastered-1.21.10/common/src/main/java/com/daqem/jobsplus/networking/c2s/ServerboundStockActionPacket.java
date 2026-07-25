package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.client.gui.jobs.stock.StockMarketService;
import com.daqem.jobsplus.client.gui.jobs.stock.StockQuote;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundStockAlertPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.stock.StockAccount;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public class ServerboundStockActionPacket implements CustomPacketPayload
{
    private static final ResourceLocation BITCOIN_ID = ResourceLocation.parse("advancednetherite:bitcoin");
    private static final double SELL_FEE_RATE = 0.00015D;
    private static final double WITHDRAW_TAX_RATE = 0.002D;

    private final Action action;
    private final String stockId;
    private final int amount;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStockActionPacket> STREAM_CODEC =
            new StreamCodec<>()
            {
                @Override
                public @NotNull ServerboundStockActionPacket decode(RegistryFriendlyByteBuf buffer)
                {
                    return new ServerboundStockActionPacket(buffer);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ServerboundStockActionPacket packet)
                {
                    buffer.writeEnum(packet.action);
                    buffer.writeUtf(packet.stockId);
                    buffer.writeInt(packet.amount);
                }
            };

    public ServerboundStockActionPacket(Action action, String stockId, int amount)
    {
        this.action = action;
        this.stockId = stockId;
        this.amount = amount;
    }

    public ServerboundStockActionPacket(RegistryFriendlyByteBuf buffer)
    {
        this.action = buffer.readEnum(Action.class);
        this.stockId = buffer.readUtf();
        this.amount = buffer.readInt();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.SERVERBOUND_STOCK_ACTION;
    }

    public static void handleServerSide(ServerboundStockActionPacket packet, NetworkManager.PacketContext context)
    {
        if (!(context.getPlayer() instanceof JobsServerPlayer jobsServerPlayer) || packet.amount <= 0)
        {
            return;
        }

        ServerPlayer player = jobsServerPlayer.jobsplus$getServerPlayer();
        StockAccount account = jobsServerPlayer.jobsplus$getStockAccount();
        Optional<Holder.Reference<Item>> bitcoinHolder = BuiltInRegistries.ITEM.get(BITCOIN_ID);
        if (bitcoinHolder.isEmpty())
        {
            player.sendSystemMessage(Component.literal("비트코인 아이템을 찾을 수 없습니다."));
            return;
        }

        Item bitcoinItem = bitcoinHolder.get().value();
        String completedMessage = null;
        switch (packet.action)
        {
            case DEPOSIT -> {
                if (!isValidTransferAmount(packet.amount))
                {
                    player.sendSystemMessage(Component.literal("입금은 10개 단위로만 가능합니다."));
                    return;
                }
                if (countItem(player.getInventory(), bitcoinItem) < packet.amount)
                {
                    player.sendSystemMessage(Component.literal("소지품에 비트코인이 부족합니다."));
                    return;
                }
                removeItem(player.getInventory(), bitcoinItem, packet.amount);
                account = account.deposit(packet.amount);
                completedMessage = packet.amount + "개가 입금되었습니다.";
            }
            case WITHDRAW -> {
                if (!isValidTransferAmount(packet.amount))
                {
                    player.sendSystemMessage(Component.literal("출금은 10개 단위로만 가능합니다."));
                    return;
                }
                double taxAmount = packet.amount * WITHDRAW_TAX_RATE;
                double requiredBalance = packet.amount + taxAmount;
                if (account.balance() + 0.00000001 < requiredBalance)
                {
                    NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                            "0.2% 소득세를 포함한 보유 자산이 부족하여 출금할 수 없습니다."));
                    return;
                }
                player.getInventory().placeItemBackInInventory(new ItemStack(bitcoinItem, packet.amount), true);
                account = account.withdraw(packet.amount, taxAmount);
                completedMessage = packet.amount + "개가 출금되었습니다.";
            }
            case BUY -> {
                StockQuote quote = getAvailableQuote(packet.stockId);
                if (quote == null)
                {
                    player.sendSystemMessage(Component.literal("현재 종목 시세를 불러온 후 다시 시도해 주세요."));
                    return;
                }
                if (account.balance() + 0.00000001 < packet.amount)
                {
                    NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                            "충전된 비트코인이 부족하여 구매할 수 없습니다.\n입출금 탭에서 비트코인을 충전해 주세요."));
                    return;
                }
                account = account.buy(packet.stockId, packet.amount, quote.priceKrw());
                completedMessage = quote.name() + "를 구매했습니다.";
            }
            case SELL -> {
                StockQuote quote = getAvailableQuote(packet.stockId);
                if (quote == null)
                {
                    player.sendSystemMessage(Component.literal("현재 종목 시세를 불러온 후 다시 시도해 주세요."));
                    return;
                }
                if (account.getPosition(packet.stockId) == null
                        || account.getPosition(packet.stockId).quantity() + 0.00000001 < packet.amount)
                {
                    player.sendSystemMessage(Component.literal("판매 가능한 주식 수량이 부족합니다."));
                    return;
                }
                account = account.sell(packet.stockId, packet.amount, quote.priceKrw(), SELL_FEE_RATE);
                completedMessage = quote.name() + "를 판매했습니다.";
            }
        }

        jobsServerPlayer.jobsplus$setStockAccount(account);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        syncScreen(jobsServerPlayer);
        if (completedMessage != null)
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(completedMessage, "확인"));
        }
    }

    private static StockQuote getAvailableQuote(String stockId)
    {
        StockMarketService stockMarketService = StockMarketService.getInstance();
        stockMarketService.refreshIfNeeded();
        StockQuote quote = stockMarketService.getQuote(stockId);
        return quote != null && quote.available() && quote.priceKrw() > 0 ? quote : null;
    }

    private static boolean isValidTransferAmount(int amount)
    {
        return amount >= 10 && amount % 10 == 0;
    }

    private static int countItem(Inventory inventory, Item item)
    {
        int count = 0;
        for (int index = 0; index < inventory.getContainerSize(); index++)
        {
            ItemStack stack = inventory.getItem(index);
            if (!stack.isEmpty() && stack.is(item))
            {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItem(Inventory inventory, Item item, int amount)
    {
        int remaining = amount;
        for (int index = 0; index < inventory.getContainerSize() && remaining > 0; index++)
        {
            ItemStack stack = inventory.getItem(index);
            if (!stack.isEmpty() && stack.is(item))
            {
                int removed = Math.min(remaining, stack.getCount());
                stack.shrink(removed);
                remaining -= removed;
            }
        }
    }

    private static void syncScreen(JobsServerPlayer player)
    {
        NetworkManager.sendToPlayer(
                player.jobsplus$getServerPlayer(),
                new ClientboundOpenJobsScreenPacket(
                        Stream.concat(player.jobsplus$getJobs().stream(), player.jobsplus$getInactiveJobs().stream())
                                .toList(),
                        player.jobsplus$getCoins(),
                        player.jobsplus$getEffectiveMaxJobs(),
                        player.jobsplus$getStockAccount()
                )
        );
    }

    public enum Action
    {
        DEPOSIT,
        WITHDRAW,
        BUY,
        SELL
    }
}

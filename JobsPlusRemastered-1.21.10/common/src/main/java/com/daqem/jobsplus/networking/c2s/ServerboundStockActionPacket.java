package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.stock.StockMarketService;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockQuote;
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

    /**
     * 클라이언트가 화면에서 보고 있던 시세 스냅샷 번호.
     * 서버 스냅샷과 다르면 표시 가격과 체결 가격이 어긋나므로 거래를 진행하지 않는다.
     */
    private final long snapshotVersion;

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
                    buffer.writeLong(packet.snapshotVersion);
                }
            };

    public ServerboundStockActionPacket(Action action, String stockId, int amount, long snapshotVersion)
    {
        this.action = action;
        this.stockId = stockId;
        this.amount = amount;
        this.snapshotVersion = snapshotVersion;
    }

    public ServerboundStockActionPacket(RegistryFriendlyByteBuf buffer)
    {
        this.action = buffer.readEnum(Action.class);
        this.stockId = buffer.readUtf();
        this.amount = buffer.readInt();
        this.snapshotVersion = buffer.readLong();
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
                StockQuote quote = resolveTradableQuote(player, packet);
                if (quote == null)
                {
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
                StockQuote quote = resolveTradableQuote(player, packet);
                if (quote == null)
                {
                    return;
                }
                if (account.getPosition(packet.stockId) == null
                        || account.getPosition(packet.stockId).investedAmount() + 0.00000001 < packet.amount)
                {
                    player.sendSystemMessage(Component.literal("판매 가능한 투자 금액이 부족합니다."));
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

    /**
     * 체결에 사용할 시세를 서버 스냅샷에서 가져온다.
     * <p>
     * 시세 API를 다시 조회하지 않는다. 조회는 비동기라서 즉시 반영되지 않고, 무엇보다 클라이언트 표가
     * 보여 준 가격과 다른 가격으로 체결될 수 있기 때문이다. 대신 클라이언트가 보고 있던 스냅샷 번호를
     * 서버 스냅샷과 대조해서, 두 값이 다르면 거래를 진행하지 않고 새 가격을 확인하도록 안내한다.
     *
     * @return 체결 가능한 시세. 불가능하면 {@code null}이며 사유는 플레이어에게 전달된다.
     */
    private static StockQuote resolveTradableQuote(ServerPlayer player, ServerboundStockActionPacket packet)
    {
        StockMarketSnapshot snapshot = StockMarketService.getInstance().getSnapshot();
        if (snapshot.isEmpty())
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    "아직 시세를 불러오지 못했습니다.\n잠시 후 다시 시도해 주세요."));
            return null;
        }

        if (packet.snapshotVersion != snapshot.version())
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    "가격이 갱신되었습니다.\n새 가격을 확인한 후 다시 거래해 주세요."));
            return null;
        }

        StockQuote quote = snapshot.getQuote(packet.stockId);
        if (quote == null)
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    "거래할 수 없는 종목입니다."));
            return null;
        }

        if (!quote.isTradable(System.currentTimeMillis()))
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    quote.name() + " 시세 갱신이 지연되고 있습니다.\n가격이 다시 갱신된 후 거래해 주세요."));
            return null;
        }
        return quote;
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

package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.event.stock.StockMarketTicker;
import com.daqem.jobsplus.stock.StockMarketService;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockQuote;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundStockAlertPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundStockSnapshotPacket;
import com.daqem.jobsplus.stock.SnapshotStatus;
import com.daqem.jobsplus.stock.StockCatalog;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.PlayerItemDelivery;
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

    /** 종목 ID는 카탈로그에 정의된 짧은 문자열뿐이다. 조작된 패킷이 긴 문자열을 보내지 못하게 막는다. */
    private static final int MAX_STOCK_ID_LENGTH = 16;

    /** 계좌 검사 전에 비정상적으로 큰 수량을 걸러 낸다. */
    private static final int MAX_TRANSACTION_AMOUNT = 99_999_999;

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
        this.stockId = buffer.readUtf(MAX_STOCK_ID_LENGTH);
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
        if (!(context.getPlayer() instanceof JobsServerPlayer jobsServerPlayer))
        {
            return;
        }
        if (packet.amount <= 0 || packet.amount > MAX_TRANSACTION_AMOUNT)
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
                ItemStack withdrawnBitcoin = new ItemStack(bitcoinItem, packet.amount);
                PlayerItemDelivery.giveOrDrop(player, withdrawnBitcoin);
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
     * 보여 준 가격과 다른 가격으로 체결될 수 있기 때문이다. 대신 아래를 모두 확인한다.
     * <ul>
     *   <li>클라이언트가 보고 있던 스냅샷 번호가 서버 스냅샷과 같은지</li>
     *   <li>그 스냅샷이 현재 서버 분의 가격인지 (이전 분 가격으로 체결 금지)</li>
     *   <li>스냅샷 상태가 {@link SnapshotStatus#READY}인지 (조회 중이거나 실패면 거래 중지)</li>
     *   <li>해당 종목의 가격이 유한한 양수인지</li>
     * </ul>
     *
     * @return 체결 가능한 시세. 불가능하면 {@code null}이며 사유는 플레이어에게 전달된다.
     */
    private static StockQuote resolveTradableQuote(ServerPlayer player, ServerboundStockActionPacket packet)
    {
        // 주식 탭을 열어야만 서버가 시세를 갱신하므로, 탭을 열지 않은 요청은 조작된 패킷이다.
        if (!StockMarketTicker.isViewing(player))
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    "주식 탭을 연 상태에서만 거래할 수 있습니다."));
            return null;
        }

        StockMarketSnapshot snapshot = StockMarketService.getInstance().getSnapshot();

        if (packet.snapshotVersion != snapshot.version())
        {
            // 클라이언트가 갱신 패킷을 놓쳤을 수 있으므로 현재 스냅샷을 다시 보내 준다.
            rejectAndResync(player, snapshot, "가격이 갱신되었습니다.\n새 가격을 확인한 후 다시 거래해 주세요.");
            return null;
        }

        if (snapshot.marketMinute() != StockMarketSnapshot.currentMarketMinute())
        {
            rejectAndResync(player, snapshot, "시세 갱신 시점이 지났습니다.\n새 가격을 확인한 후 다시 거래해 주세요.");
            return null;
        }

        if (snapshot.status() != SnapshotStatus.READY)
        {
            String reason = snapshot.status() == SnapshotStatus.REFRESHING
                    ? "시세를 갱신하는 중입니다.\n잠시 후 다시 시도해 주세요."
                    : "시세를 불러오지 못했습니다.\n다음 갱신을 기다려 주세요.";
            rejectAndResync(player, snapshot, reason);
            return null;
        }

        // 스냅샷에 없는 종목은 물론, 카탈로그에 없는 ID도 거절한다.
        StockQuote quote = StockCatalog.getStock(packet.stockId) == null
                ? null
                : snapshot.getQuote(packet.stockId);
        if (quote == null)
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket("거래할 수 없는 종목입니다."));
            return null;
        }

        if (!quote.hasValidPrice())
        {
            rejectAndResync(player, snapshot,
                    quote.name() + " 시세를 불러오지 못했습니다.\n다음 갱신을 기다려 주세요.");
            return null;
        }
        return quote;
    }

    private static void rejectAndResync(ServerPlayer player, StockMarketSnapshot snapshot, String reason)
    {
        NetworkManager.sendToPlayer(player, new ClientboundStockSnapshotPacket(snapshot));
        NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(reason));
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

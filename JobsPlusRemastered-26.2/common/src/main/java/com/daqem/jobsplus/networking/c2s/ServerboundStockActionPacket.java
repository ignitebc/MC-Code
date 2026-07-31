package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.event.stock.StockMarketTicker;
import com.daqem.jobsplus.stock.StockMarketService;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockQuote;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.StockScreenSync;
import com.daqem.jobsplus.networking.s2c.ClientboundStockAlertPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundStockSnapshotPacket;
import com.daqem.jobsplus.stock.SnapshotStatus;
import com.daqem.jobsplus.stock.StockCatalog;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.PlayerItemDelivery;
import com.daqem.jobsplus.player.stock.StockAccount;
import com.daqem.jobsplus.player.stock.StockPosition;
import com.daqem.jobsplus.player.stock.StockPositionSide;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ServerboundStockActionPacket implements CustomPacketPayload
{
    public static final int MAX_TRANSFER_AMOUNT = 1_000;
    public static final int MAX_TRADE_AMOUNT = 1_000;

    private static final Identifier BITCOIN_ID = Identifier.parse("advancednetherite:bitcoin");
    private static final double SELL_FEE_RATE = 0.00015D;
    private static final double WITHDRAW_TAX_RATE = 0.002D;

    /** 종목 ID는 카탈로그에 정의된 짧은 문자열뿐이다. 조작된 패킷이 긴 문자열을 보내지 못하게 막는다. */
    private static final int MAX_STOCK_ID_LENGTH = 16;

    private final Action action;
    private final String stockId;
    private final int amount;
    private final StockPositionSide positionSide;
    private final int leverage;

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
                    buffer.writeEnum(packet.positionSide);
                    buffer.writeVarInt(packet.leverage);
                }
            };

    public ServerboundStockActionPacket(Action action, String stockId, int amount, long snapshotVersion)
    {
        this(action, stockId, amount, snapshotVersion, StockPositionSide.LONG, StockPosition.DEFAULT_LEVERAGE);
    }

    public ServerboundStockActionPacket(Action action, String stockId, int amount, long snapshotVersion,
                                        StockPositionSide positionSide, int leverage)
    {
        this.action = action;
        this.stockId = stockId;
        this.amount = amount;
        this.snapshotVersion = snapshotVersion;
        this.positionSide = positionSide;
        this.leverage = leverage;
    }

    public ServerboundStockActionPacket(RegistryFriendlyByteBuf buffer)
    {
        this.action = buffer.readEnum(Action.class);
        this.stockId = buffer.readUtf(MAX_STOCK_ID_LENGTH);
        this.amount = buffer.readInt();
        this.snapshotVersion = buffer.readLong();
        this.positionSide = buffer.readEnum(StockPositionSide.class);
        this.leverage = buffer.readVarInt();
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
        ServerPlayer player = jobsServerPlayer.jobsplus$getServerPlayer();
        if (packet.amount <= 0)
        {
            return;
        }
        if (!StockTransactionRateLimiter.tryAcquire(player))
        {
            return;
        }
        if (packet.amount > getMaximumAmount(packet.action))
        {
            String amountType = "매수·매도";
            if (isTransferAction(packet.action))
            {
                amountType = "입출금";
            }
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    "단일 " + amountType + " 수량은 최대 1,000개입니다."));
            return;
        }

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
                if (!StockPosition.isAllowedLeverage(packet.leverage))
                {
                    NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                            "주식 배율은 기본, X2, X3, X5, X10, X15, X20만 선택할 수 있습니다."));
                    return;
                }
                StockQuote quote = resolveTradableQuote(player, packet);
                if (quote == null)
                {
                    return;
                }
                if (StockMarketTicker.liquidatePositionIfNeeded(jobsServerPlayer, quote))
                {
                    return;
                }
                account = jobsServerPlayer.jobsplus$getStockAccount();
                if (account.balance() + 0.00000001 < packet.amount)
                {
                    NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                            "주식 계좌의 비트코인이 부족합니다.\n입출금 메뉴에서 먼저 입금해 주세요."));
                    return;
                }
                StockPosition existingPosition = account.getPosition(packet.stockId);
                if (existingPosition != null
                        && (existingPosition.side() != packet.positionSide
                        || existingPosition.leverage() != packet.leverage))
                {
                    NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                            "같은 종목에는 하나의 포지션만 보유할 수 있습니다.\n"
                                    + "기존 포지션을 모두 판매한 후 변경해 주세요."));
                    return;
                }
                boolean queued = StockMarketTicker.queueBuyOrder(
                        player,
                        account,
                        packet.stockId,
                        packet.amount,
                        quote.priceKrw(),
                        packet.positionSide,
                        packet.leverage
                );
                if (!queued)
                {
                    NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                            "이 종목은 이미 구매 예약을 확인하고 있습니다.\n"
                                    + "예약 결과가 나온 뒤 다시 거래해 주세요."));
                    return;
                }
                account = account.reserveBuy(packet.amount);
                completedMessage = quote.name() + " " + packet.positionSide.getDisplayName()
                        + " " + StockPosition.getLeverageDisplayName(packet.leverage)
                        + " 구매가 예약되었습니다.\n"
                        + "투자금 " + packet.amount + "개가 계좌에서 차감되었습니다.\n"
                        + "다음 분의 진입 가격과 가격 변동을 확인한 뒤 결과를 알려드립니다.\n"
                        + "구매 예약은 취소할 수 없으니 신중하게 결정해 주세요.";
            }
            case SELL -> {
                StockQuote quote = resolveTradableQuote(player, packet);
                if (quote == null)
                {
                    return;
                }
                if (StockMarketTicker.liquidatePositionIfNeeded(jobsServerPlayer, quote))
                {
                    return;
                }
                account = jobsServerPlayer.jobsplus$getStockAccount();
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

        if (packet.action == Action.SELL)
        {
            StockMarketTicker.onStockAccountChanged(player, account);
        }
        jobsServerPlayer.jobsplus$setStockAccount(account);
        if (isTransferAction(packet.action))
        {
            // 인벤토리는 로그아웃 즉시 저장되지만 계좌 원장은 오토세이브 때 저장된다.
            // 이 시점 차이를 노려 출금 후 강제 종료로 비트코인을 복사하지 못하도록
            // 입출금 직후 원장을 바로 디스크에 기록한다.
            MinecraftServer server = player.level().getServer();
            if (server != null)
            {
                server.overworld().getDataStorage().scheduleSave();
            }
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        StockScreenSync.send(jobsServerPlayer);
        if (completedMessage != null)
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(completedMessage, "확인"));
        }
    }

    /**
     * 거래 요청을 검증할 현재 시세를 서버 스냅샷에서 가져온다.
     * <p>
     * 판매는 이 가격으로 체결하고, 구매는 이 가격을 확인한 뒤 다음 분 시작가 예약을 생성한다.
     * 시세 API를 다시 조회하지 않는다. 조회는 비동기라서 즉시 반영되지 않으며, 클라이언트가 본 상태와
     * 다른 시세를 기준으로 요청을 승인할 수 있기 때문이다. 대신 아래를 모두 확인한다.
     * <ul>
     *   <li>클라이언트가 보고 있던 스냅샷 번호가 서버 스냅샷과 같은지</li>
     *   <li>그 스냅샷이 현재 서버 분의 가격인지 (이전 분 가격으로 체결 금지)</li>
     *   <li>스냅샷 상태가 {@link SnapshotStatus#READY}인지 (조회 중이거나 실패면 거래 중지)</li>
     *   <li>해당 종목의 가격이 유한한 양수인지</li>
     * </ul>
     *
     * @return 거래 요청에 사용할 수 있는 시세. 불가능하면 {@code null}이며 사유는 플레이어에게 전달된다.
     */
    private static StockQuote resolveTradableQuote(ServerPlayer player, ServerboundStockActionPacket packet)
    {
        // 시청 상태와 무관하게 조작된 패킷으로 거래하지 못하도록 실제 주식 탭 진입 여부를 확인한다.
        if (!StockMarketTicker.isViewing(player))
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    "주식 탭을 연 상태에서만 거래할 수 있습니다."));
            return null;
        }

        StockMarketSnapshot snapshot = StockMarketService.getInstance().getSnapshot();

        if (StockMarketTicker.hasPendingBuyOrder(player, packet.stockId))
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    "이 종목의 구매 예약을 안전하게 확인하고 있습니다.\n"
                            + "확인이 끝날 때까지 추가 구매와 판매는 잠시 기다려 주세요."));
            return null;
        }

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
            String reason = "시세를 불러오지 못했습니다.\n다음 시세 갱신 후 다시 시도해 주세요.";
            if (snapshot.status() == SnapshotStatus.REFRESHING)
            {
                reason = "최신 시세를 불러오고 있습니다.\n잠시 후 다시 시도해 주세요.";
            }
            rejectAndResync(player, snapshot, reason);
            return null;
        }

        // 스냅샷에 없는 종목은 물론, 카탈로그에 없는 ID도 거절한다.
        StockQuote quote = null;
        if (StockCatalog.getStock(packet.stockId) != null)
        {
            quote = snapshot.getQuote(packet.stockId);
        }
        if (quote == null)
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket("거래할 수 없는 종목입니다."));
            return null;
        }

        if (!quote.hasValidPrice())
        {
            rejectAndResync(player, snapshot,
                    quote.name() + " 시세를 불러오지 못했습니다.\n다음 시세 갱신 후 다시 시도해 주세요.");
            return null;
        }
        if (!StockMarketTicker.isPositionCaughtUp(player, packet.stockId, snapshot.marketMinute()))
        {
            NetworkManager.sendToPlayer(player, new ClientboundStockAlertPacket(
                    "안전한 정산을 위해 아직 확인하지 못한 가격 변동을 점검하고 있습니다.\n"
                            + "점검이 끝나면 다시 거래할 수 있습니다."));
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

    private static int getMaximumAmount(Action action)
    {
        if (isTransferAction(action))
        {
            return MAX_TRANSFER_AMOUNT;
        }
        return MAX_TRADE_AMOUNT;
    }

    private static boolean isTransferAction(Action action)
    {
        return action == Action.DEPOSIT || action == Action.WITHDRAW;
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

    public enum Action
    {
        DEPOSIT,
        WITHDRAW,
        BUY,
        SELL
    }
}

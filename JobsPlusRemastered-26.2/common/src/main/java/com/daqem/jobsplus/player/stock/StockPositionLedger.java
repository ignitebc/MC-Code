package com.daqem.jobsplus.player.stock;

import com.daqem.jobsplus.JobsPlus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 접속 여부와 관계없이 청산 대상 포지션을 추적하는 월드 저장 데이터.
 * <p>
 * 플레이어 NBT의 {@link StockAccount}가 실제 계좌 원장이고, 이 데이터는 시세 감시를 계속하기 위한
 * 포지션 인덱스와 오프라인 상태에서 확정된 청산 대기를 보관한다.
 */
public final class StockPositionLedger extends SavedData
{
    private static final Identifier FILE_ID = JobsPlus.getId("stock_positions");
    private static final Codec<StockPositionSide> POSITION_SIDE_CODEC = Codec.STRING.xmap(
            StockPositionSide::fromSerializedName,
            StockPositionSide::getSerializedName
    );

    public static final Codec<TrackedPosition> TRACKED_POSITION_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.STRING_CODEC.fieldOf("player_id").forGetter(TrackedPosition::playerId),
                    Codec.STRING.fieldOf("player_name").forGetter(TrackedPosition::playerName),
                    StockPosition.CODEC.fieldOf("position").forGetter(TrackedPosition::position),
                    Codec.LONG.fieldOf("last_checked_minute").forGetter(TrackedPosition::lastCheckedMinute),
                    Codec.LONG.optionalFieldOf("trade_blocked_through_minute", Long.MIN_VALUE)
                            .forGetter(TrackedPosition::tradeBlockedThroughMinute)
            ).apply(instance, TrackedPosition::new));

    public static final Codec<PendingLiquidation> PENDING_LIQUIDATION_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.STRING_CODEC.fieldOf("player_id").forGetter(PendingLiquidation::playerId),
                    StockPosition.CODEC.fieldOf("position").forGetter(PendingLiquidation::position),
                    Codec.LONG.fieldOf("liquidated_at").forGetter(PendingLiquidation::liquidatedAt)
            ).apply(instance, PendingLiquidation::new));

    public static final Codec<PendingBuyOrder> PENDING_BUY_ORDER_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.STRING_CODEC.fieldOf("player_id").forGetter(PendingBuyOrder::playerId),
                    Codec.STRING.fieldOf("player_name").forGetter(PendingBuyOrder::playerName),
                    Codec.STRING.fieldOf("stock_id").forGetter(PendingBuyOrder::stockId),
                    Codec.DOUBLE.fieldOf("amount").forGetter(PendingBuyOrder::amount),
                    Codec.DOUBLE.fieldOf("requested_price").forGetter(PendingBuyOrder::requestedPrice),
                    POSITION_SIDE_CODEC.fieldOf("side").forGetter(PendingBuyOrder::side),
                    Codec.INT.fieldOf("leverage").forGetter(PendingBuyOrder::leverage),
                    Codec.LONG.fieldOf("activation_minute").forGetter(PendingBuyOrder::activationMinute),
                    StockPosition.CODEC.optionalFieldOf("base_position").forGetter(PendingBuyOrder::basePosition)
            ).apply(instance, PendingBuyOrder::new));

    public static final Codec<PendingBuyResult> PENDING_BUY_RESULT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.STRING_CODEC.fieldOf("player_id").forGetter(PendingBuyResult::playerId),
                    Codec.STRING.fieldOf("stock_id").forGetter(PendingBuyResult::stockId),
                    Codec.DOUBLE.fieldOf("amount").forGetter(PendingBuyResult::amount),
                    Codec.DOUBLE.optionalFieldOf("fill_price", 0D).forGetter(PendingBuyResult::fillPrice),
                    POSITION_SIDE_CODEC.fieldOf("side").forGetter(PendingBuyResult::side),
                    Codec.INT.fieldOf("leverage").forGetter(PendingBuyResult::leverage),
                    Codec.BOOL.fieldOf("filled").forGetter(PendingBuyResult::filled),
                    Codec.LONG.fieldOf("resolved_at").forGetter(PendingBuyResult::resolvedAt)
            ).apply(instance, PendingBuyResult::new));

    public static final Codec<StockPositionLedger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TRACKED_POSITION_CODEC.listOf()
                    .optionalFieldOf("positions", List.of())
                    .forGetter(StockPositionLedger::getTrackedPositions),
            PENDING_LIQUIDATION_CODEC.listOf()
                    .optionalFieldOf("pending_liquidations", List.of())
                    .forGetter(StockPositionLedger::getPendingLiquidations),
            PENDING_BUY_ORDER_CODEC.listOf()
                    .optionalFieldOf("pending_buy_orders", List.of())
                    .forGetter(StockPositionLedger::getPendingBuyOrders),
            PENDING_BUY_RESULT_CODEC.listOf()
                    .optionalFieldOf("pending_buy_results", List.of())
                    .forGetter(StockPositionLedger::getPendingBuyResults)
    ).apply(instance, StockPositionLedger::new));

    public static final SavedDataType<StockPositionLedger> TYPE = new SavedDataType<>(
            FILE_ID,
            StockPositionLedger::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<PositionKey, TrackedPosition> positions = new LinkedHashMap<>();
    private final Map<PositionKey, PendingLiquidation> pendingLiquidations = new LinkedHashMap<>();
    private final Map<PositionKey, PendingBuyOrder> pendingBuyOrders = new LinkedHashMap<>();
    private final Map<PositionKey, PendingBuyResult> pendingBuyResults = new LinkedHashMap<>();

    public StockPositionLedger()
    {
    }

    private StockPositionLedger(List<TrackedPosition> positions,
                                List<PendingLiquidation> pendingLiquidations,
                                List<PendingBuyOrder> pendingBuyOrders,
                                List<PendingBuyResult> pendingBuyResults)
    {
        for (TrackedPosition position : positions)
        {
            this.positions.put(PositionKey.of(position.playerId(), position.position().stockId()), position);
        }
        for (PendingLiquidation liquidation : pendingLiquidations)
        {
            this.pendingLiquidations.put(
                    PositionKey.of(liquidation.playerId(), liquidation.position().stockId()),
                    liquidation
            );
        }
        for (PendingBuyOrder order : pendingBuyOrders)
        {
            this.pendingBuyOrders.put(PositionKey.of(order.playerId(), order.stockId()), order);
        }
        for (PendingBuyResult result : pendingBuyResults)
        {
            this.pendingBuyResults.put(PositionKey.of(result.playerId(), result.stockId()), result);
        }
    }

    public static StockPositionLedger get(MinecraftServer server)
    {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean hasMarketWork()
    {
        if (!this.positions.isEmpty())
        {
            return true;
        }
        if (!this.pendingBuyOrders.isEmpty())
        {
            return true;
        }
        return false;
    }

    public List<TrackedPosition> getTrackedPositions()
    {
        return List.copyOf(this.positions.values());
    }

    public TrackedPosition getTrackedPosition(UUID playerId, String stockId)
    {
        return this.positions.get(PositionKey.of(playerId, stockId));
    }

    private List<PendingLiquidation> getPendingLiquidations()
    {
        return List.copyOf(this.pendingLiquidations.values());
    }

    public List<PendingBuyOrder> getPendingBuyOrders()
    {
        return List.copyOf(this.pendingBuyOrders.values());
    }

    private List<PendingBuyResult> getPendingBuyResults()
    {
        return List.copyOf(this.pendingBuyResults.values());
    }

    /**
     * 플레이어 계좌의 현재 포지션을 중앙 인덱스와 맞춘다.
     * 기존 포지션의 검사 시각과 거래 잠금은 유지한다. 중앙 원장에 없던 포지션은 현재 분을
     * 확인하기 전까지 거래를 잠가, 검사되지 않은 분을 완료 처리하는 일이 없게 한다.
     */
    public void syncPlayerPositions(ServerPlayer player, StockAccount account, long currentMinute)
    {
        UUID playerId = player.getUUID();
        Map<PositionKey, TrackedPosition> synchronizedPositions = new LinkedHashMap<>();
        for (StockPosition position : account.positions())
        {
            PositionKey key = PositionKey.of(playerId, position.stockId());
            TrackedPosition oldPosition = this.positions.get(key);
            long lastCheckedMinute = currentMinute - 1;
            long tradeBlockedThroughMinute = currentMinute;
            if (oldPosition != null)
            {
                lastCheckedMinute = oldPosition.lastCheckedMinute();
                tradeBlockedThroughMinute = oldPosition.tradeBlockedThroughMinute();
            }

            synchronizedPositions.put(
                    key,
                    new TrackedPosition(
                            playerId,
                            player.getName().getString(),
                            position,
                            lastCheckedMinute,
                            tradeBlockedThroughMinute
                    )
            );
        }

        boolean changed = this.positions.keySet().removeIf(key -> key.playerId().equals(playerId));
        if (!synchronizedPositions.isEmpty())
        {
            this.positions.putAll(synchronizedPositions);
            changed = true;
        }
        if (changed)
        {
            this.setDirty();
        }
    }

    public Map<String, Long> getEarliestCheckedMinutes()
    {
        Map<String, Long> earliestMinutes = new LinkedHashMap<>();
        for (TrackedPosition trackedPosition : this.positions.values())
        {
            String stockId = trackedPosition.position().stockId();
            Long oldMinute = earliestMinutes.get(stockId);
            if (oldMinute == null || trackedPosition.lastCheckedMinute() < oldMinute)
            {
                earliestMinutes.put(stockId, trackedPosition.lastCheckedMinute());
            }
        }
        for (PendingBuyOrder order : this.pendingBuyOrders.values())
        {
            long orderCheckedMinute = order.activationMinute() - 1;
            Long oldMinute = earliestMinutes.get(order.stockId());
            if (oldMinute == null || orderCheckedMinute < oldMinute)
            {
                earliestMinutes.put(order.stockId(), orderCheckedMinute);
            }
        }
        return Map.copyOf(earliestMinutes);
    }

    public boolean isCaughtUp(UUID playerId, String stockId, long currentMinute)
    {
        TrackedPosition position = this.positions.get(PositionKey.of(playerId, stockId));
        if (position == null)
        {
            return true;
        }

        long lastCompletedMinute = currentMinute - 1;
        if (position.lastCheckedMinute() < lastCompletedMinute)
        {
            return false;
        }
        if (position.lastCheckedMinute() < position.tradeBlockedThroughMinute())
        {
            return false;
        }
        return true;
    }

    public void markCheckedThrough(String stockId, long checkedThroughMinute)
    {
        boolean changed = false;
        List<Map.Entry<PositionKey, TrackedPosition>> entries = new ArrayList<>(this.positions.entrySet());
        for (Map.Entry<PositionKey, TrackedPosition> entry : entries)
        {
            TrackedPosition trackedPosition = entry.getValue();
            if (!trackedPosition.position().stockId().equals(stockId)
                    || checkedThroughMinute <= trackedPosition.lastCheckedMinute())
            {
                continue;
            }

            this.positions.put(
                    entry.getKey(),
                    new TrackedPosition(
                            trackedPosition.playerId(),
                            trackedPosition.playerName(),
                            trackedPosition.position(),
                            checkedThroughMinute,
                            trackedPosition.tradeBlockedThroughMinute()
                    )
            );
            changed = true;
        }
        if (changed)
        {
            this.setDirty();
        }
    }

    public boolean hasPendingBuyOrder(UUID playerId, String stockId)
    {
        PositionKey key = PositionKey.of(playerId, stockId);
        if (this.pendingBuyOrders.containsKey(key))
        {
            return true;
        }
        if (this.pendingBuyResults.containsKey(key))
        {
            return true;
        }
        return false;
    }

    public boolean queueBuyOrder(ServerPlayer player, StockAccount account, String stockId, double amount,
                                 double requestedPrice, StockPositionSide side, int leverage,
                                 long activationMinute)
    {
        if (!Double.isFinite(amount) || amount <= 0)
        {
            return false;
        }
        if (!Double.isFinite(requestedPrice) || requestedPrice <= 0)
        {
            return false;
        }
        if (!StockPosition.isAllowedLeverage(leverage))
        {
            return false;
        }

        PositionKey key = PositionKey.of(player.getUUID(), stockId);
        if (this.pendingBuyOrders.containsKey(key) || this.pendingBuyResults.containsKey(key))
        {
            return false;
        }

        StockPosition basePosition = account.getPosition(stockId);
        this.pendingBuyOrders.put(
                key,
                new PendingBuyOrder(
                        player.getUUID(),
                        player.getName().getString(),
                        stockId,
                        amount,
                        requestedPrice,
                        side,
                        leverage,
                        activationMinute,
                        Optional.ofNullable(basePosition)
                )
        );
        this.setDirty();
        return true;
    }

    public PendingBuyResult fillPendingBuyOrder(PendingBuyOrder order, double fillPrice,
                                                long entryMinute, long filledAt)
    {
        PositionKey key = PositionKey.of(order.playerId(), order.stockId());
        PendingBuyOrder storedOrder = this.pendingBuyOrders.get(key);
        if (!order.equals(storedOrder))
        {
            return null;
        }

        TrackedPosition currentTrackedPosition = this.positions.get(key);
        if (!doesBasePositionMatch(order, currentTrackedPosition))
        {
            return this.cancelPendingBuyOrder(order, filledAt);
        }

        StockPosition filledPosition;
        long lastCheckedMinute = entryMinute - 1;
        if (currentTrackedPosition == null)
        {
            filledPosition = new StockPosition(
                    order.stockId(),
                    0,
                    order.amount(),
                    order.amount(),
                    StockDecimal.truncate(fillPrice),
                    order.side(),
                    order.leverage()
            );
        }
        else
        {
            filledPosition = currentTrackedPosition.position().addInvestment(order.amount(), fillPrice);
            lastCheckedMinute = Math.min(
                    currentTrackedPosition.lastCheckedMinute(),
                    lastCheckedMinute
            );
        }

        this.positions.put(
                key,
                new TrackedPosition(
                        order.playerId(),
                        order.playerName(),
                        filledPosition,
                        lastCheckedMinute,
                        entryMinute
                )
        );
        PendingBuyResult result = new PendingBuyResult(
                order.playerId(),
                order.stockId(),
                order.amount(),
                fillPrice,
                order.side(),
                order.leverage(),
                true,
                filledAt
        );
        this.pendingBuyOrders.remove(key);
        this.pendingBuyResults.put(key, result);
        this.setDirty();
        return result;
    }

    public PendingBuyResult cancelPendingBuyOrder(PendingBuyOrder order, long cancelledAt)
    {
        PositionKey key = PositionKey.of(order.playerId(), order.stockId());
        PendingBuyOrder storedOrder = this.pendingBuyOrders.get(key);
        if (!order.equals(storedOrder))
        {
            return null;
        }

        PendingBuyResult result = new PendingBuyResult(
                order.playerId(),
                order.stockId(),
                order.amount(),
                0,
                order.side(),
                order.leverage(),
                false,
                cancelledAt
        );
        this.pendingBuyOrders.remove(key);
        this.pendingBuyResults.put(key, result);
        this.setDirty();
        return result;
    }

    public StockAccount applyPendingBuyResults(UUID playerId, StockAccount account)
    {
        List<Map.Entry<PositionKey, PendingBuyResult>> entries =
                new ArrayList<>(this.pendingBuyResults.entrySet());
        boolean changed = false;
        for (Map.Entry<PositionKey, PendingBuyResult> entry : entries)
        {
            PendingBuyResult result = entry.getValue();
            if (!result.playerId().equals(playerId))
            {
                continue;
            }

            if (result.filled())
            {
                account = account.fillReservedBuy(
                        result.stockId(),
                        result.amount(),
                        result.fillPrice(),
                        result.side(),
                        result.leverage(),
                        result.resolvedAt()
                );
            }
            else
            {
                account = account.cancelReservedBuy(result.amount());
            }
            this.pendingBuyResults.remove(entry.getKey());
            changed = true;
        }
        if (changed)
        {
            this.setDirty();
        }
        return account;
    }

    private static boolean doesBasePositionMatch(PendingBuyOrder order, TrackedPosition currentTrackedPosition)
    {
        if (order.basePosition().isEmpty())
        {
            if (currentTrackedPosition == null)
            {
                return true;
            }
            return false;
        }
        if (currentTrackedPosition == null)
        {
            return false;
        }
        if (!order.basePosition().get().equals(currentTrackedPosition.position()))
        {
            return false;
        }
        return true;
    }

    /**
     * 청산 확정을 중앙 저장 데이터에 먼저 기록한다.
     */
    public void markLiquidated(TrackedPosition trackedPosition, long liquidatedAt)
    {
        PositionKey key = PositionKey.of(trackedPosition.playerId(), trackedPosition.position().stockId());
        this.positions.remove(key);
        this.pendingLiquidations.put(
                key,
                new PendingLiquidation(trackedPosition.playerId(), trackedPosition.position(), liquidatedAt)
        );
        this.setDirty();
    }

    /**
     * 오프라인 중 확정된 청산을 플레이어 NBT 계좌에 반영한다.
     */
    public StockAccount applyPendingLiquidations(UUID playerId, StockAccount account)
    {
        List<Map.Entry<PositionKey, PendingLiquidation>> entries =
                new ArrayList<>(this.pendingLiquidations.entrySet());
        boolean changed = false;
        for (Map.Entry<PositionKey, PendingLiquidation> entry : entries)
        {
            PendingLiquidation liquidation = entry.getValue();
            if (!liquidation.playerId().equals(playerId))
            {
                continue;
            }

            account = account.liquidate(
                    liquidation.position().stockId(),
                    liquidation.liquidatedAt()
            );
            this.pendingLiquidations.remove(entry.getKey());
            changed = true;
        }
        if (changed)
        {
            this.setDirty();
        }
        return account;
    }

    public record TrackedPosition(UUID playerId, String playerName, StockPosition position,
                                  long lastCheckedMinute, long tradeBlockedThroughMinute)
    {
    }

    public record PendingBuyOrder(UUID playerId, String playerName, String stockId, double amount,
                                  double requestedPrice, StockPositionSide side, int leverage, long activationMinute,
                                  Optional<StockPosition> basePosition)
    {
    }

    public record PendingBuyResult(UUID playerId, String stockId, double amount, double fillPrice,
                                   StockPositionSide side, int leverage, boolean filled, long resolvedAt)
    {
    }

    private record PendingLiquidation(UUID playerId, StockPosition position, long liquidatedAt)
    {
    }

    private record PositionKey(UUID playerId, String stockId)
    {
        private static PositionKey of(UUID playerId, String stockId)
        {
            return new PositionKey(playerId, stockId);
        }
    }
}

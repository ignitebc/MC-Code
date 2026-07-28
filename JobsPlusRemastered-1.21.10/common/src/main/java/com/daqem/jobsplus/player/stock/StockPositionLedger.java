package com.daqem.jobsplus.player.stock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 접속 여부와 관계없이 청산 대상 포지션을 추적하는 월드 저장 데이터.
 * <p>
 * 플레이어 NBT의 {@link StockAccount}가 실제 계좌 원장이고, 이 데이터는 시세 감시를 계속하기 위한
 * 포지션 인덱스와 오프라인 상태에서 확정된 청산 대기를 보관한다.
 */
public final class StockPositionLedger extends SavedData
{
    private static final String FILE_ID = "jobsplus_stock_positions";

    public static final Codec<TrackedPosition> TRACKED_POSITION_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.STRING_CODEC.fieldOf("player_id").forGetter(TrackedPosition::playerId),
                    Codec.STRING.fieldOf("player_name").forGetter(TrackedPosition::playerName),
                    StockPosition.CODEC.fieldOf("position").forGetter(TrackedPosition::position),
                    Codec.LONG.fieldOf("last_checked_minute").forGetter(TrackedPosition::lastCheckedMinute)
            ).apply(instance, TrackedPosition::new));

    public static final Codec<PendingLiquidation> PENDING_LIQUIDATION_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.STRING_CODEC.fieldOf("player_id").forGetter(PendingLiquidation::playerId),
                    StockPosition.CODEC.fieldOf("position").forGetter(PendingLiquidation::position),
                    Codec.LONG.fieldOf("liquidated_at").forGetter(PendingLiquidation::liquidatedAt)
            ).apply(instance, PendingLiquidation::new));

    public static final Codec<StockPositionLedger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TRACKED_POSITION_CODEC.listOf()
                    .optionalFieldOf("positions", List.of())
                    .forGetter(StockPositionLedger::getTrackedPositions),
            PENDING_LIQUIDATION_CODEC.listOf()
                    .optionalFieldOf("pending_liquidations", List.of())
                    .forGetter(StockPositionLedger::getPendingLiquidations)
    ).apply(instance, StockPositionLedger::new));

    public static final SavedDataType<StockPositionLedger> TYPE = new SavedDataType<>(
            FILE_ID,
            StockPositionLedger::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<PositionKey, TrackedPosition> positions = new LinkedHashMap<>();
    private final Map<PositionKey, PendingLiquidation> pendingLiquidations = new LinkedHashMap<>();

    public StockPositionLedger()
    {
    }

    private StockPositionLedger(List<TrackedPosition> positions, List<PendingLiquidation> pendingLiquidations)
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
    }

    public static StockPositionLedger get(MinecraftServer server)
    {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean hasOpenPositions()
    {
        return !this.positions.isEmpty();
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

    /**
     * 플레이어 계좌의 현재 포지션을 중앙 인덱스와 맞춘다.
     * 기존 포지션의 검사 시각은 유지한다. 새 포지션은 진입 전 가격으로 오청산하지 않도록
     * 진입한 현재 분을 검사 완료 시각으로 기록하고 다음 완료 분부터 분봉을 검사한다.
     */
    public void syncPlayerPositions(ServerPlayer player, StockAccount account, long currentMinute)
    {
        UUID playerId = player.getUUID();
        Map<PositionKey, TrackedPosition> synchronizedPositions = new LinkedHashMap<>();
        for (StockPosition position : account.positions())
        {
            PositionKey key = PositionKey.of(playerId, position.stockId());
            TrackedPosition oldPosition = this.positions.get(key);
            long lastCheckedMinute = currentMinute;
            if (oldPosition != null)
            {
                lastCheckedMinute = oldPosition.lastCheckedMinute();
            }

            synchronizedPositions.put(
                    key,
                    new TrackedPosition(playerId, player.getName().getString(), position, lastCheckedMinute)
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
                            checkedThroughMinute
                    )
            );
            changed = true;
        }
        if (changed)
        {
            this.setDirty();
        }
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

    public record TrackedPosition(UUID playerId, String playerName, StockPosition position, long lastCheckedMinute)
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

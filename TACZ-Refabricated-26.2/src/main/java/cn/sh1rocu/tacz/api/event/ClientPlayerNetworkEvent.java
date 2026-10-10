package cn.sh1rocu.tacz.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;

public abstract class ClientPlayerNetworkEvent extends BaseEvent {
    private final MultiPlayerGameMode multiPlayerGameMode;
    private final LocalPlayer player;
    private final Connection connection;

    public static final Event<LoggingOutCallback> LOGGING_OUT = EventFactory.createArrayBacked(LoggingOutCallback.class, callbacks -> event -> {
        for (LoggingOutCallback callback : callbacks) {
            callback.post(event);
        }
    });

    /**
     * @deprecated [26.2 / r42] 이 이벤트는 <b>더 이상 발생하지 않는다</b>.
     * <p>원래는 {@code ClientPacketListenerMixin}이 {@code handleRespawn} 안의
     * {@code ClientLevel#addPlayer} 호출 지점에 주입해 발생시켰지만, 26.2에는 그 메서드가 없어
     * ({@code ClientLevel}을 메서드별로 대조해도 없고, {@code handleRespawn} 역어셈블에도 그 호출이 없다)
     * 해당 mixin도 함께 삭제했다.</p>
     * <p>"플레이어 부활·차원 이동 뒤" 시점이 필요하면 클라이언트 틱에서
     * {@code Minecraft#player} 인스턴스가 바뀌었는지 비교한다. 본체의
     * {@code RefreshClonePlayerDataEvent#onClientTick}이 그렇게 하므로 참고한다.</p>
     * <p>이 필드는 기존 외부 코드의 컴파일을 깨지 않으려고 남겨 둔 것이며, 등록한 콜백은 한 번도 호출되지 않는다.</p>
     */
    @Deprecated
    public static final Event<CloneCallback> CLONE = EventFactory.createArrayBacked(CloneCallback.class, callbacks -> event -> {
        for (CloneCallback callback : callbacks) {
            callback.post(event);
        }
    });

    public interface LoggingOutCallback {
        void post(LoggingOut event);
    }

    public interface CloneCallback {
        void post(Clone event);
    }

    protected ClientPlayerNetworkEvent(final MultiPlayerGameMode multiPlayerGameMode, final LocalPlayer player, final Connection connection) {
        this.multiPlayerGameMode = multiPlayerGameMode;
        this.player = player;
        this.connection = connection;
    }

    public MultiPlayerGameMode getMultiPlayerGameMode() {
        return multiPlayerGameMode;
    }

    public LocalPlayer getPlayer() {
        return player;
    }

    public Connection getConnection() {
        return connection;
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent {
        public LoggingOut(@Nullable final MultiPlayerGameMode controller, @Nullable final LocalPlayer player, @Nullable final Connection networkManager) {
            super(controller, player, networkManager);
        }

        @Nullable
        @Override
        public MultiPlayerGameMode getMultiPlayerGameMode() {
            return super.getMultiPlayerGameMode();
        }

        @Nullable
        @Override
        public LocalPlayer getPlayer() {
            return super.getPlayer();
        }

        @Nullable
        @Override
        public Connection getConnection() {
            return super.getConnection();
        }
    }

    public static class Clone extends ClientPlayerNetworkEvent {
        private final LocalPlayer oldPlayer;

        public Clone(final MultiPlayerGameMode pc, final LocalPlayer oldPlayer, final LocalPlayer newPlayer, final Connection networkManager) {
            super(pc, newPlayer, networkManager);
            this.oldPlayer = oldPlayer;
        }

        public LocalPlayer getOldPlayer() {
            return oldPlayer;
        }

        public LocalPlayer getNewPlayer() {
            return super.getPlayer();
        }

        @Override
        public LocalPlayer getPlayer() {
            return super.getPlayer();
        }
    }
}

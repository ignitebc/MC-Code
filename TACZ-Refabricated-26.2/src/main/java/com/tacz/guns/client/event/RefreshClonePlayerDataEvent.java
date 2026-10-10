package com.tacz.guns.client.event;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.util.DelayedTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.lang.ref.WeakReference;
import java.util.function.BooleanSupplier;

/**
 * 플레이어가 차원을 넘으면 클라이언트가 플레이어의 부착물 속성 캐시를 한 번 새로 고쳐야 한다
 */
@Environment(EnvType.CLIENT)
public class RefreshClonePlayerDataEvent {
    /**
     * 마지막으로 본 로컬 플레이어 인스턴스. "플레이어 객체가 바뀌었는지" 감지하는 데 쓴다.
     *
     * <p>서버를 옮기거나 나간 뒤에도 예전 LocalPlayer를 붙잡지 않도록 약한 참조를 쓴다.</p>
     */
    private static WeakReference<LocalPlayer> lastPlayer = new WeakReference<>(null);

    /**
     * 지연 실행은 이 메서드로 처리한다.
     *
     * <p>[42차] 효력을 잃은 {@code ClientPacketListenerMixin}을 대신해
     * "부활·차원 이동 뒤 부착물 캐시 새로 고침"을 시작하는 역할도 함께 맡는다.</p>
     *
     * <h2>더 이상 mixin을 쓰지 않는 이유</h2>
     * 예전 {@code ClientPacketListenerMixin}은
     * {@code handleRespawn} 안의 {@code ClientLevel#addPlayer} 호출 지점에 주입했지만,
     * 26.2에는 <b>{@code ClientLevel#addPlayer} 메서드가 없다</b>
     * ({@code ClientLevel}을 메서드별로 대조해도 없고,
     * {@code handleRespawn} 전체 역어셈블에도 그 호출이 없다).
     * 즉 {@code ClientPlayerNetworkEvent.CLONE} 이벤트가 <b>절대 발생하지 않아</b>
     * 부활 뒤 부착물 속성 캐시 새로 고침이 계속 동작하지 않았다.
     *
     * <h2>폴링으로 바꾼 것이 적절한 이유</h2>
     * 이 기능의 본질은 "플레이어 인스턴스가 바뀐 뒤 10틱 늦게
     * {@code initialData()}를 한 번 호출"하는 것뿐이다 — <b>정확한 시점이 필요 없다</b>.
     * 원래 구현도 이벤트 발생 시점에는 인벤토리가 아직 동기화되지 않아
     * {@link DelayedTask}로 10틱을 더 늦춰야 했다.
     *
     * <p>{@code Minecraft#player} 필드는 부활·차원 이동 때 새 인스턴스로 통째로 바뀌므로,
     * 여기서 참조를 비교하면 같은 시점을 잡을 수 있고 다음과 같은 장점이 있다:</p>
     * <ul>
     *   <li>이 메서드는 <b>원래부터</b> {@code START_CLIENT_TICK} 콜백으로 등록되어 있어(DelayedTask 구동)
     *       틱 비용이 늘지 않는다.</li>
     *   <li>틱마다 참조 비교({@code !=}) 한 번만 하므로 비용이 거의 없다.</li>
     *   <li>mixin이 없다 — 버전마다 이름이 바뀌는 내부 메서드에 기대지 않는다.</li>
     * </ul>
     */
    public static void onClientTick(Minecraft client) {
        try {
            detectPlayerSwap(client);
        } catch (Exception e) {
            GunMod.LOGGER.error("Failed to detect local player swap", e);
        }
        try {
            DelayedTask.SUPPLIERS.removeIf(BooleanSupplier::getAsBoolean);
        } catch (Exception e) {
            DelayedTask.SUPPLIERS.clear();
            GunMod.LOGGER.error(e.getMessage(), e);
        }
    }

    private static void detectPlayerSwap(Minecraft client) {
        LocalPlayer current = client.player;
        LocalPlayer previous = lastPlayer.get();
        if (current == previous) {
            return;
        }
        lastPlayer = new WeakReference<>(current);
        if (current == null || previous == null) {
            // null -> 플레이어: 처음 월드에 들어옴. PlayerEnterWorld가 이미 초기화하므로 여기서는 반복하지 않는다.
            // 플레이어 -> null: 월드에서 나감. 처리할 필요 없다.
            return;
        }
        // 여기까지 왔다면 플레이어 인스턴스가 바뀐 것이다(부활 / 차원 이동). 예전 CLONE 이벤트와 의미가 같다.
        // 똑같이 10틱 늦춘다: 지금은 인벤토리 동기화가 끝나지 않아 바로 총기 데이터를 읽으면 부착물을 얻지 못한다.
        DelayedTask.add(() -> IGunOperator.fromLivingEntity(current).initialData(), 10);
    }
}

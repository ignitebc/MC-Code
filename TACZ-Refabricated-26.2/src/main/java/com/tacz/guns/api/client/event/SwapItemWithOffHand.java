package com.tacz.guns.api.client.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * 플레이어가 주 손과 보조 손 아이템을 바꿀 때 발생하는 이벤트
 */
public class SwapItemWithOffHand extends BaseEvent {
    public SwapItemWithOffHand() {
    }

    public static final Event<Callback> CALLBACK = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback callback : callbacks) {
            callback.post(event);
        }
    });

    public interface Callback {
        void post(SwapItemWithOffHand event);
    }
}

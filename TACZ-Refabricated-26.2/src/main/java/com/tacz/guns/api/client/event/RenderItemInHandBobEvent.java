package com.tacz.guns.api.client.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * 1인칭 시점이 흔들릴 때의 플레이어 손 흔들림
 */
public class RenderItemInHandBobEvent extends BaseEvent {
    public static final Event<HurtCallback> HURT = EventFactory.createArrayBacked(HurtCallback.class, callbacks -> event -> {
        for (HurtCallback callback : callbacks) {
            callback.post(event);
        }
    });

    public static final Event<ViewCallback> VIEW = EventFactory.createArrayBacked(ViewCallback.class, callbacks -> event -> {
        for (ViewCallback callback : callbacks) {
            callback.post(event);
        }
    });

    public interface HurtCallback {
        void post(BobHurt event);
    }

    public interface ViewCallback {
        void post(BobView event);
    }

    public static class BobHurt extends RenderItemInHandBobEvent implements ICancellableEvent {
        public BobHurt() {
        }
    }

    public static class BobView extends RenderItemInHandBobEvent implements ICancellableEvent {
        public BobView() {
        }
    }
}

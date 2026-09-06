package com.tacz.guns.api.client.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * 当第一人称视角触发摇晃时，世界背景的摇晃
 */
public class RenderLevelBobEvent extends BaseEvent {
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

    public static class BobHurt extends RenderLevelBobEvent implements ICancellableEvent {
        public BobHurt() {
        }
    }

    public static class BobView extends RenderLevelBobEvent implements ICancellableEvent {
        public BobView() {
        }
    }
}

package com.tacz.guns.api.client.event;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * ItemInHandRenderer#renderHandsWithItems를 호출할 때 발생하는 이벤트
 * 카메라 애니메이션 관련 처리에 쓴다
 */
public class BeforeRenderHandEvent extends BaseEvent {
    private final PoseStack poseStack;

    public static Event<Callback> CALLBACK = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback callback : callbacks) {
            callback.post(event);
        }
    });

    public interface Callback {
        void post(BeforeRenderHandEvent event);
    }

    public BeforeRenderHandEvent(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }
}

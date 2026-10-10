package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;

/**
 * 부착물 속성 보정값을 캐시할 때 발생하는 이벤트
 * <p>
 * 다른 모드가 사용자 정의 부착물 속성 보정값을 추가하려면 이 이벤트를 잡으면 된다
 */
public class AttachmentPropertyEvent extends BaseEvent {
    private final ItemStack gunItem;
    private final AttachmentCacheProperty cacheProperty;

    public static final Event<Callback> CALLBACK = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback callback : callbacks) {
            callback.post(event);
        }
    });

    public interface Callback {
        void post(AttachmentPropertyEvent event);
    }

    public AttachmentPropertyEvent(ItemStack gunItem, AttachmentCacheProperty attachmentProperty) {
        this.gunItem = gunItem;
        this.cacheProperty = attachmentProperty;
    }

    public ItemStack getGunItem() {
        return gunItem;
    }

    public AttachmentCacheProperty getCacheProperty() {
        return cacheProperty;
    }
}

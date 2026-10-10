package com.tacz.guns.init;

import cn.sh1rocu.tacz.api.event.EntityRemoveEvent;
import com.tacz.guns.entity.sync.core.DataHolderCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;

/**
 * 26.2: CCA가 제거되어 DataHolderCapabilityProvider에 내장된 WeakHashMap 저장소를 쓴다
 */
public class CapabilityRegistry {
    public static void init() {
        EntityRemoveEvent.EVENT.register(event -> {
            var entity = event.getEntity();
            if (!(entity instanceof ServerPlayer)) {
                DataHolderCapabilityProvider.maybeGet(entity).ifPresent(DataHolderCapabilityProvider::invalidate);
                DataHolderCapabilityProvider.remove(entity);
            }
        });
    }
}
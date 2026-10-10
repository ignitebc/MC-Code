package com.tacz.guns.resource.modifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

import static org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * 부착물 캐시 계산과 관련된 것은 모두 여기에 있다
 */
public class AttachmentCacheProperty {
    @SuppressWarnings("rawtypes")
    private final Map<String, CacheValue> cacheValues = Maps.newHashMap();
    private final Map<String, List<?>> cacheModifiers = Maps.newHashMap();

    @SuppressWarnings("all")
    public void eval(ItemStack gunItem, GunData gunData) {
        // 수치 초기화
        var modifiers = AttachmentPropertyManager.getModifiers();
        modifiers.forEach((id, value) -> {
            cacheValues.put(id, value.initCache(gunItem, gunData));
            cacheModifiers.put(id, Lists.newArrayList());
        });

        // 부착물 속성을 하나씩 읽어 modifier에 쓴다
        AttachmentDataUtils.getAllAttachmentData(gunItem, gunData, data -> {
            data.getModifier().forEach((id, value) -> {
                List objects = cacheModifiers.get(id);
                objects.add(value.getValue());
            });
        });

        // 마지막에 한 번에 계산을 끝내고 캐시에 넣는다
        cacheValues.forEach((id, value) -> {
            List cacheModifier = cacheModifiers.get(id);
            // 이 총에 해당 modifier가 없거나 modifier가 비었을 수 있다
            if (cacheModifier == null || cacheModifier.isEmpty()) {
                return;
            }
            modifiers.get(id).eval(cacheModifier, value);
        });

        // 메모리를 차지하지 않도록 필요 없는 데이터를 지운다
        cacheModifiers.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getCache(String id) {
        return (T) cacheValues.get(id).getValue();
    }

    @Experimental
    public <T> T getCache(GunProperty<T> key) {
        return key.type().cast(cacheValues.get(key.name()).getValue());
    }

    @Experimental
    @SuppressWarnings("unchecked")
    public <T> void setCache(GunProperty<T> key, T value) {
        if (!key.type().isInstance(value)) {
            throw new IllegalArgumentException("Gun cache type mismatch, needs %s, found %s".formatted(key.type().getSimpleName(), value.getClass().getSimpleName()));
        }
        cacheValues.get(key.name()).setValue(value);
    }
}

package com.tacz.guns.api.modifier;

/**
 * 부착물 캐시 속성 값 하나
 */
public class CacheValue<T> {
    private T value;

    public CacheValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}

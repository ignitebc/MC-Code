package com.tacz.guns.api.modifier;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 부착물이 JSON에서 읽은 데이터
 *
 * @param <T> JSON을 읽어 바꾼 중간 데이터 타입
 */
public abstract class JsonProperty<T> {
    protected List<Component> components = Lists.newArrayList();
    private @Nullable T value;

    public JsonProperty(@Nullable T value) {
        this.value = value;
    }

    @Nullable
    public T getValue() {
        return value;
    }

    public void setValue(@Nullable T value) {
        this.value = value;
    }

    public List<Component> getComponents() {
        return components;
    }

    /**
     * 부착물 설명 글자에 쓸 안내 문구를 초기화한다
     */
    public abstract void initComponents();
}

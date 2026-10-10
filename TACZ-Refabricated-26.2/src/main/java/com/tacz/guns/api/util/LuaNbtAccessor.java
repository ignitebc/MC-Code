package com.tacz.guns.api.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.ApiStatus;

/**
 * Lua에서 NBT 데이터에 접근하기 위한 간단한 NBT 래퍼.<br/>
 * 지금은 기본 데이터 타입 읽기·쓰기만 지원하고, 배열 같은 복잡한 타입은 지원하지 않는다.
 */
@SuppressWarnings("unused")
public record LuaNbtAccessor(CompoundTag nbt) {

    public static LuaNbtAccessor from(ItemStack stack) {
        CompoundTag tag = java.util.Optional.ofNullable(stack.get(DataComponents.CUSTOM_DATA))
                .map(CustomData::copyTag).orElseGet(CompoundTag::new);
        return new LuaNbtAccessor(tag);
    }

    public static LuaNbtAccessor from(CompoundTag nbt) {
        return new LuaNbtAccessor(nbt);
    }

    public boolean contains(String key) {
        return nbt.contains(key);
    }

    public boolean contains(String key, int type) {
        return nbt.contains(key);
    }

    public LuaNbtAccessor newCompoundTag() {
        return new LuaNbtAccessor(new CompoundTag());
    }

    public int getInt(String key) {
        return nbt.getIntOr(key, 0);
    }

    public double getDouble(String key) {
        return nbt.getDoubleOr(key, 0);
    }

    public float getFloat(String key) {
        return nbt.getFloatOr(key, 0);
    }

    public long getLong(String key) {
        return nbt.getLongOr(key, 0);
    }

    public String getString(String key) {
        return nbt.getStringOr(key, "");
    }

    public boolean getBoolean(CompoundTag nbt, String key) {
        return nbt.getBooleanOr(key, false);
    }

    public LuaNbtAccessor getCompound(String key) {
        if (!nbt.contains(key)) {
            return null;
        }
        return from(nbt.getCompoundOrEmpty(key));
    }

    public void putInt(String key, int value) {
        nbt.putInt(key, value);
    }

    public void putDouble(String key, double value) {
        nbt.putDouble(key, value);
    }

    public void putFloat(String key, float value) {
        nbt.putFloat(key, value);
    }

    public void putLong(String key, long value) {
        nbt.putLong(key, value);
    }

    public void putString(String key, String value) {
        nbt.putString(key, value);
    }

    public void putBoolean(String key, boolean value) {
        nbt.putBoolean(key, value);
    }

    /**
     * 현재 NbtCompound에 새 Compound를 추가한다
     *
     * @param key   키
     * @param value 스크립트에서는 {@link LuaNbtAccessor#newCompoundTag()}로 새 LuaNbtAccessor 객체를 만들어 쓴다
     */
    public void putCompound(String key, LuaNbtAccessor value) {
        if (value != null) {
            nbt.put(key, value.nbt());
        }
    }

    @Override
    @ApiStatus.Internal
    public CompoundTag nbt() {
        return nbt;
    }
}

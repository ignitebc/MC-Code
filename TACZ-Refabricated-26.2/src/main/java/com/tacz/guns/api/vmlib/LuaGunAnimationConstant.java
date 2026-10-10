package com.tacz.guns.api.vmlib;

import com.google.common.collect.Maps;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * 기능은 {@link LuaAnimationConstant}와 비슷하다.
 */
public class LuaGunAnimationConstant implements LuaLibrary {
    private final Map<String, Object> constantMap = Maps.newHashMap();

    public LuaGunAnimationConstant() {
        // GunAnimationConstant의 public 필드를 모두 얻는다
        Field[] fields = GunAnimationConstant.class.getFields();
        // static final 상수 필드를 constantMap에 뽑아낸다
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                try {
                    // 변수 이름과 값을 얻는다
                    String name = field.getName();
                    Object value = field.get(null);
                    constantMap.put(name, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // ReloadState.StateType 열거형 대응
        for (ReloadState.StateType stateType : ReloadState.StateType.values()) {
            constantMap.put(stateType.name(), stateType.ordinal());
        }

        // FireMode 열거형 대응
        for (var fireMode : FireMode.values()) {
            constantMap.put(fireMode.name(), fireMode.ordinal());
        }
    }

    @Override
    public void install(LuaValue chunk) {
        for (Map.Entry<String, Object> entry : constantMap.entrySet()) {
            chunk.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
        }
    }
}

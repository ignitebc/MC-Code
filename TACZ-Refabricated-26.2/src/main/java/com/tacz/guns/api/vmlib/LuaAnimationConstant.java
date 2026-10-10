package com.tacz.guns.api.vmlib;

import com.google.common.collect.Maps;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.statemachine.AnimationConstant;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * ContextConstant가 정의한 재생 방식 등의 상수를 Lua 스크립트에 넣기 위한 클래스.
 * install 메서드를 호출하면 상수를 환경에 바로 주입한다
 *
 * @see AnimationConstant
 */
public class LuaAnimationConstant implements LuaLibrary {
    private final Map<String, Object> constantMap = Maps.newHashMap();

    public LuaAnimationConstant() {
        // AnimationConstant의 public 필드를 모두 얻는다
        Field[] fields = AnimationConstant.class.getFields();
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

        // PlayType 열거형 대응
        for (var playType : ObjectAnimation.PlayType.values()) {
            constantMap.put(playType.name(), playType.ordinal());
        }
    }

    @Override
    public void install(LuaValue chunk) {
        for (Map.Entry<String, Object> entry : constantMap.entrySet()) {
            chunk.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
        }
    }
}

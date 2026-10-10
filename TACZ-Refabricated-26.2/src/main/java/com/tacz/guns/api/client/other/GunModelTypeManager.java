package com.tacz.guns.api.client.other;

import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class GunModelTypeManager {
    public static final Map<String, BiFunction<BedrockModelPOJO, BedrockVersion, ? extends BedrockGunModel>> GUN_MODEL_TYPE_MAP = new HashMap<>();

    //문자열에 해당하는 모델 인스턴스 생성자를 Map에 등록한다
    //멀티스레드 안전에 주의
    public static synchronized void registerModelType(String typeName, BiFunction<BedrockModelPOJO, BedrockVersion, ? extends BedrockGunModel> constructor) {
        GUN_MODEL_TYPE_MAP.put(typeName, constructor);
    }

    //문자열에 해당하는 모델 인스턴스 생성자가 없으면 기본 생성자를 돌려준다
    public static synchronized BiFunction<BedrockModelPOJO, BedrockVersion, ? extends BedrockGunModel> getModelInstanceConstructor(String typeName) {
        return GUN_MODEL_TYPE_MAP.getOrDefault(typeName, BedrockGunModel::new);
    }
}
package com.tacz.guns.client.resource.pojo.display.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * 총기 팩 {@code display/blocks/*.json}의 {@code transforms} 부분을 26.2의
 * {@link ItemTransforms}로 해석한다.
 *
 * <p><b>이 클래스가 필요한 이유</b></p>
 *
 * <p>1.21.1에서는 {@code BlockDisplay#getTransforms()}가 {@code ItemTransforms}를 바로 돌려주고,
 * Minecraft 자체 Gson 어댑터({@code BlockModel.GSON}에 등록됨)가 역직렬화했다.
 * 26.2는 이 모델 JSON 해석을 다른 곳으로 옮겼고, {@code ItemTransform.Deserializer}는
 * {@code protected static} 내부 클래스가 되었으며 {@code ItemTransforms}도 공개 Codec을 더는 노출하지 않아
 * 바깥에서 그대로 재사용할 수 없다.</p>
 *
 * <p>이식할 때의 처리는 이랬다: {@code BlockDisplay.transforms}의 타입을 맨
 * {@code JsonObject}로 낮추고, {@code ClientBlockIndex}의 {@code checkTransforms(...)}와
 * {@code getTransforms()}를 <b>통째로 지웠다</b>. 그래서 {@code GunSmithTableItemRenderer}
 * 에 있던 "transforms 적용" 부분도 함께 사라졌다 — 결과적으로 세 종류 작업대/조립대의 손에 든 모델이
 * <b>전혀 축소되지 않고</b> 블록 원래 크기(1×1×1 m)로 그려져 거대하게 보였다.
 * 기본 팩의 {@code gun_smith_table.json}은 {@code scale: [0.25, 0.25, 0.25]}를 선언하므로
 * 실제 표시 크기가 원래 크기의 <b>4배</b>였다.</p>
 *
 * <p>이 클래스는 26.2 디컴파일 소스 {@code ItemTransform.Deserializer}의 <b>줄 단위 의미</b>대로 해석을 다시 구현한다:</p>
 * <ul>
 *   <li>{@code translation}에 {@code 0.0625}(1/16, 픽셀→미터)를 곱한 뒤 ±5로 clamp</li>
 *   <li>{@code scale}은 ±4로 clamp</li>
 *   <li>{@code rotation}은 그대로 유지(도 단위)</li>
 *   <li>기본값: rotation/translation은 0, scale은 1</li>
 * </ul>
 *
 * <p>{@link ItemTransform#apply(boolean, com.mojang.blaze3d.vertex.PoseStack.Pose)}는
 * 26.2에서 {@code PoseStack}이 아니라 {@code PoseStack.Pose}를 받고,
 * {@code translate(-0.5, -0.5, -0.5)} 가운데 맞춤을 스스로 한다는 점에 주의한다 — 호출하는 쪽에서 따로 더할 필요가 없다.</p>
 */
public final class BlockTransformParser {
    private static final Vector3f DEFAULT_ROTATION = new Vector3f();
    private static final Vector3f DEFAULT_TRANSLATION = new Vector3f();
    private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
    private static final float MAX_TRANSLATION = 5.0F;
    private static final float MAX_SCALE = 4.0F;

    private BlockTransformParser() {
    }

    /**
     * @param json 총기 팩의 {@code transforms} 객체. null이어도 된다
     * @return 해석 결과. json이 null이면 {@link ItemTransforms#NO_TRANSFORMS}를 돌려준다
     */
    public static ItemTransforms parse(@Nullable JsonObject json) {
        if (json == null) {
            return ItemTransforms.NO_TRANSFORMS;
        }
        return new ItemTransforms(
                get(json, "thirdperson_lefthand"),
                get(json, "thirdperson_righthand"),
                get(json, "firstperson_lefthand"),
                get(json, "firstperson_righthand"),
                get(json, "head"),
                get(json, "gui"),
                get(json, "ground"),
                get(json, "fixed"),
                // 26.2에서 새로 생긴 fixedFromBottom: 총기 팩 JSON에는 이 키가 없으므로 fixed 값을 그대로 쓴다.
                // 바닐라 모델 해석이 빠진 키를 NO_TRANSFORM으로 되돌리는 동작과 맞춘다.
                get(json, "fixed")
        );
    }

    private static ItemTransform get(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonObject()) {
            return ItemTransform.NO_TRANSFORM;
        }
        JsonObject object = root.getAsJsonObject(key);

        Vector3f rotation = getVector3f(object, "rotation", DEFAULT_ROTATION);

        Vector3f translation = getVector3f(object, "translation", DEFAULT_TRANSLATION);
        translation.mul(0.0625F);
        translation.set(
                Mth.clamp(translation.x, -MAX_TRANSLATION, MAX_TRANSLATION),
                Mth.clamp(translation.y, -MAX_TRANSLATION, MAX_TRANSLATION),
                Mth.clamp(translation.z, -MAX_TRANSLATION, MAX_TRANSLATION)
        );

        Vector3f scale = getVector3f(object, "scale", DEFAULT_SCALE);
        scale.set(
                Mth.clamp(scale.x, -MAX_SCALE, MAX_SCALE),
                Mth.clamp(scale.y, -MAX_SCALE, MAX_SCALE),
                Mth.clamp(scale.z, -MAX_SCALE, MAX_SCALE)
        );

        return new ItemTransform(rotation, translation, scale);
    }

    private static Vector3f getVector3f(JsonObject object, String key, Vector3f def) {
        if (!object.has(key)) {
            return new Vector3f(def);
        }
        JsonElement element = object.get(key);
        if (!element.isJsonArray()) {
            return new Vector3f(def);
        }
        JsonArray array = element.getAsJsonArray();
        if (array.size() != 3) {
            return new Vector3f(def);
        }
        return new Vector3f(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat()
        );
    }

    /** 호출하는 쪽이 문맥이 왼손인지 판단하기 쉽게 한다({@code ItemTransform#apply}의 첫 번째 인자). */
    public static boolean isLeftHand(ItemDisplayContext context) {
        return context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }
}

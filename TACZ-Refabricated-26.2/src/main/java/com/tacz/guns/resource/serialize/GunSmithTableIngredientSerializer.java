package com.tacz.guns.resource.serialize;

import com.google.gson.*;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

/**
 * 14차: 여기서 더는 {@code Ingredient.CODEC.parse}를 호출하지 않는다.
 *
 * <p>이 직렬화기는 {@code CommonDataManager}(server reload listener 하나)가 돌리는데,
 * 26.2의 item tag는 {@code MinecraftServer#reloadResources} 안의
 * {@code updateComponentsAndStaticRegistryTags()}에서야 묶이며, 이는 <b>모든 reload listener보다 늦다</b>.
 * 여기서 {@code "#c:ingots/copper"}를 해석하면 반드시 {@code Missing tag}를 얻고,
 * 그 예외를 {@code JsonDataManager#apply}가 삼켜 레시피 전체가 조용히 사라진다.
 * 자세한 내용은 {@link GunSmithTableIngredient}의 클래스 주석 참고.
 *
 * <p>그래서 여기서는 구조만 검사하고 JSON을 그대로 저장하며, 실제 해석은 처음 꺼내 쓸 때로 미룬다.
 */
public class GunSmithTableIngredientSerializer implements JsonDeserializer<GunSmithTableIngredient> {
    @Override
    public GunSmithTableIngredient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            if (!jsonObject.has("item")) {
                throw new JsonSyntaxException("Expected " + jsonObject + " must has a item member");
            }
            int count = 1;
            if (jsonObject.has("count")) {
                count = Math.max(GsonHelper.getAsInt(jsonObject, "count"), 1);
            }
            // 지연 해석: 지금은 tag가 아직 묶이지 않았으므로 원문만 저장한다.
            return new GunSmithTableIngredient(jsonObject.get("item"), count);
        } else {
            throw new JsonSyntaxException("Expected " + json + " to be a Pair because it's not an object");
        }
    }
}

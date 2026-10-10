package com.tacz.guns.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import com.tacz.guns.GunMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

/**
 * 작업대 레시피의 재료 하나.
 *
 * <h2>14차: 여기서 "지연 해석"을 하는 이유</h2>
 *
 * 원래 {@code GunSmithTableIngredientSerializer}는 Gson 역직렬화 그 자리에서
 * {@code Ingredient.CODEC.parse(...)}를 호출했다. 이는 26.2에서 <b>잘못된 시점</b>이며, 원인은 디컴파일로 단계별 확인했다:
 *
 * <ol>
 *   <li>{@code Ingredient.CODEC = ExtraCodecs.nonEmptyHolderSet(NON_AIR_HOLDER_SET_CODEC)}이고,
 *       {@code NON_AIR_HOLDER_SET_CODEC = HolderSetCodec.create(Registries.ITEM, ...)}이다;</li>
 *   <li>{@code HolderSetCodec#decode}는 {@code "#c:ingots/copper"} 같은 tag 표기를 만나면
 *       {@code lookupTag(registry, tag)}로 가는데, 이 메서드는 tag가 아직 묶이지 않았으면 곧바로
 *       {@code DataResult.error("Missing tag: ...")}를 돌려준다;</li>
 *   <li>{@code MappedRegistry#get(TagKey)}는 {@code allTags}를 읽는데, {@code allTags}는
 *       {@code Registry.PendingTags#apply()} 이후에야 내용이 생긴다;</li>
 *   <li>{@code ReloadableServerResources#loadResources}는 {@code postponedTags}를 보관해 두고,
 *       실제 {@code updateComponentsAndStaticRegistryTags()}
 *       (내부에서 {@code postponedTags.forEach(PendingTags::apply)})
 *       는 {@code MinecraftServer#reloadResources}의 {@code thenAcceptAsync} 안에서 실행되어
 *       <b>모든 reload listener가 끝난 뒤</b>에 돈다.</li>
 * </ol>
 *
 * 우리 {@code CommonDataManager}(레시피 로더)가 바로 reload listener이므로,
 * 그것이 {@code apply()}할 때는 item tag를 하나도 찾지 못한다 → {@code #tag}가 든 재료마다 예외가 난다 →
 * {@code JsonDataManager#apply}가 {@code JsonParseException}을 잡아 error 한 줄만 남긴다 →
 * <b>레시피 전체가 조용히 버려진다</b>.
 *
 * <p>실측 결과 기본 레시피 172개 중 {@code attachments/ammo_mod_he.json}(고폭탄)만 {@code #tag}가 없어,
 * "고폭탄 하나만 조회된다" — 사용자 관찰과 정확히 맞았다. 실행 중 실험으로도 확인했다:
 * {@code "#c:ingots/copper"} → {@code FAIL: Missing tag: 'c:ingots/copper' in 'minecraft:item'},
 * {@code "minecraft:crying_obsidian"} → OK.
 *
 * <p>원본 1.21.1과 비교: 원본도 역직렬화 그 자리에서 해석하지만, 원본 레시피는 바닐라 {@code RecipeManager} 경로를 타며
 * 그 ops는 {@code ReloadableServerResources}의
 * {@code loadingContext = fullRegistries.lookupWithUpdatedTags()}에서 온다 —
 * <b>그것은 이미 새 tag가 반영된 lookup</b>이라 원본에서는 이 문제가 생기지 않는다.
 * 12차에서 레시피를 자체 {@code DataType.RECIPES} 동기화 경로로 바꾸면서 이 lookup을 잃었으므로,
 * 해석을 tag가 묶인 뒤로 직접 미뤄야 한다.
 */
public class GunSmithTableIngredient {
    private final int count;

    @Nullable
    private Ingredient ingredient;
    /** 아직 해석하지 않은 {@code "item"} 필드 원문. 해석에 성공하면 비운다. */
    @Nullable
    private JsonElement rawItem;
    /** "이미 로그를 남겼는지"만 기록하고 실패 결과는 캐시하지 않는다 — 너무 이른 호출 한 번이 재료를 영구히 망가뜨리는 것을 막는다. */
    private boolean loggedFailure;

    public GunSmithTableIngredient(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    /** 지연 해석용 생성자. 클래스 주석 참고. */
    public GunSmithTableIngredient(JsonElement rawItem, int count) {
        this.rawItem = rawItem;
        this.count = count;
    }

    /**
     * @return 해석한 {@link Ingredient}. 원본 JSON을 아직도 해석할 수 없으면(예: tag가 정말 없음) {@code null}을 돌려준다.
     *         호출하는 쪽은 반드시 null을 확인해야 한다 — 이것이 "레시피 전체가 사라짐"과 "재료 칸은 비었지만 레시피는 남음"의 차이다.
     */
    @Nullable
    public Ingredient getIngredient() {
        if (this.ingredient == null && this.rawItem != null) {
            JsonElement raw = this.rawItem;
            try {
                this.ingredient = Ingredient.CODEC.parse(
                        RegistryOps.create(JsonOps.INSTANCE,
                                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)),
                        normalizeLegacy(raw)
                ).getOrThrow();
                this.rawItem = null;
            } catch (RuntimeException e) {
                if (!this.loggedFailure) {
                    this.loggedFailure = true;
                    GunMod.LOGGER.error("Failed to resolve gun smith table ingredient {}", raw, e);
                }
            }
        }
        return this.ingredient;
    }

    /**
     * 1.20 이하의 <b>객체형</b> Ingredient 표기를 26.2의 문자열 표기로 정규화한다.
     *
     * <pre>
     * {"tag":  "forge:ingots/iron"}  ->  "#forge:ingots/iron"
     * {"item": "minecraft:flint"}    ->  "minecraft:flint"
     * </pre>
     *
     * <h2>변환해야 하는 이유</h2>
     * 26.2의 {@code Ingredient.CODEC}은
     * {@code ExtraCodecs.nonEmptyHolderSet(HolderSetCodec.create(Registries.ITEM, ...))}
     * 이고(바이트코드 확인), {@code HolderSetCodec}은 <b>문자열 또는 문자열 배열만 받는다</b> —
     * {@code "#tag"}는 tag, 맨 id는 아이템 하나를 뜻한다.
     * {@code {"tag": ...}} / {@code {"item": ...}} 같은 객체 형식은 <b>인식하지 않으며</b>,
     * 객체를 만나면 바로 {@code DataResult.error}를 돌려준다.
     *
     * <p>그런데 1.20 이하의 총기 팩은 바로 객체 형식으로 적혀 있다. 실측 결과 서드파티 팩
     * GunpowderRevolution v1.2.7의 레시피 68개가 <b>모두</b>
     * {@code {"tag": "forge:ingots/iron"}} 표기를 써서 재료마다 해석에 실패했고,
     * "레시피 항목도 있고 재료 수도 있지만 재료 칸에 아이콘이 없고 제작할 수 없음"으로 나타났다
     * — {@code getIngredient()}가 null을 돌려줘
     * "재료 칸은 비었지만 레시피는 남음" 분기를 탔기 때문이다(클래스 주석 참고).
     *
     * <p>이는 지난 차수에 고친 {@code forge/tags/items → item}과 <b>별개의 문제</b>다:
     * 그때는 "tag 정의 파일을 불러오지 못함"을 고쳤고, 이번에는 "tag를 참조하는 표기를 인식하지 못함"을 고친다.
     * 둘 다 고쳐야 예전 총기 팩이 실제로 동작한다.
     *
     * <h2>변환기가 아니라 여기에 두는 이유</h2>
     * 이 팩은 이미 <b>새 배치</b>(자체 {@code gunpack.meta.json} 포함)라
     * {@code tacz/}에 넣기만 하면 로드되고, {@code PackConvertor}를 전혀 거치지 않는다.
     * 즉 이 경로는 예전 표기를 스스로 받아들여야 하며 "먼저 한 번 변환하기"를 기대할 수 없다.
     *
     * <p>배열 형식({@code [{"tag":...},{"item":...}]})도 항목마다 똑같이 처리한다 —
     * 예전 형식은 배열로 "여럿 중 하나"를 나타냈고, 새 형식은 문자열 배열이다.
     *
     * @return 정규화한 요소. 이미 새 표기이면 아무것도 바꾸지 않고 <b>그대로 돌려준다</b>
     */
    private static JsonElement normalizeLegacy(JsonElement raw) {
        if (raw.isJsonObject()) {
            JsonObject obj = raw.getAsJsonObject();
            // [type이 있는 사용자 정의 Ingredient] Fabric의 사용자 정의 Ingredient 형식으로 고쳐 쓴다.
            //
            // 예전 Forge 생태계에는 사용자 정의 Ingredient 종류가 여럿 있었고, 이 프로젝트는 util/forge 아래에
            // 대응하는 Fabric판(PartialNBTIngredient / StrictNBTIngredient)을 구현해
            // TaCZFabric#onInitialize에서 이미 등록했다. 여기서는 [JSON 표기]만 맞춘다:
            //
            //   Forge : {"type":"forge:partial_nbt","item":"tacz:modern_kinetic_gun",
            //            "nbt":{"GunId":"hamster:coltm1892"}}
            //   Fabric: {"fabric:type":"forge:partial_nbt","items":["tacz:modern_kinetic_gun"],
            //            "nbt":{"GunId":"hamster:coltm1892"}}
            //
            // 두 차이 모두 강제 조건이다(모두 소스로 확인):
            //   1. 판별 키는 반드시 "fabric:type"(CustomIngredientImpl.TYPE_KEY 상수)이어야 하며,
            //      Ingredient.CODEC는 Fabric의 IngredientMixin이
            //      CustomIngredientImpl.CODEC.dispatch(TYPE_KEY, ...)로 넘겨받는다;
            //   2. 우리 Serializer가 선언한 필드 이름은 "items"이고 [목록]이다
            //      (holderByNameCodec().listOf().fieldOf("items")). 반면 Forge는
            //      단수 "item" 문자열로 쓴다.
            //
            // 의미는 전혀 바뀌지 않는다: 여전히 "그 NBT를 가진 바로 그 아이템이어야 함"이며,
            // "아무 TACZ 총기"로 완화하지 않는다 — 이것이 예전에 일부러 변환하지 않았던 이유이고,
            // 이제 같은 구현이 생겼으니 게임 동작을 바꾸지 않고 실제로 지원할 수 있다.
            //
            // 우리가 [실제로 등록한] 종류만 고쳐 쓴다. 나머지 type은 모두 그대로 돌려줘
            // CODEC이 스스로 오류를 내게 한다. 잘못된 대체 표기를 짐작해 진짜 문제를 가리는 일을 막는다.
            if (obj.has("type")) {
                return normalizeCustomIngredient(obj);
            }
            // 이 두 키만 인정하며 반드시 문자열이어야 한다. 나머지 경우는 그대로 돌려줘 CODEC이 스스로 오류를 내게 하여,
            // 잘못된 대체 표기를 "짐작"해 진짜 문제를 가리는 일을 막는다.
            JsonElement tag = obj.get("tag");
            if (tag != null && tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString()) {
                return new JsonPrimitive("#" + tag.getAsString());
            }
            JsonElement item = obj.get("item");
            if (item != null && item.isJsonPrimitive() && item.getAsJsonPrimitive().isString()) {
                return new JsonPrimitive(item.getAsString());
            }
            return raw;
        }
        if (raw.isJsonArray()) {
            JsonArray src = raw.getAsJsonArray();
            JsonArray out = new JsonArray(src.size());
            boolean changed = false;
            for (JsonElement e : src) {
                JsonElement n = normalizeLegacy(e);
                changed |= n != e;
                out.add(n);
            }
            return changed ? out : raw;
        }
        return raw;
    }

    /**
     * Fabric판 구현을 등록해 둔 Forge 사용자 정의 Ingredient 종류.
     *
     * <p>{@code util/forge} 아래 두 Serializer의 {@code ID}와 하나씩 대응한다.
     * 이 집합에 없는 type은 전혀 고쳐 쓰지 않는다 — 비슷한 의미를 "짐작"해
     * 레시피 조건을 몰래 바꾸느니 분명하게 실패하는 편이 낫다.
     */
    private static final java.util.Set<String> SUPPORTED_CUSTOM_INGREDIENTS =
            java.util.Set.of("forge:partial_nbt", "forge:nbt");

    /**
     * Forge 표기의 사용자 정의 Ingredient를 Fabric 표기로 고쳐 쓴다. {@link #normalizeLegacy}의 설명 참고.
     *
     * @return 고쳐 쓴 객체. 지원하지 않는 종류이면 <b>그대로 돌려준다</b>
     */
    private static JsonElement normalizeCustomIngredient(JsonObject obj) {
        JsonElement typeElement = obj.get("type");
        if (typeElement == null || !typeElement.isJsonPrimitive() || !typeElement.getAsJsonPrimitive().isString()) {
            return obj;
        }
        String type = typeElement.getAsString();
        if (!SUPPORTED_CUSTOM_INGREDIENTS.contains(type)) {
            return obj;
        }

        JsonObject out = new JsonObject();
        // Fabric의 판별 키. CustomIngredientImpl.TYPE_KEY 참고.
        out.add("fabric:type", new JsonPrimitive(type));
        for (java.util.Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            if ("type".equals(key)) {
                continue;
            }
            // 단수 item(문자열) -> 복수 items(목록). Serializer가 선언한 필드에 맞춘다.
            // 원문에 이미 items가 있으면 그대로 두고 다시 감싸지 않는다.
            if ("item".equals(key) && !obj.has("items")) {
                JsonArray items = new JsonArray(1);
                items.add(entry.getValue());
                out.add("items", items);
                continue;
            }
            out.add(key, entry.getValue());
        }
        return out;
    }

    /** 반드시 null이 아닌 값이 필요한 곳(예: 네트워크 인코딩)에서 쓴다. */
    public Ingredient getIngredientOrThrow() {
        Ingredient resolved = this.getIngredient();
        if (resolved == null) {
            throw new IllegalStateException("Unresolved gun smith table ingredient: " + this.rawItem);
        }
        return resolved;
    }

    /** 재료를 쓸 수 있는지(tag가 묶였고 해석에 성공했는지). */
    public boolean isResolved() {
        return this.getIngredient() != null;
    }

    public int getCount() {
        return count;
    }
}

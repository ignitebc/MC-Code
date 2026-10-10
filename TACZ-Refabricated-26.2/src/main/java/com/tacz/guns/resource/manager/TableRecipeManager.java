package com.tacz.guns.resource.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tacz.guns.GunMod;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import com.tacz.guns.util.ResourceScanner;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 총기 작업대 레시피 로더.
 *
 * <h2>{@code CommonDataManager}를 그대로 쓰지 않고 이 하위 클래스가 필요한 이유</h2>
 * 작업대 레시피와 바닐라 레시피는 <b>같은 디렉터리를 함께 쓴다</b>: {@code data/<ns>/recipe/**.json}.
 * 이는 원본이 정한 배치이며(원본은 바닐라 {@code RecipeManager}가 {@code "type"}으로 나눠 주므로
 * 자연히 자기 몫만 받는다), 디렉터리를 바꾸면 지금 있는 모든 총기 팩이 무효가 되므로 바꿀 수 없다.
 *
 * <p>그러나 26.2 클라이언트에는 완전한 레시피 표가 <b>없어서</b>({@code ClientLevel#recipeAccess}에는
 * {@code propertySet}과 {@code stonecutterRecipes}만 남음), 작업대 레시피는
 * mod가 직접 {@code DataType.RECIPES} 경로로 동기화한다. 이 자체 경로에는 바닐라의 종류별 분배가 없어서
 * {@code FileToIdConverter.json("recipe")}가 <b>모든</b> 네임스페이스의
 * 모든 레시피를 싹 긁어 온다 — 실측: 바닐라 1585개 + 이 모드의 바닐라 형식 레시피 20개
 * ({@code crafting_shaped}/{@code crafting_shapeless})가 모두
 * {@code TableRecipe}의 Gson 해석기로 들어갔다.
 *
 * <p>그것들에는 {@code result.type} 필드가 없어 하나하나가
 * {@code GunSmithTableResultSerializer} 24번째 줄
 * ({@code GsonHelper.getAsString(jsonObject, "type")})에서
 * {@code JsonSyntaxException}을 던져 로그 전체를 채웠다. 더 나쁜 것은 이 무관한 JSON 더미가
 * <b>그대로 네트워크 패킷에 직렬화되어</b> 서버에 들어오는 모든 클라이언트에 보내지고, 클라이언트가 다시 해석해 로그를 또 채운 것이다
 * (충돌 로그의 {@code CommonNetworkCache.parse} 호출 스택이 바로 여기서 왔다).
 *
 * <h2>방법</h2>
 * 해석하기 <b>전에</b> 최상위 {@code "type"}으로 걸러
 * {@code tacz:gun_smith_table_crafting}만 남긴다. 거른 결과가 {@code dataMap}과
 * {@code networkCache}를 함께 정하므로(부모 클래스의 {@code apply}가 같은 입력으로 둘을 만든다)
 * 로그 소음과 네트워크 패킷 크기가 함께 해결된다.
 *
 * <p>종류 id는 문자열 상수로 쓰지 않고 레지스트리에서 {@link ModRecipe#GUN_SMITH_TABLE_CRAFTING}을 거꾸로 찾는다.
 * 그러면 나중에 등록 이름을 바꿔도 여기가 몰래 어긋나지 않는다.
 */
public class TableRecipeManager extends CommonDataManager<TableRecipe> {
    /**
     * 작업대 레시피의 {@code "type"} 값. 하드코딩한 문자열이 아니라 레지스트리에서 가져온다
     * — 클래스 주석 마지막 단락 참고. 대체값은 레지스트리에 이상이 있을 때의 안전망일 뿐이며 정상 경로에서는 쓰이지 않는다.
     */
    private static final String RECIPE_TYPE_ID = resolveRecipeTypeId();

    /**
     * 예전 총기 팩(1.20 이하)이 쓰는 <b>복수</b> 레시피 디렉터리.
     *
     * <p>26.2는 바닐라 데이터 팩 디렉터리를 단수 {@code recipe/}로 통일했지만, <b>작업대 레시피는
     * 바닐라 데이터 팩 로더를 타지 않는다</b> — 이 mod 자체의 {@link DataType#RECIPES} 경로를 탄다
     * (클래스 주석 참고). 해석과 동기화를 모두 우리가 맡으므로 단수여야 한다는 제약이 <b>없으며</b>,
     * 두 가지 과거 배치를 함께 받아들일 수 있다.</p>
     *
     * <p>이것이 "예전 총기 팩을 넣으면 작업대에서 그 팩의 레시피가 하나도 검색되지 않음"의 근본 원인이었다: 그 팩들의 레시피는 {@code data/<ns>/recipes/} 아래에 있는데
     * 우리는 {@code recipe/}만 훑어서 <b>팩 전체 레시피가 조용히 무시되었다</b>
     * (로더 입장에서는 존재하지 않는 파일 묶음이라 아무 오류도 없었다).</p>
     */
    private static final String LEGACY_RECIPE_DIRECTORY = "recipes";

    /** 예전 디렉터리 탐색기. 부모 클래스의 {@code recipe} 탐색기와 <b>나란히</b> 쓰며, 대체하지 않는다. */
    private static final FileToIdConverter LEGACY_CONVERTER = FileToIdConverter.json(LEGACY_RECIPE_DIRECTORY);

    public TableRecipeManager() {
        // 디렉터리는 바닐라 데이터 팩 레시피와 같다(data/<ns>/recipe). 원본이 정한 배치라 바꿀 수 없다.
        // 예전 총기 팩의 data/<ns>/recipes(복수)는 아래 prepare() 재정의가 추가로 훑는다.
        super(DataType.RECIPES, TableRecipe.class, CommonAssetsManager.GSON, "recipe", "TableRecipeLoader");
    }

    /**
     * {@code recipe/}(현재 배치)와 {@code recipes/}(예전 총기 팩 배치)를 함께 훑는다.
     *
     * <h2>로드 뒤에 보충하지 않고 prepare 단계에서 합치는 이유</h2>
     * {@code prepare}는 "어떤 후보 파일이 있는지"를 정할 수 있는 유일한 곳이며,
     * 이후의 {@link #apply}는 기존 집합에서 거르기만 할 수 있다. 예전 디렉터리를 여기서 넣지 않으면
     * 이후 어느 단계에서도 보충할 방법이 없다.
     *
     * <h2>충돌 처리</h2>
     * 두 디렉터리의 파일은 <b>같은 형식</b>의 Identifier(네임스페이스 + 상대 경로)로 매핑되므로,
     * 한 팩이 두 곳에 같은 이름의 레시피를 두면 키가 충돌한다.
     * 여기서는 <b>새 디렉터리를 우선</b>한다: 예전 것을 먼저 넣고 새것으로 덮어쓴다.
     * 총기 팩 작성자가 이미 26.2 대응을 했다면({@code recipe/}에 씀)
     * 그쪽이 남아 있는 예전 파일보다 분명히 더 믿을 만하기 때문이다.
     */
    @NotNull
    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<Identifier, JsonElement> legacy =
                ResourceScanner.scanDirectory(pResourceManager, LEGACY_CONVERTER, CommonAssetsManager.GSON);
        Map<Identifier, JsonElement> current = super.prepare(pResourceManager, pProfiler);
        if (legacy.isEmpty()) {
            return current;
        }
        // 새 디렉터리가 같은 이름 항목을 우선 덮어쓴다. 메서드 주석 참고.
        Map<Identifier, JsonElement> merged = new LinkedHashMap<>(legacy);
        merged.putAll(current);
        GunMod.LOGGER.info(getMarker(),
                "Found {} recipe file(s) in legacy 'recipes/' directory (old gun pack layout), {} in 'recipe/'.",
                legacy.size(), current.size());
        return merged;
    }

    private static String resolveRecipeTypeId() {
        Identifier id = BuiltInRegistries.RECIPE_TYPE.getKey(ModRecipe.GUN_SMITH_TABLE_CRAFTING);
        return id != null ? id.toString() : GunMod.MOD_ID + ":gun_smith_table_crafting";
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        // LinkedHashMap으로 순서를 지켜, 문제를 재현할 때 로그 순서와 맞춰 보기 쉽게 한다.
        Map<Identifier, JsonElement> ours = new LinkedHashMap<>();
        for (Map.Entry<Identifier, JsonElement> entry : pObject.entrySet()) {
            if (isGunSmithTableRecipe(entry.getValue())) {
                ours.put(entry.getKey(), entry.getValue());
            }
        }
        GunMod.LOGGER.debug(getMarker(), "Gun smith table recipes: {} accepted, {} foreign recipe files skipped",
                ours.size(), pObject.size() - ours.size());
        // 부모 클래스는 이것 하나(이것만)로 dataMap과 networkCache를 함께 만든다.
        super.apply(ours, pResourceManager, pProfiler);
    }

    private static boolean isGunSmithTableRecipe(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return false;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement type = object.get("type");
        // 문자열형 최상위 type만 인정한다. 문자열이 아니면(배열/객체) 모두 외부 파일로 보고 건너뛰며
        // 예외를 던지지 않는다 — 이 경로에서는 언제든 다른 모드의 자체 형식을 만날 수 있다.
        if (type == null || !type.isJsonPrimitive() || !type.getAsJsonPrimitive().isString()) {
            return false;
        }
        return RECIPE_TYPE_ID.equals(type.getAsString());
    }
}

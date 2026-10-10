package com.tacz.guns.loot;

import com.tacz.guns.GunMod;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 총기 전리품 주입: 총기 팩이 정의한 {@code loot_injection}을 바닐라 전리품 표의 결과에 덧붙인다.
 *
 * <h2>39차: 봉인을 풀고 이전 TODO의 판단을 바로잡는다</h2>
 * 예전 TODO에는 "26.2의 전리품 표는 HolderGetter/LootContext로 해석되므로
 * {@code ServerLevel.registryAccess()}에서 <b>얻을 수 없고</b>, 블록이 드롭될 때 충돌한다"고 적혀 있었다.
 * 이번 차수에 26.2 바이트코드를 메서드마다 대조한 결론은 <b>이 말이 절반만 맞다</b>는 것이다:
 * <ul>
 *   <li>{@code ServerLevel#registryAccess()} — <b>실제로 더는 없다</b>(비슷한 것은
 *       {@code recipeAccess}뿐). 1.21.1 표기를 그대로 베끼면 반드시 컴파일에 실패한다;</li>
 *   <li>그러나 {@code MinecraftServer#registryAccess()}는 <b>아직 있으며</b>
 *       {@code RegistryAccess$Frozen}을 돌려준다. {@code RegistryAccess}의
 *       {@code lookupOrThrow(ResourceKey)} → {@code Registry}와
 *       {@code Registry#getKey(Object)} → {@code Identifier}도 모두 있다.</li>
 * </ul>
 * 즉 "{@code context.getLevel().getServer().registryAccess()}" 사슬은
 * <b>이어진다</b> — 충돌 원인은 API가 사라진 것이 아니라 그때 바로
 * {@code ServerLevel#registryAccess()}를 썼기 때문이다. ID를 거꾸로 찾는 발상 자체는 계속 쓸 수 있으며,
 * TODO 말처럼 자원 로드 경로로 옮겨 다시 만들 필요는 없다.
 *
 * <h2>이번 차수에 실제로 고친 문제 두 개</h2>
 * <ol>
 *   <li><b>{@code ID_CACHE} 메모리 누수.</b> 원래는 {@link HashMap}에 {@code LootTable}
 *       인스턴스를 <b>강한 참조</b> 키로 썼다. 전리품 표는
 *       {@code /reload}, 저장 파일 전환, 리소스 팩 다시 불러오기 때마다 통째로 다시 만들어지는데,
 *       예전 표 인스턴스를 이 static map이 계속 붙잡아 영원히 회수되지 않았다.
 *       {@link WeakHashMap} + {@code synchronizedMap}으로 바꿨다:
 *       표 인스턴스를 아무도 참조하지 않으면 회수된다. 잠그는 이유는 전리품 생성이
 *       서버의 여러 스레드에서 일어날 수 있기 때문이다.</li>
 *   <li><b>{@code getLevel()}이 null일 수 있다.</b> {@code LootContext#getLevel()}은
 *       실제로 {@code params.getLevel()}이며(바이트코드 확인), {@code LootParams}의
 *       {@code level} 필드는 생성자가 바로 써서 이론상 null이 아니다. 그러나 전리품 표는
 *       데이터 생성기 / 서드파티 mod가 월드 없는 환경에서 돌릴 수도 있다. 여기에 방어를 하나 더 두어
 *       어느 단계에서든 값을 얻지 못하면 그대로 돌려주며, 주입 로직이 주 흐름을 망가뜨리지 않게 한다.</li>
 * </ol>
 */
public class LootTableInjectorModifier {
    /**
     * 전리품 표 → 레지스트리 ID 캐시.
     *
     * <p>{@link HashMap} 대신 {@link WeakHashMap}을 쓴다: 키가 {@code LootTable} 인스턴스이고
     * {@code /reload}마다 새 인스턴스로 바뀌므로, 강한 참조를 쓰면 예전 표가 영원히 남는다.</p>
     */
    private static final Map<LootTable, Identifier> ID_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * 표지값: "이미 조회했고 이 표는 주입 대상이 아님을 확인함"을 뜻한다.
     *
     * <p>참조 동일성({@code ==})으로 비교하며 레지스트리 조회에 전혀 관여하지 않는다.</p>
     */
    private static final Identifier NOT_A_TARGET = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "not_a_loot_target");

    public static @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context, LootTable table) {
        // [40차] 전체 안전망.
        //
        // 이 메서드는 LootTable#getRandomItems의 반환값에 걸려 있으며 <b>블록 드롭 주 경로</b>에 있다
        // (BlockBehaviour#getDrops -> Block#dropResources. 물에 블록이 쓸려 부서질 때도 이리로 온다).
        // 지난 차수에는 여기서 IllegalStateException을 던져 "블록 캐기 / 물 퍼짐"이 서버를 바로 멈추게 했다.
        //
        // 전리품 주입은 <b>덤</b> 기능이므로 주 흐름을 망가뜨려서는 안 된다.
        // 그래서 안에서 어떤 문제가 생겨도 로그만 남기고 원래 드롭을 그대로 돌려준다.
        try {
            return doApplyUnsafe(generatedLoot, context, table);
        } catch (Exception e) {
            if (WARNED.compareAndSet(false, true)) {
                GunMod.LOGGER.error(
                        "TACZ loot injection failed; falling back to vanilla drops. "
                                + "This message is logged only once per session.", e);
            }
            return generatedLoot;
        }
    }

    /** 주입 실패는 세션마다 한 번만 알려, 드롭할 때마다 로그가 넘치지 않게 한다. */
    private static final AtomicBoolean WARNED = new AtomicBoolean(false);

    private static @NotNull ObjectArrayList<ItemStack> doApplyUnsafe(ObjectArrayList<ItemStack> generatedLoot, LootContext context, LootTable table) {
        CommonAssetsManager manager = CommonAssetsManager.getInstance();
        if (manager == null) {
            return generatedLoot;
        }
        // 빠른 종료: 총기 팩이 주입 대상을 하나도 선언하지 않았으면 아무것도 해석하지 않는다(대부분 저장 파일의 평소 상태).
        if (manager.getLootInjectionTargets().isEmpty()) {
            return generatedLoot;
        }

        // computeIfAbsent + null을 그대로 쓰면 안 된다: Map 규약상 "null로 매핑"은 "없음"과 같아서,
        // 대상이 아닌 표가 드롭할 때마다 resolveId(후보 집합 순회)를 다시 돌리게 된다.
        // 대부분 블록은 주입 대상이 아니므로 — 가장 뜨거운 경로에 비용을 얹는 셈이다.
        // 여기서는 NOT_A_TARGET 표지값으로 "조회했고 대상이 아님을 확인함"도 캐시한다.
        Identifier lootTableId = ID_CACHE.computeIfAbsent(table, lootTable -> {
            Identifier resolved = resolveId(context, lootTable);
            return resolved == null ? NOT_A_TARGET : resolved;
        });
        if (lootTableId == NOT_A_TARGET) {
            return generatedLoot;
        }

        List<LootTableInjection> injections = manager.getLootTableInjections(lootTableId);
        if (injections.isEmpty()) {
            return generatedLoot;
        }

        for (LootTableInjection injection : injections) {
            generatedLoot.addAll(injection.createStacks(context));
        }
        return generatedLoot;
    }

    /**
     * 전리품 표의 등록 ID를 거꾸로 찾는다.
     *
     * <h2>40차: 지난 차수의 {@code server.registryAccess()}는 틀렸고, 실제로 충돌했다</h2>
     * 충돌 로그(블록을 캐거나 물에 블록이 쓸려 부서질 때 반드시 발생):
     * <pre>
     * IllegalStateException: Missing registry: ResourceKey[minecraft:root / minecraft:loot_table]
     *   at RegistryAccess.lookupOrThrow(RegistryAccess.java:22)
     *   at LootTableInjectorModifier.resolveId
     * </pre>
     *
     * <p>지난 차수에는 "{@code MinecraftServer#registryAccess()} 메서드가 있다"는 것만 확인하고
     * <b>"전리품 표가 실제로 이 RegistryAccess 안에 있는지"는 확인하지 않았다</b> — 둘은 다른 문제다.
     * 26.2의 레지스트리 계층({@code RegistryLayer} 열거형)은
     * {@code STATIC} / {@code WORLDGEN} / {@code DIMENSIONS} / {@code RELOADABLE}이다.
     * {@code server.registryAccess()}는 앞의 세 계층만 다루고,
     * 전리품 표는 <b>데이터 팩과 함께 다시 불러오는</b> {@code RELOADABLE} 계층에 속해
     * 별도의 {@code ReloadableServerRegistries.Holder}가 가진다
     * ({@code MinecraftServer#reloadableRegistries()}).
     * 그러니 그 TODO가 말한 "registryAccess에서 얻을 수 없다"는 <b>완전히 맞았고</b>, 내가 잘못 뒤집었다.</p>
     *
     * <h2>계속 거꾸로 찾지 않고 "정방향 조회"로 바꾼 이유</h2>
     * {@code reloadableRegistries().lookup()}이 돌려주는 것은 {@link HolderLookup.Provider}이며,
     * key로 값을 꺼낼 수만 있고 값으로 key를 거꾸로 찾는 메서드는 <b>없다</b>
     * ({@code HolderLookup}에는 {@code key()}만 있고, 이는 레지스트리 자신의 key를 돌려준다).
     * {@code listElements()}로 표 전체를 훑어 거꾸로 찾을 수는 있지만, 그것은 O(n)이고 캐시 무효화도 처리해야 한다.
     *
     * <p>더 안정적인 방법은 방향을 바꾸는 것이다: <b>어느 표에 주입할지는 원래 알고 있다</b> —
     * {@code LootInjectionManager}의 {@code injections}가 바로 "대상 표 ID"를 키로 하는 Map이다.
     * 그래서 여기서는 총기 팩이 선언한 몇 개의 ID(기본 총기 팩은 1개뿐:
     * {@code minecraft:chests/spawn_bonus_chest})로 정방향 조회해 인스턴스가 같은지 비교하면 된다.
     * 후보 집합은 보통 한 자릿수이며, "레지스트리가 없다" 같은 실행 중 가정을 완전히 피한다.</p>
     *
     * @return 찾지 못하면 {@code null}을 돌려주고, 호출하는 쪽은 주입을 그대로 건너뛴다
     */
    @Nullable
    private static Identifier resolveId(LootContext context, LootTable lootTable) {
        ServerLevel level = context.getLevel();
        if (level == null) {
            return null;
        }
        MinecraftServer server = level.getServer();
        if (server == null) {
            return null;
        }
        CommonAssetsManager manager = CommonAssetsManager.getInstance();
        if (manager == null) {
            return null;
        }
        // "총기 팩이 주입하겠다고 선언한 표" 안에서만 찾고, 레지스트리 전체를 거꾸로 찾지 않는다.
        Set<Identifier> candidates = manager.getLootInjectionTargets();
        if (candidates.isEmpty()) {
            return null;
        }
        HolderLookup.Provider lookup = server.reloadableRegistries().lookup();
        // [r41] 여기 제네릭에는 함정이 두 개 있으며, 둘 다 26.2의 <b>제네릭 시그니처</b>(서술자가 아님)로 대조했다:
        //
        // 1. Provider#lookup의 시그니처는
        //        <T> Optional<? extends RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>>)
        //    반환값이 [공변] Optional<? extends ...>이라
        //    Optional<RegistryLookup<LootTable>>에 대입할 수 없다 — r40은 바로 여기서 컴파일에 실패했다.
        // 2. var로 바꿔도 추론되는 것은 RegistryLookup<capture of ? extends LootTable>이라,
        //    이것으로 get(ResourceKey<LootTable>)을 부르면 캡처 타입이 맞지 않아 여전히 오류가 날 수 있다.
        //
        // 그래서 lookupOrThrow를 쓴다: 그 시그니처는
        //        <T> RegistryLookup<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>>)
        //    [와일드카드가 없는] RegistryLookup<T>를 돌려주고, T는 인자에서 바로 LootTable로 추론되어
        //    두 함정을 모두 피한다. 레지스트리가 없으면 IllegalStateException을 던지지만,
        //    이 메서드 전체가 doApply의 try-catch에 감싸여 있으므로(그곳 설명 참고)
        //    r40처럼 블록 드롭을 망가뜨리지 않는다.
        HolderLookup.RegistryLookup<LootTable> registry = lookup.lookupOrThrow(Registries.LOOT_TABLE);
        for (Identifier candidate : candidates) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, candidate);
            LootTable value = registry.get(key).map(Holder::value).orElse(null);
            if (value == lootTable) {
                return candidate;
            }
        }
        return null;
    }
}
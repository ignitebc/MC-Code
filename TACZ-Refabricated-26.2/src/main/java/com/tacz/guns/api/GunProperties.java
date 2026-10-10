package com.tacz.guns.api;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.tacz.guns.api.modifier.ParameterizedCachePair;
import com.tacz.guns.resource.modifier.custom.InaccuracyModifier;
import com.tacz.guns.resource.pojo.data.gun.*;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * AttachmentCacheProperty에 쓰는 타입 안전 키.
 */
public class GunProperties {
    /**
     * @since 1.1.7
     */
    static final Map<String, GunProperty<?>> ALL = new ConcurrentHashMap<>();

    /**
     * @since 1.1.7
     */
    static final Supplier<Map<String, GunProperty<?>>> ALL_CACHE_MODIFIABLE_BY_SCRIPT = Suppliers.memoize(() -> List.<GunProperty<?>>of(
            GunProperties.AMMO_SPEED,
            GunProperties.ARMOR_IGNORE,
            GunProperties.EFFECTIVE_RANGE,
            GunProperties.HEADSHOT_MULTIPLIER,
            GunProperties.KNOCKBACK,
            GunProperties.PIERCE,
            GunProperties.WEIGHT
    ).stream().collect(Collectors.toMap(GunProperty::name, Function.identity())));

    /**
     * 모든 속성을 돌려준다
     *
     * @author ChloePrime
     * @since 1.1.7
     */
    public static Map<String, GunProperty<?>> all() {
        return Collections.unmodifiableMap(ALL);
    }

    /**
     * 부착물 캐시 안에서 스크립트가 바꿀 수 있는 속성을 모두 돌려준다
     *
     * @author ChloePrime
     * @since 1.1.7
     */
    public static Map<String, GunProperty<?>> allCacheModifiableByScript() {
        return ALL_CACHE_MODIFIABLE_BY_SCRIPT.get();
    }

    public static final GunProperty<Float> ADS_TIME = GunProperty.of("ads", Float.class);
    /**
     * @deprecated 이 클래스는 실수로 생긴 설계 결함이며 기능이 {@link InaccuracyModifier}와 완전히 겹친다<br/>
     * 더 이상 쓰지 않고 안의 메서드는 실제로 실행되지 않는다. {@link InaccuracyModifier}를 쓴다 <br/>
     * <p>
     * 이 Modifier의 ID도 {@link InaccuracyModifier}로 넘겨 두었다 <br/>
     */
    @Deprecated
    public static final GunProperty<Map<InaccuracyType, Float>> AIM_INACCURACY = GunProperty.of("inaccuracy", new TypeToken<>() {
    });

    @CacheModifiableByScript
    @ValueModifiableAtRuntime(Float.class)
    public static final GunProperty<Float> AMMO_SPEED = GunProperty.of("ammo_speed", Float.class);

    @CacheModifiableByScript
    @ValueModifiableAtRuntime(Float.class)
    public static final GunProperty<Float> ARMOR_IGNORE = GunProperty.of("armor_ignore", Float.class);

    /**
     * 총기 피해량.
     * 실제 적용 값은 명중할 때 스크립트가 바꾼다.
     */
    @ValueModifiableAtRuntime(Float.class)
    public static final GunProperty<LinkedList<ExtraDamage.DistanceDamagePair>> DAMAGE = GunProperty.of("damage", new TypeToken<>() {
    });

    @CacheModifiableByScript
    @ValueModifiableAtRuntime(Float.class)
    public static final GunProperty<Float> EFFECTIVE_RANGE = GunProperty.of("effective_range", Float.class);

    /**
     * @see RuntimeOnly#EXPLODE_ENABLED
     * @see RuntimeOnly#EXPLOSION_DAMAGE
     * @see RuntimeOnly#EXPLOSION_RADIUS
     * @see RuntimeOnly#EXPLOSION_KNOCKBACK
     * @see RuntimeOnly#EXPLOSION_DESTROYS_BLOCK
     * @see RuntimeOnly#EXPLOSION_DELAY
     */
    public static final GunProperty<ExplosionData> EXPLOSION = GunProperty.of("explosion", ExplosionData.class);

    public static final GunProperty<MoveSpeed> MOVE_SPEED = GunProperty.of("movement_speed", MoveSpeed.class);

    @CacheModifiableByScript
    @ValueModifiableAtRuntime(Float.class)
    public static final GunProperty<Float> HEADSHOT_MULTIPLIER = GunProperty.of("head_shot", Float.class);

    /**
     * @see RuntimeOnly#IGNITE_ENTITY
     * @see RuntimeOnly#IGNITE_ENTITY_TIME
     * @see RuntimeOnly#IGNITE_BLOCK
     */
    public static final GunProperty<Ignite> IGNITE = GunProperty.of("ignite", Ignite.class);

    @ValueModifiableAtRuntime(Float.class)
    public static final GunProperty<Map<InaccuracyType, Float>> INACCURACY = GunProperty.of("inaccuracy", new TypeToken<>() {
    });

    @CacheModifiableByScript
    @ValueModifiableAtRuntime(Float.class)
    public static final GunProperty<Float> KNOCKBACK = GunProperty.of("knockback", Float.class);

    @CacheModifiableByScript
    @ValueModifiableAtRuntime(Integer.class)
    public static final GunProperty<Integer> PIERCE = GunProperty.of("pierce", Integer.class);

    public static final GunProperty<ParameterizedCachePair<Float, Float>> RECOIL = GunProperty.of("recoil", new TypeToken<>() {
    });
    public static final GunProperty<Integer> ROUNDS_PER_MINUTE = GunProperty.of("rpm", Integer.class);

    /**
     * @see RuntimeOnly#SOUND_DISTANCE
     */
    public static final GunProperty<Pair<Integer, Boolean>> SILENCE = GunProperty.of("silence", new TypeToken<>() {
    });

    @CacheModifiableByScript
    public static final GunProperty<Float> WEIGHT = GunProperty.of("weight_modifier", Float.class);

    /**
     * 문서 전용 클래스로,
     * 부착물 캐시에는 없고 스크립트 실행 중에만 바꿀 수 있는 총기 속성을 소개한다.
     *
     * @author ChloePrime
     * @since 1.1.7
     */
    @ApiStatus.Experimental
    public static final class RuntimeOnly {
        /**
         * 열량 상한
         */
        @ValueModifiableAtRuntime(Float.class)
        public static final String MAX_HEAT = "max_heat";

        /**
         * 탄환(펠릿) 수
         */
        @ValueModifiableAtRuntime(Integer.class)
        public static final String BULLET_AMOUNT = "bullet_amount";

        /**
         * 연발 수.
         * Burst 모드가 아니어도 적용된다
         */
        @ValueModifiableAtRuntime(Integer.class)
        public static final String BURST_COUNT = "burst_count";

        /**
         * 연발 간격(밀리초, ms)
         * Burst 모드가 아니어도 적용된다
         */
        @ValueModifiableAtRuntime(Long.class)
        public static final String BURST_SHOOT_INTERVAL = "burst_shoot_interval";

        /**
         * 탄환 수명(초, s)
         */
        @ValueModifiableAtRuntime(Float.class)
        public static final String BULLET_LIFE = "bullet_life";

        /**
         * 탄환 중력
         */
        @ValueModifiableAtRuntime(Float.class)
        public static final String BULLET_GRAVITY = "bullet_gravity";

        /**
         * 탄환 공기 저항
         */
        @ValueModifiableAtRuntime(Float.class)
        public static final String BULLET_FRICTION = "bullet_friction";

        /**
         * 탄환 소리 전달 거리
         */
        @ValueModifiableAtRuntime(Integer.class)
        public static final String SOUND_DISTANCE = "sound_distance";

        /**
         * 엔티티에 불을 붙일지
         */
        @ValueModifiableAtRuntime(Boolean.class)
        public static final String IGNITE_ENTITY = "ignite_entity";

        /**
         * 엔티티에 붙는 불의 지속 시간(틱)
         */
        @ValueModifiableAtRuntime(Integer.class)
        public static final String IGNITE_ENTITY_TIME = "ignite_entity_time";

        /**
         * 블록에 불을 붙일지
         */
        @ValueModifiableAtRuntime(Boolean.class)
        public static final String IGNITE_BLOCK = "ignite_block";

        /**
         * 탄환이 폭발하는지.
         * 탄환을 만들 때 바뀐다.
         */
        @ValueModifiableAtRuntime(Boolean.class)
        public static final String EXPLODE_ENABLED = "explode_enabled";

        /**
         * 탄환 폭발 피해량.
         * 탄환을 만들 때 바뀐다.
         */
        @ValueModifiableAtRuntime(Float.class)
        public static final String EXPLOSION_DAMAGE = "explosion_damage";

        /**
         * 탄환 폭발 반경.
         * 탄환을 만들 때 바뀐다.
         */
        @ValueModifiableAtRuntime(Float.class)
        public static final String EXPLOSION_RADIUS = "explosion_radius";

        /**
         * 탄환 폭발이 밀어내기를 주는지.
         * 탄환을 만들 때 바뀐다.
         */
        @ValueModifiableAtRuntime(Boolean.class)
        public static final String EXPLOSION_KNOCKBACK = "explosion_knockback";

        /**
         * 탄환 폭발이 블록을 부수는지.
         * 탄환을 만들 때 바뀐다.
         */
        @ValueModifiableAtRuntime(Boolean.class)
        public static final String EXPLOSION_DESTROYS_BLOCK = "explosion_destroys_block";

        /**
         * 발사 후 자동으로 폭발할 때까지의 지연(초, s).
         * 탄환을 만들 때 바뀐다.
         */
        @ValueModifiableAtRuntime(Float.class)
        public static final String EXPLOSION_DELAY = "explosion_delay";
    }

    private GunProperties() {
    }
}

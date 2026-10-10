package com.tacz.guns.entity.sync.core;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish.
 * Open source at <a href="https://github.com/MrCrayfish/Framework">Github</a> under LGPL License.
 */
public record SyncedDataKey<E extends Entity, T>(Pair<Identifier, Identifier> pairKey, Identifier id,
                                                 SyncedClassKey<E> classKey, IDataSerializer<T> serializer,
                                                 Supplier<T> defaultValueSupplier, boolean save, boolean persistent,
                                                 SyncMode syncMode) {
    public static <E extends Entity, T> Builder<E, T> builder(SyncedClassKey<E> entityClass, IDataSerializer<T> serializer) {
        return new Builder<>(entityClass, serializer);
    }

    public void setValue(E entity, T value) {
        SyncedEntityData.instance().set(entity, this, value);
    }

    public T getValue(E entity) {
        return SyncedEntityData.instance().get(entity, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SyncedDataKey<?, ?> that = (SyncedDataKey<?, ?>) o;
        return Objects.equals(this.pairKey, that.pairKey);
    }

    @Override
    public int hashCode() {
        return this.pairKey.hashCode();
    }

    public enum SyncMode {
        /**
         * 키를 아예 동기화하지 않는다. 데이터는 서버에서만 쓸 수 있다.
         */
        NONE(false, false),

        /**
         * 데이터를 가진 플레이어를 포함한 모든 플레이어에게 키를 동기화한다. 키가 묶인
         * 엔티티가 플레이어가 아니면 추적 중인 플레이어만 데이터를 받는다.
         */
        ALL(true, true),

        /**
         * 엔티티를 추적 중인 플레이어에게만 키를 동기화한다. 데이터를 가진
         * 엔티티는 클라이언트에서 받지 않는다.
         */
        TRACKING_ONLY(true, false),

        /**
         * 데이터를 가진 엔티티에게만 키를 동기화한다. 엔티티를 추적 중인 플레이어는
         * 클라이언트에서 데이터를 받지 않는다.
         */
        SELF_ONLY(false, true);

        final boolean tracking;
        final boolean self;

        SyncMode(boolean tracking, boolean self) {
            this.tracking = tracking;
            this.self = self;
        }

        public boolean isTracking() {
            return this.tracking;
        }

        public boolean isSelf() {
            return this.self;
        }
    }

    public static class Builder<E extends Entity, T> {
        private final SyncedClassKey<E> classKey;
        private final IDataSerializer<T> serializer;
        private Identifier id;
        private Supplier<T> defaultValueSupplier;
        private boolean save = false;
        private boolean persistent = true;
        private SyncMode syncMode = SyncMode.ALL;

        private Builder(SyncedClassKey<E> classKey, IDataSerializer<T> serializer) {
            this.classKey = classKey;
            this.serializer = serializer;
        }

        public SyncedDataKey<E, T> build() {
            Validate.notNull(this.id, "Missing 'id' when building synced data key");
            Validate.notNull(this.defaultValueSupplier, "Missing 'defaultValueSupplier' when building synced data key");
            Pair<Identifier, Identifier> pairKey = Pair.of(this.classKey.id(), this.id);
            return new SyncedDataKey<>(pairKey, this.id, this.classKey, this.serializer, this.defaultValueSupplier, this.save, this.persistent, this.syncMode);
        }

        /**
         * 동기화 키의 id를 설정한다. 필수 속성이다.
         */
        public Builder<E, T> id(Identifier id) {
            this.id = id;
            return this;
        }

        /**
         * String으로 동기화 키의 id를 설정한다. 필수 속성이다.
         */
        public Builder<E, T> id(String id) {
            this.id = Identifier.parse(id);
            return this;
        }

        /**
         * String으로 동기화 키의 id를 설정한다. 필수 속성이다.
         * <p>
         * 대신 {@link #id(String)}를 쓴다.
         */
        @Deprecated
        public Builder<E, T> key(String key) {
            return id(key);
        }

        /**
         * 동기화 키의 기본값 공급자를 설정한다. 필수 속성이다.
         */
        public Builder<E, T> defaultValueSupplier(Supplier<T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
            return this;
        }

        /**
         * 이 동기화 키를 플레이어 파일에 저장한다. 플레이어가 월드를 다시 불러오거나
         * 서버에 다시 들어와도 데이터가 유지된다.
         */
        public Builder<E, T> saveToFile() {
            this.save = true;
            return this;
        }

        /**
         * 플레이어가 죽었을 때 이 동기화 키가 넘어가지 않게 하고, 데이터를
         * 기본값 공급자의 결과로 되돌린다. 플레이어에게만 효과가 있다.
         */
        public Builder<E, T> resetOnDeath() {
            this.persistent = false;
            return this;
        }

        /**
         * 클라이언트에 데이터를 보낼 때 쓰는 동기화 방식.
         * 자세한 내용은 {@link SyncMode} 참고
         */
        public Builder<E, T> syncMode(SyncMode mode) {
            this.syncMode = mode;
            return this;
        }
    }
}
package com.tacz.guns.entity.sync.core;

import com.google.common.collect.ImmutableSet;
import com.tacz.guns.GunMod;
import com.tacz.guns.init.CommonRegistry;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish.
 * Open source at <a href="https://github.com/MrCrayfish/Framework">Github</a> under LGPL License.
 */
public class SyncedEntityData {
    private static final Marker SYNCED_ENTITY_DATA_MARKER = MarkerFactory.getMarker("SYNCED_ENTITY_DATA_TAC_COPY");
    private static SyncedEntityData INSTANCE;

    private final Set<SyncedClassKey<?>> registeredClassKeys = new HashSet<>();
    private final Object2ObjectMap<Identifier, SyncedClassKey<?>> idToClassKey = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, SyncedClassKey<?>> classNameToClassKey = new Object2ObjectOpenHashMap<>();
    private final Map<String, Boolean> clientClassNameCapabilityCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> serverClassNameCapabilityCache = new ConcurrentHashMap<>();

    private final Set<SyncedDataKey<?, ?>> registeredDataKeys = new HashSet<>();
    private final Reference2ObjectMap<SyncedClassKey<?>, HashMap<Identifier, SyncedDataKey<?, ?>>> classToKeys = new Reference2ObjectOpenHashMap<>();
    private final Reference2IntMap<SyncedDataKey<?, ?>> internalIds = new Reference2IntOpenHashMap<>();
    private final Int2ReferenceMap<SyncedDataKey<?, ?>> syncedIdToKey = new Int2ReferenceOpenHashMap<>();

    private final AtomicInteger nextIdTracker = new AtomicInteger();
    private final List<Entity> dirtyEntities = new ArrayList<>();
    private boolean dirty = false;

    private SyncedEntityData() {
    }

    public static SyncedEntityData instance() {
        if (INSTANCE == null) {
            INSTANCE = new SyncedEntityData();
        }
        return INSTANCE;
    }

    private <E extends Entity> void registerClassKey(SyncedClassKey<E> classKey) {
        if (!this.registeredClassKeys.contains(classKey)) {
            this.registeredClassKeys.add(classKey);
            this.idToClassKey.put(classKey.id(), classKey);
            this.classNameToClassKey.put(classKey.entityClass().getName(), classKey);
        }
    }

    /**
     * 동기화 데이터 키를 시스템에 등록한다.
     *
     * @param dataKey 동기화 데이터 키 인스턴스
     */
    public synchronized <E extends Entity, T> void registerDataKey(SyncedDataKey<E, T> dataKey) {
        Identifier keyId = dataKey.id();
        SyncedClassKey<E> classKey = dataKey.classKey();
        if (CommonRegistry.isLoadComplete()) {
            throw new IllegalStateException(String.format("Tried to register synced data key %s for %s after game initialization", keyId, classKey.id()));
        }
        if (this.registeredDataKeys.contains(dataKey)) {
            throw new IllegalArgumentException(String.format("The synced data key %s for %s is already registered", keyId, classKey.id()));
        }
        // 클래스 키 등록을 시도한다. 이미 등록되어 있으면 무시한다.
        this.registerClassKey(dataKey.classKey());
        this.registeredDataKeys.add(dataKey);
        this.classToKeys.computeIfAbsent(classKey, c -> new HashMap<>()).put(keyId, dataKey);
        int nextId = this.nextIdTracker.getAndIncrement();
        this.internalIds.put(dataKey, nextId);
        this.syncedIdToKey.put(nextId, dataKey);
        GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered synced data key {} for {}", dataKey.id(), classKey.id());
    }

    /**
     * 지정한 플레이어에게 동기화 데이터 키의 값을 설정한다
     *
     * @param entity 값을 지정할 플레이어
     * @param key    등록된 동기화 데이터 키
     * @param value  동기화 데이터 키 타입과 맞는 새 값
     */
    public <E extends Entity, T> void set(E entity, SyncedDataKey<?, ?> key, T value) {
        if (!this.registeredDataKeys.contains(key)) {
            String keys = this.registeredDataKeys.stream().map(k -> k.pairKey().toString()).collect(Collectors.joining(",", "[", "]"));
            GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered keys before throwing exception: {}", keys);
            throw new IllegalArgumentException(String.format("The synced data key %s for %s is not registered!", key.id(), key.classKey().id()));
        }
        DataHolder holder = this.getDataHolder(entity);
        if (holder != null && holder.set(entity, key, value)) {
            if (!entity.level().isClientSide()) {
                this.dirty = true;
                this.dirtyEntities.add(entity);
            }
        }
    }

    /**
     * 지정한 플레이어에게서 동기화 데이터 키의 값을 가져온다. 값을 가져오기 전에
     * 플레이어가 살아 있는지 확인하는 것이 좋다.
     *
     * @param entity 데이터를 가져올 플레이어
     * @param key    등록된 동기화 데이터 키
     */
    public <E extends Entity, T> T get(E entity, SyncedDataKey<E, T> key) {
        if (!this.registeredDataKeys.contains(key)) {
            String keys = this.registeredDataKeys.stream().map(k -> k.pairKey().toString()).collect(Collectors.joining(",", "[", "]"));
            GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered keys before throwing exception: {}", keys);
            throw new IllegalArgumentException(String.format("The synced data key %s for %s is not registered!", key.id(), key.classKey().id()));
        }
        DataHolder holder = this.getDataHolder(entity);
        return holder != null ? holder.get(key) : key.defaultValueSupplier().get();
    }

    public int getInternalId(SyncedDataKey<?, ?> key) {
        return this.internalIds.getInt(key);
    }

    @Nullable
    public SyncedClassKey<?> getClassKey(Identifier id) {
        return idToClassKey.get(id);
    }

    @Nullable
    public SyncedDataKey<?, ?> getKey(int id) {
        return this.syncedIdToKey.get(id);
    }

    @Nullable
    public SyncedDataKey<?, ?> getKey(SyncedClassKey<?> classKey, Identifier dataKey) {
        Map<Identifier, SyncedDataKey<?, ?>> keys = SyncedEntityData.instance().classToKeys.get(classKey);
        if (keys == null) {
            return null;
        }
        return keys.get(dataKey);
    }

    public Set<SyncedDataKey<?, ?>> getKeys() {
        return ImmutableSet.copyOf(this.registeredDataKeys);
    }

    @Nullable
    public DataHolder getDataHolder(Entity entity) {
        if (!this.hasSyncedDataKey(entity.getClass())) {
            return null;
        }
        // 지연 생성으로 예전 수명 주기 구멍을 막는다. 예전에는 모든 호출자가 maybeGet()을 써서
        // provider가 한 번도 생기지 않았고, set() 호출이 모두 값을 조용히 버렸다.
        return DataHolderCapabilityProvider.get(entity).getDataHolder().orElse(null);
    }

    public boolean hasSyncedDataKey(Class<? extends Entity> entityClass) {
        /* 엔티티 자체에는 키가 없지만 그 상위 클래스나 하위 클래스에 동기화 데이터 키가
         * 있을 수 있다. capability를 붙일 때마다 이를 확인하지 않도록 한 번만
         * 간단히 확인한 뒤 결과를 캐시한다. */
        return this.getClassNameCapabilityCache(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
                .computeIfAbsent(entityClass.getName(), c ->
                {
                    Class<?> targetClass = entityClass;
                    while (!targetClass.isAssignableFrom(Entity.class)) // 이 정도면 충분하다
                    {
                        if (this.classNameToClassKey.containsKey(targetClass.getName())) {
                            return true;
                        }
                        targetClass = targetClass.getSuperclass();
                    }
                    return false;
                });
    }

    private Map<String, Boolean> getClassNameCapabilityCache(boolean client) {
        return client ? this.clientClassNameCapabilityCache : this.serverClassNameCapabilityCache;
    }

    public boolean updateMappings(Map<Identifier, List<Pair<Identifier, Integer>>> keyMap) {
        this.syncedIdToKey.clear();

        List<Pair<Identifier, Identifier>> missingKeys = new ArrayList<>();
        keyMap.forEach((classId, list) -> {
            SyncedClassKey<?> classKey = this.idToClassKey.get(classId);
            if (classKey == null || !this.classToKeys.containsKey(classKey)) {
                list.forEach(pair -> missingKeys.add(Pair.of(classId, pair.getLeft())));
                return;
            }

            Map<Identifier, SyncedDataKey<?, ?>> keys = this.classToKeys.get(classKey);
            list.forEach(pair -> {
                SyncedDataKey<?, ?> syncedDataKey = keys.get(pair.getLeft());
                if (syncedDataKey == null) {
                    missingKeys.add(Pair.of(classId, pair.getLeft()));
                    return;
                }
                this.syncedIdToKey.put((int) pair.getRight(), syncedDataKey);
            });
        });

        if (!missingKeys.isEmpty()) {
            String keys = missingKeys.stream().map(Object::toString).collect(Collectors.joining(",", "[", "]"));
            GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Received unknown synced keys: {}", keys);
        }

        return missingKeys.isEmpty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public List<Entity> getDirtyEntities() {
        return dirtyEntities;
    }
}

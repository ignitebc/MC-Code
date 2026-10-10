package com.tacz.guns.resource.manager;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.network.DataType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Locale;
import java.util.Map;

/**
 * 서버 쪽 데이터 관리자<br>
 * 이 종류의 데이터 관리자는 서버 데이터를 로드하고 클라이언트에 동기화하는 데 쓴다
 *
 * @param <T> 데이터 종류
 */
public class CommonDataManager<T> extends JsonDataManager<T> implements INetworkCacheReloadListener {
    private final DataType type;
    protected Map<Identifier, String> networkCache;

    public final Identifier ID;

    public CommonDataManager(DataType type, Class<T> dataClass, Gson pGson, String directory, String marker) {
        super(dataClass, pGson, directory, marker);
        this.type = type;
        this.ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, marker.toLowerCase(Locale.ROOT));
    }

    public CommonDataManager(DataType type, Class<T> dataClass, Gson pGson, FileToIdConverter fileToIdConverter, String marker) {
        super(dataClass, pGson, fileToIdConverter, marker);
        this.type = type;
        this.ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, marker.toLowerCase(Locale.ROOT));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        super.apply(pObject, pResourceManager, pProfiler);

        ImmutableMap.Builder<Identifier, String> builder = ImmutableMap.builder();
        pObject.forEach((id, element) -> builder.put(id, element.toString()));
        this.networkCache = builder.build();
    }

    public void clear() {
        this.dataMap.clear();
    }

    public Map<Identifier, String> getNetworkCache() {
        return this.networkCache;
    }

    public DataType getType() {
        return this.type;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}

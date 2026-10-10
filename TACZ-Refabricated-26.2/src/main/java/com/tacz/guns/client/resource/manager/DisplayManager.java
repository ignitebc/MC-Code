package com.tacz.guns.client.resource.manager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.client.resource.pojo.display.IDisplay;
import com.tacz.guns.resource.manager.JsonDataManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * 범용 데이터 관리자<br>
 * 리소스 팩/데이터 팩에서 json 파일을 읽어 데이터로 해석한다
 *
 * @param <T> 데이터 종류
 */
public class DisplayManager<T extends IDisplay> extends JsonDataManager<T> {

    public DisplayManager(Class<T> dataClass, Gson pGson, String directory, String marker) {
        super(dataClass, pGson, FileToIdConverter.json(directory), marker);
    }

    public DisplayManager(Class<T> dataClass, Gson pGson, FileToIdConverter fileToIdConverter, String marker) {
        super(dataClass, pGson, fileToIdConverter, marker);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        dataMap.clear();
        for (Map.Entry<Identifier, JsonElement> entry : pObject.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement element = entry.getValue();
            try {
                T data = getGson().fromJson(element, getDataClass());
                if (data != null) {
                    data.init();
                    dataMap.put(id, data);
                }
            } catch (JsonParseException | IllegalArgumentException e) {
                GunMod.LOGGER.error(getMarker(), "Failed to load data file {}", id, e);
            }
        }
    }

}

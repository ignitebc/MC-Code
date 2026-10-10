package com.tacz.guns.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.tacz.guns.GunMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class ResourceScanner {
    /**
     * 지정한 디렉터리 아래 모든 json 파일을 훑는다<br>
     * 바닐라 scanDirectory 메서드와 다른 점은 조회 결과를 반환값으로 돌려주고 주석을 허용한다는 것이다
     * 같은 파일 경로이면 우선순위가 가장 높은 파일만 읽는다
     *
     * @param pResourceManager 자원 관리자
     * @param pName            디렉터리 이름
     * @param pGson            Gson 인스턴스
     * @return 찾은 json 파일
     */
    public static Map<Identifier, JsonElement> scanDirectory(ResourceManager pResourceManager, String pName, Gson pGson) {
        return scanDirectory(pResourceManager, FileToIdConverter.json(pName), pGson);
    }

    public static Map<Identifier, JsonElement> scanDirectory(ResourceManager pResourceManager, FileToIdConverter filetoidconverter, Gson pGson) {
        Map<Identifier, JsonElement> output = Maps.newHashMap();
        for (Map.Entry<Identifier, Resource> entry : filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
            Identifier resourcelocation = entry.getKey();
            Identifier resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement jsonelement = parseLenient(pGson, reader);
                JsonElement jsonelement1 = output.put(resourcelocation1, jsonelement);
                if (jsonelement1 != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + resourcelocation1);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                GunMod.LOGGER.error("Couldn't parse data file {} from {}", resourcelocation1, resourcelocation, jsonparseexception);
            }
        }
        return output;
    }

    public static Map<Identifier, Identifier> scanDirectoryResources(ResourceManager pResourceManager, FileToIdConverter filetoidconverter) {
        Map<Identifier, Identifier> output = Maps.newHashMap();
        for (Map.Entry<Identifier, Resource> entry : filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
            Identifier rawLocation = entry.getKey();
            Identifier id = filetoidconverter.fileToId(rawLocation);
            Identifier old = output.put(id, rawLocation);
            if (old != null) {
                throw new IllegalStateException("Duplicate data file ignored with ID " + id);
            }
        }
        return output;
    }

    /**
     * 지정한 디렉터리 아래 모든 json 파일을 훑는다<br/>
     * {@link #scanDirectory(ResourceManager, String, Gson)}와 달리 모든 json 파일을 읽어 목록으로 돌려준다
     *
     * @param pResourceManager  자원 관리자
     * @param filetoidconverter 파일 경로와 id의 매핑
     * @param pGson             Gson 인스턴스
     * @return 찾은 json 파일
     */
    public static Map<Identifier, List<JsonElement>> scanDirectoryAll(ResourceManager pResourceManager, FileToIdConverter filetoidconverter, Gson pGson) {
        Map<Identifier, List<JsonElement>> output = Maps.newHashMap();
        for (Map.Entry<Identifier, List<Resource>> entry : filetoidconverter.listMatchingResourceStacks(pResourceManager).entrySet()) {
            Identifier resourcelocation = entry.getKey();
            Identifier resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            for (Resource resource : entry.getValue()) {
                try (Reader reader = resource.openAsReader()) {
                    JsonElement jsonelement = parseLenient(pGson, reader);
                    List<JsonElement> list = output.computeIfAbsent(resourcelocation1, k -> Lists.newArrayList());
                    list.add(jsonelement);
                } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                    GunMod.LOGGER.error("Couldn't parse data file {} from {}", resourcelocation1, resourcelocation, jsonparseexception);
                }
            }
        }
        return output;
    }

    private static JsonElement parseLenient(Gson gson, Reader reader) {
        JsonReader jsonReader = gson.newJsonReader(reader);
        jsonReader.setStrictness(Strictness.LENIENT);
        return gson.fromJson(jsonReader, JsonElement.class);
    }
}

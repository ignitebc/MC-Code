package com.tacz.guns.api.resource;


import com.google.common.collect.Lists;
import com.tacz.guns.GunMod;

import java.nio.file.Paths;
import java.util.List;

/**
 * 압축을 풀어 내보내야 하는 총기 팩을 등록할 때 쓴다. 다른 애드온 모드용이다
 */
public final class ResourceManager {
    /**
     * 압축을 풀 총기 팩 경로를 모두 담는다
     */
    public static final List<ExtraEntry> EXTRA_ENTRIES = Lists.newArrayList();


    /**
     * @param modMainClass    애드온 모드의 메인 클래스
     * @param extraFolderPath 압축을 풀 폴더. 예를 들어 TACZ 자신은 /assets/tacz/custom/tacz_default_gun 이다 <br>
     *                        tacz_default_gun 폴더를 풀어 총기 팩 설치 폴더에 둔다는 뜻이다
     * @deprecated 예전 파일 입구는 더 이상 쓰지 않는다. 이제 assets와 data를 모드에 바로 넣거나 아래의 새 방법으로 내보낸다
     */
    @Deprecated
    public static void registerExtraGunPack(Class<?> modMainClass, String extraFolderPath) {
        GunMod.LOGGER.warn("some mod is using deprecated method to export gun pack, notifying the mod author to update it: {}", extraFolderPath);
    }

    public static void registerExportResource(Class<?> modMainClass, String extraFolderPath) {
        EXTRA_ENTRIES.add(new ExtraEntry(modMainClass, extraFolderPath, Paths.get(extraFolderPath).getFileName().toString()));
    }

    /**
     * 압축 해제 항목
     */
    public record ExtraEntry(Class<?> modMainClass, String srcPath, String extraDirName) {
    }
}

package com.tacz.guns.util;

import com.tacz.guns.GunMod;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Map;

/** PUBG 총 다섯 정의 이름이 바뀌기 전에 저장된 식별자를 해석한다. */
public final class GunIdAliases {
    // 예전 이름은 여기에만 둔다: 현재 자원과 새로 지정하는 ID는 새 이름을 쓴다.
    private static final Map<String, String> LEGACY_PATHS = Map.of(
            "hk416d", "m416",
            "glock_17", "p18c",
            "db_short", "sawed_off",
            "hk_mp5a5", "mp5k",
            "uzi", "micro_uzi"
    );

    private GunIdAliases() {
    }

    @Nullable
    public static Identifier canonicalGunId(@Nullable Identifier gunId) {
        if (gunId == null || !GunMod.MOD_ID.equals(gunId.getNamespace())) {
            return gunId;
        }
        String canonicalPath = LEGACY_PATHS.get(gunId.getPath());
        return canonicalPath == null ? gunId : Identifier.fromNamespaceAndPath(GunMod.MOD_ID, canonicalPath);
    }

    @Nullable
    public static Identifier canonicalDisplayId(@Nullable Identifier displayId) {
        if (displayId == null || !GunMod.MOD_ID.equals(displayId.getNamespace())) {
            return displayId;
        }
        String suffix = "_display";
        String path = displayId.getPath();
        if (!path.endsWith(suffix)) {
            return displayId;
        }
        String canonicalPath = LEGACY_PATHS.get(path.substring(0, path.length() - suffix.length()));
        return canonicalPath == null ? displayId : Identifier.fromNamespaceAndPath(GunMod.MOD_ID, canonicalPath + suffix);
    }
}

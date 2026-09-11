package com.tacz.guns.util;

import com.tacz.guns.GunMod;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Map;

/** Resolves saved identifiers from before the five PUBG gun renames. */
public final class GunIdAliases {
    // Legacy names belong only here: current resources and newly set IDs use the new names.
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

package com.tacz.guns.entity.ai;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.util.GunIdAliases;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * 몬스터가 든 총이 저격 전투를 하는 총인지 가른다.
 * <p>
 * 총기 분류가 sniper인 저격총과, 분류는 rifle이지만 저격 성격인 지정사수소총이 대상이다.
 * 두 종류는 조준과 연사 박자만 다르고 이동 방식(멈춰 서서 한 발씩)은 같다.
 */
public final class SniperGuns {
    public enum Kind {
        /** 저격 전투를 하지 않는 총 */
        NONE,
        /** 반자동 지정사수소총. 저격총보다 조금 빠르게 쏜다. */
        MARKSMAN,
        /** 볼트액션 등 분류가 sniper인 저격총 */
        SNIPER
    }

    private static final String SNIPER_TYPE = "sniper";
    /** rifle 분류 가운데 지정사수소총. 사냥꾼 화력 증강에서 돌격소총과 구분할 때와 같은 목록이다. */
    private static final Set<String> MARKSMAN_RIFLES = Set.of(
            "dragunov", "mk14", "sks_tactical", "slr", "vss", "spr15hb", "mini14", "mk12");

    private SniperGuns() {
    }

    public static Kind kindOf(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return Kind.NONE;
        }
        // 이름이 바뀌기 전에 저장된 총도 지금 ID로 판정해야 같은 총으로 본다.
        Identifier gunId = GunIdAliases.canonicalGunId(gun.getGunId(stack));
        String gunType = TimelessAPI.getCommonGunIndex(gunId).map(CommonGunIndex::getType).orElse(null);
        return kindOf(gunId, gunType);
    }

    public static Kind kindOf(Identifier gunId, @Nullable String gunType) {
        if (SNIPER_TYPE.equals(gunType)) {
            return Kind.SNIPER;
        }
        boolean defaultPackGun = GunMod.MOD_ID.equals(gunId.getNamespace());
        if (defaultPackGun && MARKSMAN_RIFLES.contains(gunId.getPath())) {
            return Kind.MARKSMAN;
        }
        return Kind.NONE;
    }

    /** 저격 전투(멈춰 서서 한 발씩)를 하는 총인지 */
    public static boolean isSniperClass(ItemStack stack) {
        return kindOf(stack) != Kind.NONE;
    }
}

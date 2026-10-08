package com.tacz.guns.util;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 사수의 능력으로 늘어나는 탄창 장탄 수.
 *
 * <p>TACZ는 직업 모드를 모르므로 보너스는 0을 돌려주는 빈 메서드로 두고, 직업 모드가 선택적 Mixin으로 값을 바꾼다.
 * 직업 모드가 없으면 원래 장탄 수 그대로다.
 *
 * <p>재장전 가능 여부는 서버와 클라이언트가 각각 판정하므로, 사수가 있는 장탄 계산(재장전·탄약 표시·툴팁)은 모두
 * {@link #maxAmmoCount}를 거쳐 같은 값을 쓴다. 몬스터 총기는 기본 장탄을 직접 쓰므로 영향이 없다.
 */
public final class ShooterMagazineBonus {
    private ShooterMagazineBonus() {
    }

    /**
     * 부착물까지 반영한 최대 장탄 수에 사수 보너스를 더한다.
     *
     * @param shooter 총을 든 생물. 알 수 없으면 null이며 보너스 없이 계산한다.
     */
    public static int maxAmmoCount(@Nullable LivingEntity shooter, ItemStack gunItem, GunData gunData) {
        int baseAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(gunItem, gunData);
        return baseAmmoCount + bonusFor(shooter, gunItem);
    }

    public static int maxAmmoCount(@Nullable LivingEntity shooter, ItemStack gunItem, CommonGunIndex gunIndex) {
        return maxAmmoCount(shooter, gunItem, gunIndex.getGunData());
    }

    /**
     * 사수 능력으로 더해지는 장탄 수. 직업 모드가 Mixin으로 덮어쓰며 기본은 0이다.
     *
     * @param gunType 총기 분류(pistol, rifle, sniper 등). 총기 인덱스에 적힌 값 그대로다.
     */
    public static int extraRounds(LivingEntity shooter, Identifier gunId, String gunType) {
        return 0;
    }

    private static int bonusFor(@Nullable LivingEntity shooter, ItemStack gunItem) {
        if (shooter == null) {
            return 0;
        }
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return 0;
        }
        // 이름이 바뀌기 전에 저장된 총(예: hk416d)도 지금 ID(m416)로 넘겨야 같은 총으로 판정된다.
        Identifier gunId = GunIdAliases.canonicalGunId(iGun.getGunId(gunItem));
        String gunType = TimelessAPI.getCommonGunIndex(gunId).map(CommonGunIndex::getType).orElse(null);
        if (gunType == null) {
            return 0;
        }
        return Math.max(0, extraRounds(shooter, gunId, gunType));
    }
}

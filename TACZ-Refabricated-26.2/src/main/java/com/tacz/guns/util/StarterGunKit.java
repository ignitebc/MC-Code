package com.tacz.guns.util;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 시작 장비로 주는 권총 1정과 그 총의 탄약 3탄창.
 *
 * <p>총기는 작업대에서 만든 것과 같게 빈 탄창으로 준다. 탄약은 그 총의 기본 장탄수의 3배를 주며,
 * 탄약 한 묶음의 최대 개수보다 많으면 여러 묶음으로 나눈다.
 */
public final class StarterGunKit {
    /** 시작 장비로 줄 총기 분류. 크리에이티브 권총 탭과 같은 기준으로 고른다. */
    private static final String STARTER_GUN_TYPE = GunTabType.PISTOL.name().toLowerCase(Locale.US);
    /** 시작 장비로 줄 탄창 수. 탄약은 기본 장탄수에 이 값을 곱한 만큼 준다. */
    private static final int STARTER_MAGAZINE_COUNT = 3;

    private StarterGunKit() {
    }

    /**
     * 등록된 권총 중 하나를 같은 확률로 골라 총기와 탄약을 만든다.
     *
     * @return 첫 번째가 총기이고 나머지는 탄약이다. 줄 수 있는 권총이 없으면 빈 목록
     */
    public static List<ItemStack> create(RandomSource random) {
        List<Map.Entry<Identifier, CommonGunIndex>> pistols = starterCandidates();
        if (pistols.isEmpty()) {
            return List.of();
        }

        Map.Entry<Identifier, CommonGunIndex> chosen = pistols.get(random.nextInt(pistols.size()));
        GunData gunData = chosen.getValue().getGunData();
        ItemStack gun = GunItemBuilder.create()
                .setId(chosen.getKey())
                .setAmmoCount(0)
                .setAmmoInBarrel(false)
                .setFireMode(gunData.getFireModeSet().getFirst())
                .build();
        if (gun.isEmpty()) {
            return List.of();
        }

        List<ItemStack> kit = new ArrayList<>();
        kit.add(gun);
        kit.addAll(createMagazines(gunData));
        return kit;
    }

    /**
     * 채팅 안내에 쓸 총기 이름. 아이템의 이름 메서드는 클라이언트 전용이라 서버에서는 공통 이름이 나오므로
     * 공통 인덱스의 번역 키를 직접 쓴다.
     *
     * @return 총기 이름. 총기가 아니거나 로드되지 않은 총기면 빈 값
     */
    public static Optional<Component> displayName(ItemStack gun) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return Optional.empty();
        }
        return TimelessAPI.getCommonGunIndex(iGun.getGunId(gun))
                .map(index -> Component.translatable(index.getPojo().getName()));
    }

    /** 등록된 총기 중 권총만. ID 순을 유지하므로 같은 난수가 언제나 같은 권총을 고른다. */
    private static List<Map.Entry<Identifier, CommonGunIndex>> starterCandidates() {
        return RegisteredGuns.sortedById().stream()
                .filter(entry -> STARTER_GUN_TYPE.equals(entry.getValue().getType()))
                .toList();
    }

    /** 총의 기본 장탄수로 {@link #STARTER_MAGAZINE_COUNT}탄창을 채울 탄약. 탄약이 정의되지 않은 총이면 빈 목록 */
    private static List<ItemStack> createMagazines(GunData gunData) {
        Identifier ammoId = gunData.getAmmoId();
        int remaining = gunData.getAmmoAmount() * STARTER_MAGAZINE_COUNT;
        boolean hasAmmo = ammoId != null
                && !DefaultAssets.EMPTY_AMMO_ID.equals(ammoId)
                && TimelessAPI.getCommonAmmoIndex(ammoId).isPresent();
        if (!hasAmmo || remaining <= 0) {
            return List.of();
        }

        List<ItemStack> magazine = new ArrayList<>();
        while (remaining > 0) {
            ItemStack ammo = AmmoItemBuilder.create().setId(ammoId).build();
            // 탄약 ID를 넣어야 그 탄종의 묶음 최대 개수가 정해지므로 먼저 만든 뒤 개수를 맞춘다.
            int count = Math.min(remaining, ammo.getMaxStackSize());
            ammo.setCount(count);
            magazine.add(ammo);
            remaining -= count;
        }
        return magazine;
    }
}

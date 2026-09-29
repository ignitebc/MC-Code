package com.tacz.guns.util;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** 아이템으로 만들 수 있는 총기 목록. 몬스터 장비 추첨과 시작 장비가 같은 후보를 쓴다. */
public final class RegisteredGuns {
    private RegisteredGuns() {
    }

    /**
     * 로드된 총기 중 아이템 종류가 등록된 것을 ID 순으로 돌려준다.
     *
     * <p>ID 순으로 정렬해야 같은 난수가 언제나 같은 총기를 고른다.
     */
    public static List<Map.Entry<Identifier, CommonGunIndex>> sortedById() {
        return TimelessAPI.getAllCommonGunIndex().stream()
                .filter(entry -> GunItemManager.getGunItemRegistryObject(entry.getValue().getPojo().getItemType()) != null)
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .toList();
    }
}

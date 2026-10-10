package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.util.ItemNbtUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public interface AmmoItemDataAccessor extends IAmmo {
    String AMMO_ID_TAG = "AmmoId";

    @Override
    @Nonnull
    default Identifier getAmmoId(ItemStack ammo) {
        CompoundTag nbt = ItemNbtUtils.getTag(ammo);
        if (nbt.contains(AMMO_ID_TAG)) {
            Identifier gunId = Identifier.tryParse(nbt.getStringOr(AMMO_ID_TAG, ""));
            return Objects.requireNonNullElse(gunId, DefaultAssets.EMPTY_AMMO_ID);
        }
        return DefaultAssets.EMPTY_AMMO_ID;
    }

    @Override
    default void setAmmoId(ItemStack ammo, @Nullable Identifier ammoId) {
        ItemNbtUtils.updateTag(ammo, nbt -> {
            if (ammoId != null) {
                nbt.putString(AMMO_ID_TAG, ammoId.toString());
            } else {
                nbt.putString(AMMO_ID_TAG, DefaultAssets.DEFAULT_AMMO_ID.toString());
            }
        });
        applyMaxStackSize(ammo);
    }

    /**
     * 총기 팩 데이터에 따라 {@code minecraft:max_stack_size} 컴포넌트를 써서 "탄이 겹쳐지지 않는" 문제를 고친다.
     *
     * <h2>문제의 원인</h2>
     * {@code AmmoItem}의 생성자는 {@code super(properties.stacksTo(1))}다 — 원본과 같다.
     * 실제 최대 묶음 수가 <b>탄약마다 다르기</b> 때문이다(총기 팩 {@code CommonAmmoIndex#getStackSize}에서 온다).
     * 아이템 등록 시점에 고정할 수 없다.
     *
     * <p>원본은 {@code Item#verifyComponentsAfterLoad(ItemStack)}를 재정의해 아이템을 불러온 뒤
     * {@code DataComponents.MAX_STACK_SIZE}를 썼다. 하지만 <b>26.2의 {@code Item}에는 그 메서드가 없다</b>
     * (바이트코드 확인). 그래서 이식할 때 사용자 정의 {@code IItem#tacz$getMaxStackSize} +
     * {@code ItemStackMixin}으로 {@code ItemStack#getMaxStackSize}의 반환값을 바꾸도록 했다.
     *
     * <p><b>하지만 그 경로는 죽어 있었고, 서로 독립적인 실패가 두 곳 있었다:</b>
     * <ol>
     *   <li>{@code ItemStackMixin}이 어떤 {@code *.mixins.json}에도 <b>등록된 적이 없다</b>
     *       (저장소 전체 grep 결과 0건) → 아예 로드되지 않는다.</li>
     *   <li>등록하더라도 대상인 {@code ItemStack#getMaxStackSize}가 26.2에는 <b>없다</b>
     *       (바이트코드 확인 결과 {@code ItemStack}에는 {@code getCount/setCount/limitSize/copyWithCount}만 있다).
     *       등록하면 오히려 대상을 찾지 못해 <b>크래시</b>가 난다.</li>
     * </ol>
     * 두 가지가 겹쳐 모든 탄약이 계속 {@code stacksTo(1)}에 머물렀다.
     *
     * <h2>이번 수정</h2>
     * 26.2에서는 {@code DataComponents.MAX_STACK_SIZE} 컴포넌트가 최대 묶음 수를 정한다(컴포넌트 존재 확인).
     * 여기서 <b>탄약 ID를 쓸 때 함께</b> 이 컴포넌트를 쓴다 — {@code setAmmoId}는 모든 탄약 아이템이
     * 정체성을 얻는 유일한 입구다({@code AmmoItemBuilder#build}, 재장전, 제작, 크리에이티브 탭이 모두 여기를 거친다).
     * 그래서 적용 범위가 원본의 {@code verifyComponentsAfterLoad}와 같고 mixin도 필요 없다.
     */
    static void applyMaxStackSize(ItemStack ammo) {
        if (!(ammo.getItem() instanceof IAmmo iAmmo)) {
            return;
        }
        // [34차] 상한을 [1, 99]로 묶어야 한다.
        //
        // 26.2의 max_stack_size 컴포넌트는 ExtraCodecs.intRange(1, 99)이며(바닐라
        // Item.ABSOLUTE_MAX_STACK_SIZE와 같다), 총기 팩에 99를 넘는 stack_size를 적었을 때
        // 그대로 넣으면 <b>직렬화·네트워크 동기화</b> 때 codec이 거부해
        // 아이템 이상이나 접속 끊김으로 나타난다. 그래서 먼저 범위를 묶는다. 덜 겹쳐지는 편이 크래시보다 낫다.
        TimelessAPI.getCommonAmmoIndex(iAmmo.getAmmoId(ammo))
                .map(index -> Math.clamp(index.getStackSize(), 1, 99))
                .ifPresent(size -> ammo.set(DataComponents.MAX_STACK_SIZE, size));
    }

    @Override
    default boolean isAmmoOfGun(ItemStack gun, ItemStack ammo) {
        if (gun.getItem() instanceof IGun iGun && ammo.getItem() instanceof IAmmo iAmmo) {
            Identifier gunId = iGun.getGunId(gun);
            Identifier ammoId = iAmmo.getAmmoId(ammo);
            return TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> gunIndex.getGunData().getAmmoId().equals(ammoId)).orElse(false);
        }
        return false;
    }
}

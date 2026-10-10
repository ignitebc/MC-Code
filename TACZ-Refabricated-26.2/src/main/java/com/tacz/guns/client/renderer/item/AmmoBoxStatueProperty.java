package com.tacz.guns.client.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 탄약 상자 외형 변형의 {@code select} 속성. 원본 1.21.1의
 * {@code ItemProperties.register(AmmoBoxItem.PROPERTY_NAME, AmmoBoxItem::getStatue)}에 해당한다.
 *
 * <h2>바닐라 {@code minecraft:component}를 쓰지 않고 직접 만들어야 하는 이유</h2>
 * 26.2 내장 {@code select} 속성 중 아이템 데이터를 읽을 수 있는 것은
 * {@code minecraft:component}뿐이며, 그 구현은 한 줄이다:
 * <pre>
 * return stack.get(this.componentType);   // ComponentContents#get
 * </pre>
 * 즉 컴포넌트 하나를 <b>통째로</b> 꺼내 {@code when}의 리터럴과 비교할 수만 있다.
 * 그런데 탄약 상자 상태(등급 / 열림)는
 * {@code DataComponents.CUSTOM_DATA} 컴포넌트 하나 <b>안의 NBT 필드 몇 개</b>에 들어 있고
 * ({@code AmmoBoxItemDataAccessor} 참고: {@code Level} / {@code AmmoId} /
 * {@code AmmoCount}),
 * 최종 외형도 이 필드들을 <b>조합 계산</b>해야 나온다.
 * 바닐라 속성 중 "컴포넌트 안 필드를 읽어 계산하는" 것을 표현할 수 있는 것이 없으므로 직접 만들어야 한다.
 *
 * <h2>값의 의미는 원본과 비트 단위로 맞춘다</h2>
 * {@code AmmoBoxItem#getStatue}의 계산법을 그대로 옮겼다(원본은 float를 돌려주지만 여기서는 int를 돌려준다.
 * {@code select}는 값을 정확히 비교하므로 정수가 더 안전하다):
 * <pre>
 * 일반                              -> 2 * 등급 + 열림(0/1)
 * 열림: ammoId가 비었거나 수량 &lt;= 0이면 0(open), 아니면 1(close)
 * </pre>
 * 그래서 0..5가 {@code models/item/ammo_box/}에 있는 변형 모델 6개와 딱 맞고,
 * 예전 {@code overrides} 형식의 {@code tacz:ammo_statue} 조건과 하나씩 대응한다.
 *
 * <h2>더 이상 사용자 정의 렌더러를 쓰지 않는 이유</h2>
 * 예전 {@code AmmoBoxItemRenderer}는 128×128 <b>3D 모델 UV 전개도</b>
 * ({@code textures/item/ammo_box.png})를 평면 아이콘처럼 {@code SlotModel}의
 * 16×16 사각형에 붙였다. 그 그림은 왼쪽 위 69×69 픽셀만 불투명하고 여섯 면 전개도 조각이라,
 * 16×16으로 늘이면 아이콘도 모델도 아닌 엉망인 색 덩어리가 인벤토리와 손에 보였다.
 * 원본에는 이 렌더러가 <b>처음부터 없었다</b> — 여기서는 일반 탄약 상자 JSON 모델 6개를 바닐라 렌더링에 맡긴다.
 * 이 클래스는 26.2에서 빠진 고리(속성 등록)를 채워 탄약 상자를 바닐라 렌더링 경로로 돌려보낸다.
 */
public record AmmoBoxStatueProperty() implements SelectItemModelProperty<Integer> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ammo_statue");

    public static final MapCodec<AmmoBoxStatueProperty> MAP_CODEC =
            MapCodec.unit(new AmmoBoxStatueProperty());

    public static final SelectItemModelProperty.Type<AmmoBoxStatueProperty, Integer> TYPE =
            SelectItemModelProperty.Type.create(MAP_CODEC, Codec.INT);

    /** 뚜껑 열림(탄약 없음). */
    private static final int OPEN = 0;
    /** 뚜껑 닫힘(탄약 있음). */
    private static final int CLOSE = 1;

    @Override
    @Nullable
    public Integer get(ItemStack stack,
                       @Nullable ClientLevel level,
                       @Nullable LivingEntity entity,
                       int seed,
                       ItemDisplayContext displayContext) {
        if (!(stack.getItem() instanceof IAmmoBox iAmmoBox)) {
            return null;
        }
        int openStatue = getOpenStatue(stack, iAmmoBox);
        return openStatue + 2 * iAmmoBox.getAmmoLevel(stack);
    }

    private static int getOpenStatue(ItemStack stack, IAmmoBox iAmmoBox) {
        boolean idIsEmpty = iAmmoBox.getAmmoId(stack).equals(DefaultAssets.EMPTY_AMMO_ID);
        boolean countIsZero = iAmmoBox.getAmmoCount(stack) <= 0;
        return (idIsEmpty || countIsZero) ? OPEN : CLOSE;
    }

    @Override
    public Codec<Integer> valueCodec() {
        return Codec.INT;
    }

    @Override
    public SelectItemModelProperty.Type<AmmoBoxStatueProperty, Integer> type() {
        return TYPE;
    }
}

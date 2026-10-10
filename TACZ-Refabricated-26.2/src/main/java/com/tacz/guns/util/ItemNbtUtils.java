package com.tacz.guns.util;

import com.mojang.serialization.DataResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/**
 * MC 26.2+에서 ItemStack 사용자 정의 데이터에 접근하는 도구 클래스.
 * 제거된 getOrCreateTag()/getTag()/hasTag() 메서드를 대신한다.
 */
public final class ItemNbtUtils {
    private static RegistryOps<Tag> nbtOps;

    private ItemNbtUtils() {
    }

    private static RegistryOps<Tag> getOps() {
        if (nbtOps == null) {
            RegistryAccess access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
            nbtOps = RegistryOps.create(NbtOps.INSTANCE, access);
        }
        return nbtOps;
    }

    /**
     * 아이템의 사용자 정의 데이터 tag 사본을 가져온다. 없으면 빈 CompoundTag를 돌려준다.
     */
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && !data.isEmpty()) {
            return data.copyTag();
        }
        return new CompoundTag();
    }

    /**
     * 아이템의 사용자 정의 데이터 tag를 그 자리에서 갱신한다.
     */
    public static void updateTag(ItemStack stack, Consumer<CompoundTag> consumer) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, consumer);
    }

    /**
     * Codec으로 ItemStack을 CompoundTag로 직렬화한다.
     *
     * <p><b>15차 수정</b>: {@code ItemStack.CODEC}이 아니라 반드시 {@link ItemStack#OPTIONAL_CODEC}을 써야 한다.
     * 26.2의 {@code ItemStack.MAP_CODEC}에서 count 필드는
     * {@code ExtraCodecs.optionalAlwaysPresentFieldOf(ExtraCodecs.intRange(1, 99), "count", 1)}이고,
     * {@code ItemStack.EMPTY}의 count는 0이라 <b>[1,99] 범위를 벗어나</b>
     * {@code CODEC.encodeStart(ops, ItemStack.EMPTY)}가 바로 실패한다:
     * <pre>Value must be within range [1;99]: 0</pre>
     * 그리고 {@code getOrThrow()}를 거쳐 IllegalStateException을 던진다.
     *
     * <p>이것이 바로 "부착물을 떼어 내면 부착물이 복제됨"의 근본 원인이다: {@code ClientMessageUnloadAttachment#handle}이 먼저
     * {@code inventory.add(attachmentItem)}로 부착물을 플레이어에게 주고, 이어서
     * {@code unloadAttachment} → {@code saveItemStack(ItemStack.EMPTY)}가 예외를 던져
     * 총의 부착물 NBT가 <b>지워지지 않았다</b> — 아이템은 손에 들어오고 부착물은 남음 = 무한 복제.
     *
     * <p>{@code OPTIONAL_CODEC}은 EMPTY를 {@code {}}로 인코딩하고 다시 읽으면 {@code ItemStack.EMPTY}를 얻어
     * 양방향 모두 올바르다(실측 검증함).
     */
    public static CompoundTag saveItemStack(ItemStack stack) {
        DataResult<Tag> result = ItemStack.OPTIONAL_CODEC.encodeStart(getOps(), stack);
        return (CompoundTag) result.getOrThrow();
    }

    /**
     * Codec으로 CompoundTag에서 ItemStack을 역직렬화한다.
     *
     * <p>{@link #saveItemStack}과 짝을 맞춰 {@code OPTIONAL_CODEC}을 쓴다.
     * 그러면 빈 태그 {@code {}}를 {@link ItemStack#EMPTY}로 올바르게 다시 읽는다.
     */
    public static ItemStack loadItemStack(CompoundTag tag) {
        DataResult<ItemStack> result = ItemStack.OPTIONAL_CODEC.parse(getOps(), tag);
        return result.result().orElse(ItemStack.EMPTY);
    }
}

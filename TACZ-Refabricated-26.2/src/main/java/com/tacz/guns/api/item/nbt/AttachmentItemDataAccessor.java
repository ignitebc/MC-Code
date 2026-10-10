package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.util.ItemNbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public interface AttachmentItemDataAccessor extends IAttachment {
    String ATTACHMENT_ID_TAG = "AttachmentId";
    String SKIN_ID_TAG = "Skin";
    String ZOOM_NUMBER_TAG = "ZoomNumber";
    String LASER_COLOR_TAG = "LaserColor";

    // 주어진 CompoundTag에 부착물 ID가 있는지만 확인하고, 그 부착물이 실제로 있는지는 검사하지 않는다
    static boolean isAttachmentLike(CompoundTag tag) {
        return tag.contains(ATTACHMENT_ID_TAG);
    }

    @Nonnull
    static Identifier getAttachmentIdFromTag(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return DefaultAssets.EMPTY_ATTACHMENT_ID;
        }
        if (isAttachmentLike(nbt)) {
            Identifier attachmentId = Identifier.tryParse(nbt.getStringOr(ATTACHMENT_ID_TAG, ""));
            return Objects.requireNonNullElse(attachmentId, DefaultAssets.EMPTY_ATTACHMENT_ID);
        }
        return DefaultAssets.EMPTY_ATTACHMENT_ID;
    }

    static int getZoomNumberFromTag(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return 0;
        }
        if (nbt.contains(ZOOM_NUMBER_TAG)) {
            return nbt.getIntOr(ZOOM_NUMBER_TAG, 0);
        }
        return 0;
    }

    static void setZoomNumberToTag(CompoundTag nbt, int zoomNumber) {
        nbt.putInt(ZOOM_NUMBER_TAG, zoomNumber);
    }

    @Override
    @Nonnull
    default Identifier getAttachmentId(ItemStack attachmentStack) {
        CompoundTag nbt = ItemNbtUtils.getTag(attachmentStack);
        return getAttachmentIdFromTag(nbt);
    }

    @Override
    default void setAttachmentId(ItemStack attachmentStack, @Nullable Identifier attachmentId) {
        ItemNbtUtils.updateTag(attachmentStack, nbt -> {
            if (attachmentId != null) {
                nbt.putString(ATTACHMENT_ID_TAG, attachmentId.toString());
            }
        });
    }

    @Override
    @Nullable
    default Identifier getSkinId(ItemStack attachmentStack) {
        CompoundTag nbt = ItemNbtUtils.getTag(attachmentStack);
        if (nbt.contains(SKIN_ID_TAG)) {
            return Identifier.tryParse(nbt.getStringOr(SKIN_ID_TAG, ""));
        }
        return null;
    }

    @Override
    default void setSkinId(ItemStack attachmentStack, @Nullable Identifier skinId) {
        ItemNbtUtils.updateTag(attachmentStack, nbt -> {
            if (skinId != null) {
                nbt.putString(SKIN_ID_TAG, skinId.toString());
            } else {
                nbt.remove(SKIN_ID_TAG);
            }
        });
    }

    @Override
    default int getZoomNumber(ItemStack attachmentStack) {
        CompoundTag nbt = ItemNbtUtils.getTag(attachmentStack);
        return getZoomNumberFromTag(nbt);
    }

    @Override
    default void setZoomNumber(ItemStack attachmentStack, int zoomNumber) {
        ItemNbtUtils.updateTag(attachmentStack, nbt -> setZoomNumberToTag(nbt, zoomNumber));
    }

    /**
     * 주어진 부착물 NBT 태그에 레이저 색을 바로 쓴다.
     *
     * <p>{@link #setLaserColor(ItemStack, int)}와의 차이: 그쪽은
     * {@code ItemStack} 객체에 쓰지만, <b>총에 장착한 부착물에는 독립된 ItemStack이 없다</b> —
     * 총 NBT 안의 데이터 조각일 뿐이고, {@code IGun#getAttachment}는 매번 Codec으로
     * 임시 사본을 역직렬화하므로 사본을 바꿔도 총에 다시 쓰이지 않는다.
     *
     * <p>그래서 서버에서 "장착한 부착물의 레이저 색 변경"을 처리할 때는 반드시
     * {@code getAttachmentTag → setLaserColorToTag → setAttachmentTag} 경로를 거쳐야 한다.
     * {@code ClientMessageLaserColor#handle} 참고. 이 메서드는 원본에 있던 같은 이름의 정적 도구로,
     * 이식할 때 빠져 색 변경이 저장되지 않았다.
     */
    static void setLaserColorToTag(CompoundTag nbt, int color) {
        nbt.putInt(LASER_COLOR_TAG, color);
    }

    @Override
    default boolean hasCustomLaserColor(ItemStack attachmentStack) {
        CompoundTag nbt = ItemNbtUtils.getTag(attachmentStack);
        return nbt.contains(LASER_COLOR_TAG);
    }

    @Override
    default int getLaserColor(ItemStack attachmentStack) {
        CompoundTag nbt = ItemNbtUtils.getTag(attachmentStack);
        if (!hasCustomLaserColor(attachmentStack)) {
            return 0xFF0000;
        }
        return nbt.getIntOr(LASER_COLOR_TAG, 0xFF0000);
    }

    @Override
    default void setLaserColor(ItemStack attachmentStack, int color) {
        ItemNbtUtils.updateTag(attachmentStack, nbt -> nbt.putInt(LASER_COLOR_TAG, color));
    }
}

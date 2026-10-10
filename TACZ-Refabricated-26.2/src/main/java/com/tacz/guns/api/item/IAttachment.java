package com.tacz.guns.api.item;

import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IAttachment {
    /**
     * @return 아이템 종류가 IAttachment면 명시적으로 형변환한 인스턴스, 아니면 null
     */
    @Nullable
    static IAttachment getIAttachmentOrNull(@Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.getItem() instanceof IAttachment iAttachment) {
            return iAttachment;
        }
        return null;
    }

    /**
     * 부착물 ID를 얻는다
     */
    @Nonnull
    Identifier getAttachmentId(ItemStack attachmentStack);

    /**
     * 부착물 ID를 설정한다
     */
    void setAttachmentId(ItemStack attachmentStack, @Nullable Identifier attachmentId);

    /**
     * @deprecated
     */
    @Deprecated
    @Nullable
    Identifier getSkinId(ItemStack attachmentStack);

    /**
     * @deprecated
     */
    @Deprecated
    void setSkinId(ItemStack attachmentStack, @Nullable Identifier skinId);

    /**
     * 조준경 부착물의 확대 배율 번호를 얻는다. 조준경 부착물만 쓸 수 있다
     */
    int getZoomNumber(ItemStack attachmentStack);

    /**
     * 조준경 부착물의 확대 배율 번호를 설정한다
     */
    void setZoomNumber(ItemStack attachmentStack, int zoomNumber);

    /**
     * 부착물 종류
     */
    @Nonnull
    AttachmentType getType(ItemStack attachmentStack);

    boolean hasCustomLaserColor(ItemStack attachmentStack);

    /**
     * 레이저 부착물의 레이저 색을 얻는다
     *
     * @return 레이저 색(RGB)
     */
    int getLaserColor(ItemStack attachmentStack);

    void setLaserColor(ItemStack attachmentStack, int color);
}

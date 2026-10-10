package com.tacz.guns.util.datafixer;

import com.google.common.collect.ImmutableMap;
import com.tacz.guns.api.DefaultAssets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import java.util.Map;

import static com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor.ATTACHMENT_ID_TAG;
import static com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor.getAttachmentIdFromTag;

public final class AttachmentIdFix {
    private AttachmentIdFix() {
    }

    public static final Map<Identifier, Identifier> OLD_TO_NEW;

    static {
        OLD_TO_NEW = ImmutableMap.<Identifier, Identifier>builder()
                .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_knight_qd"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_knight_qd"))
                .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_mirage"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_mirage"))
                .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_phantom_s1"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_phantom_s1"))
                .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_ptilopsis"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_ptilopsis"))
                .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_ursus"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_ursus"))
                .put(Identifier.fromNamespaceAndPath("tacz", "muzzle_silence_vulture"), Identifier.fromNamespaceAndPath("tacz", "muzzle_silencer_vulture"))
                .build();
    }

    // 나중에 쓰려고 boolean 반환값을 남겨 둔다. "작업이 쓸모 있는 정보를 줄 수 없는 경우가 아니면 void를 되도록 쓰지 않는다"
    public static boolean updateAttachmentIdInTag(CompoundTag tag) {
        Identifier old = getAttachmentIdFromTag(tag);
        if (!old.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            Identifier fixed = updateAttachmentId(old);
            if (!old.equals(fixed)) {
                tag.putString(ATTACHMENT_ID_TAG, fixed.toString());
                return true;
            }
        }
        return false;
    }

    public static Identifier updateAttachmentId(Identifier old) {
        return OLD_TO_NEW.getOrDefault(old, old);
    }
}

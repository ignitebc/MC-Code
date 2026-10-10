package com.tacz.guns.client.gui.components.refit;

import cn.sh1rocu.tacz.util.forge.ForgeSlider;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.util.LaserColorUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class HSVSliderGroup {
    private final Inventory inventory;
    private final int gunItemIndex;

    private final AttachmentType type;

    private final LaserColorSlider hueSlider;
    private final LaserColorSlider saturationSlider;

    public HSVSliderGroup(int x, int y, int width, int height, Inventory inventory, int gunItemIndex, @NotNull AttachmentType type) {
        this.inventory = inventory;
        this.gunItemIndex = gunItemIndex;
        this.type = type;

        int color = getColor(type);
        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);

        hueSlider = new LaserColorSlider(x, y, width, height, this, hsb[0]);
        saturationSlider = new LaserColorSlider(x, y + 2 + height, width, height, this, hsb[1]);
    }

    public LaserColorSlider getHueSlider() {
        return hueSlider;
    }

    public LaserColorSlider getSaturationSlider() {
        return saturationSlider;
    }


    public void apply() {
        // 확인이 필요한 구현
        // 여기서 클라이언트에 nbt를 쓰는 것은 사실 임시 쓰기로, 염색 효과를 실시간으로 미리 보기 위해서다
        // 알맞은 때 서버에 패킷을 보내 변경을 알려야 한다
        // 이 컴포넌트는 슬라이더를 움직이는 동안 매우 자주 호출되므로, 서버에 자주 패킷을 보내지 않으려고 여기서 바로 보내지 않는다
        ItemStack gun = inventory.getItem(gunItemIndex);
        if (gun.getItem() instanceof IGun iGun) {
            int rgb_new = Color.HSBtoRGB((float) hueSlider.getValue(), (float) saturationSlider.getValue(), 1f);

            if (type == AttachmentType.NONE) {
                iGun.setLaserColor(gun, rgb_new);
                return;
            }

            // [getAttachment()가 돌려준 ItemStack이 아니라 "총에 있는 부착물 NBT"를 고쳐야 한다]
            //
            // getAttachment(gun, type) 내부는
            //     ItemNbtUtils.loadItemStack(nbt.getCompoundOrEmpty(key))
            // 이며 — 호출할 때마다 Codec으로 [완전히 새 ItemStack을 역직렬화]해서,
            // 총에 실제로 저장된 데이터와 아무런 참조 관계가 없다.
            //
            // 예전에는 여기를 이렇게 썼다
            //     ItemStack laser = iGun.getAttachment(gun, type);
            //     iAttachment.setLaserColor(laser, rgb_new);
            // 임시 사본에 색을 쓴 셈이라 메서드가 끝나면 바로 버려졌다.
            // 그 결과 [클라이언트 로컬 데이터도 바뀌지 않아] 다음과 같았다:
            //   1. 슬라이더를 끌어도 레이저 색이 전혀 바뀌지 않았다(이 메서드는 원래 실시간 미리 보기를 위해 클라이언트 NBT에 "임시 쓰기"를 하는데,
            //      써지지 않으니 미리 보기도 움직이지 않았다).
            //   2. 화면에서 NBT를 다시 읽을 때마다(다른 버튼으로 다시 만들거나 화면을 닫을 때)
            //      표시가 기본 색으로 돌아갔다.
            //   3. 더 숨어 있던 문제는, 화면을 닫을 때 서버로 보내는 ClientMessageLaserColor가
            //      hasCustomLaserColor(attachment)를 돌며 동기화할 색을 모으는데,
            //      이 NBT가 한 번도 쓰이지 않았으니 -> colorMap이 비어 -> 서버는 아무것도 바꾸지 않았다.
            //      그래서 지난번에 서버 handle만 고친 것으로는 부족했다. 양쪽이 같은 버그였다.
            //
            // 원본 1.21.1의 작성법(줄마다 대조):
            //     CompoundTag tag = iGun.getAttachmentTag(gun, type);
            //     if (tag != null) { AttachmentItemDataAccessor.setLaserColorToTag(tag, rgb_new); }
            //     iGun.setAttachmentTag(gun, type, tag);
            // getAttachmentTag/setAttachmentTag는 총 NBT 안의
            // "부착물 ItemStack의 components.custom_data" 층을 다루므로 변경이 실제로 적용된다.
            CompoundTag tag = iGun.getAttachmentTag(gun, type);
            if (tag != null) {
                AttachmentItemDataAccessor.setLaserColorToTag(tag, rgb_new);
                iGun.setAttachmentTag(gun, type, tag);
            }
        }
    }


    private int getColor(AttachmentType type) {
        if (inventory == null) {
            return 0XFF0000;
        }
        ItemStack gun = inventory.getItem(gunItemIndex);

        if (gun.getItem() instanceof IGun iGun) {
            if (type == AttachmentType.NONE) {
                return LaserColorUtil.getLaserColor(gun);
            } else {
                ItemStack attachment = iGun.getAttachment(gun, type);
                return LaserColorUtil.getLaserColor(attachment);
            }
        }

        return 0XFF0000;
    }

    public static class LaserColorSlider extends ForgeSlider {
        private final HSVSliderGroup parent;

        public LaserColorSlider(int x, int y, int width, int height, HSVSliderGroup parent, double current) {
            super(x, y, width, height, Component.empty(), Component.empty(), 0, 1, current, 0.01, 0, true);
            this.parent = parent;
        }

        @Override
        protected void applyValue() {
            parent.apply();
        }
    }
}

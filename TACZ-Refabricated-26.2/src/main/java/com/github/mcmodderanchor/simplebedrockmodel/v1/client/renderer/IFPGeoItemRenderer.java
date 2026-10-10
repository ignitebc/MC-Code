package com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.animation.IFPAnimationInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * simplebedrockmodel IFPGeoItemRenderer의 대체 구현(26.2용 라이브러리가 아직 없음).
 */
public interface IFPGeoItemRenderer {
    long getPutAwayDuration(ItemStack stack);

    @Nullable
    IFPAnimationInstance createAnimationInstance(ItemStack stack, Entity entity);

    boolean isSameItem(ItemStack oldStack, ItemStack newStack);

    boolean blockOffhandRender();
}

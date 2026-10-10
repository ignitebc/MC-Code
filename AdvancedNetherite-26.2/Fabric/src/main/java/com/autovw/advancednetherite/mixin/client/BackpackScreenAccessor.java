package com.autovw.advancednetherite.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 여러 화면에 한 번에 붙는 Mixin은 화면 위치 필드를 직접 상속받지 못하므로 접근자로 읽는다. */
@Mixin(AbstractContainerScreen.class)
public interface BackpackScreenAccessor
{
    @Accessor("leftPos")
    int advancednetherite$getLeftPos();

    @Accessor("topPos")
    int advancednetherite$getTopPos();
}

package com.daqem.uilib.mixin;

import com.daqem.uilib.api.component.IComponent;
import com.daqem.uilib.api.component.IComponentsParent;
import com.daqem.uilib.api.widget.IWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements Renderable, GuiEventListener, LayoutElement, NarratableEntry, IWidget {

    @Shadow
    private int x;

    @Shadow
    private int y;

    private int uilib$parentX;
    private int uilib$parentY;

    @Override
    public int uilib$getParentX() {
        return this.uilib$parentX;
    }

    @Override
    public int uilib$getParentY() {
        return this.uilib$parentY;
    }

    @Override
    public void uilib$updateParentPosition(int parentX, int parentY) {
        this.uilib$parentX = parentX;
        this.uilib$parentY = parentY;

        if (this instanceof IComponentsParent componentsParent) {
            for (IComponent component : componentsParent.getComponents()) {
                component.updateParentPosition(getX(), getY(), getWidth(), getHeight());
            }
        }
    }

    /*
     * 부모 위치는 열린 화면 종류와 관계없이 더한다. UILib 화면이 아닌 곳(TACZ 총기 작업대의 총기 도감 칸)에
     * 끼운 컴포넌트도 버튼이 제자리에 그려지고 눌려야 하기 때문이다.
     * 부모를 지정받지 않은 바닐라 위젯은 부모 위치가 0이라 돌려주는 값이 바뀌지 않는다.
     */
    @Inject(method = "getX()I", at = @At("RETURN"), cancellable = true)
    private void uilib$modifyGetX(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.x + this.uilib$parentX);
    }

    @Inject(method = "getY()I", at = @At("RETURN"), cancellable = true)
    private void uilib$modifyGetY(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.y + this.uilib$parentY);
    }
}

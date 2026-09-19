package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 레시피 책을 가방 패널 폭의 절반만큼 왼쪽으로 옮긴다.
 *
 * <p>인벤토리 화면은 가방 패널 때문에 imageWidth가 176이 아니라 260이다. 바닐라는 imageWidth를
 * 빼서 화면을 가운데 맞추므로 인벤토리가 그만큼 왼쪽에서 시작한다. 그런데 레시피 책의 자리는
 * 화면 폭만 보고 정해져서 따라 움직이지 않는다. 그대로 두면 책이 인벤토리 왼쪽 두 칸을 덮는다.
 *
 * <p>책을 패널 폭의 절반만큼 왼쪽으로 옮기면 [책][인벤토리][가방 패널]이 한 덩어리로 가운데
 * 놓인다. 옆에 나란히 둘 만큼 넓지 않아 책이 전체 덮개로 바뀌는 경우에는 인벤토리와 겹칠 일이
 * 없으므로 옮기지 않는다.
 */
@Mixin(RecipeBookComponent.class)
public abstract class BackpackRecipeBookLayoutMixin
{
    @Shadow @Final protected RecipeBookMenu menu;
    @Shadow private boolean widthTooNarrow;

    /** 책 본문, 검색창, 레시피 목록, 클릭 판정이 모두 이 값을 기준으로 놓인다. */
    @Inject(method = "getXOrigin", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$shiftBook(CallbackInfoReturnable<Integer> callback)
    {
        callback.setReturnValue(callback.getReturnValue() - advancednetherite$shift());
    }

    /**
     * 탭 단추는 getXOrigin을 쓰지 않고 같은 식을 다시 계산한다. 그 식의 왼쪽 여백 30을 늘려
     * 본문과 같은 만큼 옮긴다.
     */
    @ModifyConstant(method = "updateTabs", constant = @Constant(intValue = 30))
    private int advancednetherite$shiftTabs(int tabOffset)
    {
        return tabOffset + advancednetherite$shift();
    }

    private int advancednetherite$shift()
    {
        // 플레이어 인벤토리의 2x2 제작대만 가방 패널을 달고 있다. 제작대 화면의 책은 그대로 둔다.
        if (this.widthTooNarrow || !(this.menu instanceof InventoryMenu))
        {
            return 0;
        }
        return BackpackInventory.PANEL_WIDTH / 2;
    }
}

package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.backpack.BackpackPanel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * 상자·작업대·셜커 상자·화로 계열·호퍼·발사기·공급기·양조기 화면에도 E키 화면과 같은 가방 패널을 붙인다.
 *
 * <p>서버와 클라이언트가 같은 생성자로 메뉴를 만들므로 양쪽 슬롯 수가 함께 늘어난다.
 * 한쪽만 이 버전이면 슬롯 수가 달라 동기화가 깨지므로 서버와 클라이언트를 같이 배포해야 한다.</p>
 */
@Mixin(AbstractContainerMenu.class)
public abstract class BackpackPanelMenuMixin
{
    /**
     * 정확히 이 바닐라 메뉴들만 대상으로 한다. 다른 모드가 상속한 메뉴는 화면 배치와 클릭 처리를
     * 알 수 없어 패널을 붙이지 않는다. 화면 쪽 대상은 BackpackContainerScreenMixin과 맞춘다.
     */
    @Unique
    private static final Set<Class<?>> advancednetherite$PANEL_MENUS = Set.of(
            ChestMenu.class,
            ShulkerBoxMenu.class,
            CraftingMenu.class,
            FurnaceMenu.class,
            BlastFurnaceMenu.class,
            SmokerMenu.class,
            HopperMenu.class,
            DispenserMenu.class,
            BrewingStandMenu.class);

    @Shadow
    protected abstract Slot addSlot(Slot slot);

    @Shadow
    protected abstract boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse);

    /** 대상 메뉴는 모두 플레이어 인벤토리를 마지막에 추가하므로, 그 직후에 붙이면 슬롯 목록의 맨 끝이 된다. */
    @Inject(method = "addStandardInventorySlots", at = @At("TAIL"))
    private void advancednetherite$addPanel(Container container, int left, int top, CallbackInfo ci)
    {
        boolean panelMenu = advancednetherite$PANEL_MENUS.contains(getClass());
        if (!panelMenu || !(container instanceof Inventory inventory))
        {
            return;
        }
        // 플레이어 인벤토리 첫 줄 높이가 화면마다 달라 패널도 그만큼 위아래로 옮긴다.
        int offsetY = top - BackpackPanel.ANCHOR_Y;
        for (Slot slot : BackpackPanel.createSlots(inventory, offsetY))
        {
            addSlot(slot);
        }
    }

    /**
     * 상자·셜커 상자·호퍼는 Shift 이동의 플레이어 쪽 범위를 "슬롯 끝까지"로 잡고 뒤에서부터 채운다.
     * 그대로 두면 꺼낸 물건이 핫바보다 가방 칸에 먼저 들어가므로 패널 칸을 범위에서 뺀다.
     * 일반 인벤토리가 가득 차면 BackpackTransferMixin이 남은 물건을 가방에 넣는다.
     */
    @Inject(method = "moveItemStackTo", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$skipPanel(ItemStack stack, int start, int end, boolean reverse,
                                            CallbackInfoReturnable<Boolean> cir)
    {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (!BackpackPanel.hasPanel(menu))
        {
            return;
        }
        int panelStart = BackpackPanel.firstSlotIndex(menu);
        boolean spansPanel = start < panelStart && end > panelStart;
        if (!spansPanel)
        {
            return;
        }
        cir.setReturnValue(moveItemStackTo(stack, start, panelStart, reverse));
    }
}

package com.autovw.advancednetherite.client;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import com.autovw.advancednetherite.common.backpack.BackpackPanel;
import com.autovw.advancednetherite.common.backpack.BackpackSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/** E키 화면과 상자·작업대 등 다른 화면이 똑같은 가방 패널을 그리도록 한곳에서 그린다. */
public final class BackpackPanelRenderer
{
    private BackpackPanelRenderer()
    {
    }

    public static void extract(GuiGraphicsExtractor graphics, Font font, AbstractContainerMenu menu,
                               int leftPos, int topPos)
    {
        int left = leftPos + BackpackInventory.PANEL_LEFT;
        int right = left + BackpackInventory.PANEL_WIDTH;
        int top = topPos + BackpackPanel.offsetY(menu);
        int bottom = top + BackpackPanel.PANEL_HEIGHT;
        graphics.fill(left, top, right, bottom, 0xFF373737);
        graphics.fill(left, top + 1, right - 1, bottom - 1, 0xFFC6C6C6);
        graphics.fill(left, top + 1, right - 1, top + 3, 0xFFFFFFFF);
        graphics.fill(right - 3, top + 3, right - 1, bottom - 1, 0xFF555555);
        graphics.text(font, Component.literal("가방"), left + 8, top + 8, 0xFF404040, false);

        int capacity = BackpackInventory.get(Minecraft.getInstance().player.getInventory()).capacity();
        Component capacityText = Component.literal("+" + capacity + "칸");
        // 칸 수가 두 자리가 되어도 장비 슬롯과 같은 중심에 오도록 글자 폭으로 맞춘다.
        int capacityX = left + (BackpackInventory.PANEL_WIDTH - font.width(capacityText)) / 2;
        graphics.text(font, capacityText, capacityX, top + 49, 0xFF404040, false);
        graphics.text(font, Component.literal("추가 인벤토리"), left + 8, top + 70, 0xFF404040, false);
        for (Slot slot : menu.slots)
        {
            if (!(slot instanceof BackpackSlot) || !slot.isActive()) continue;
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFFFFFFFF);
            graphics.fill(x - 1, y - 1, x + 16, y + 16, 0xFF373737);
            graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
        }
    }
}

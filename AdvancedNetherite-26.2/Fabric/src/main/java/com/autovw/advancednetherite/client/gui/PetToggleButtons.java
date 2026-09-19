package com.autovw.advancednetherite.client.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * 인벤토리 화면(E키) 아래에 펫 ON/OFF 판을 붙인다.
 *
 * <p>판 하나만 붙이고 그 안의 단추와 스크롤은 {@link PetPanelWidget}이 직접 다룬다.
 * 그래야 레시피 책을 여닫아 화면이 좌우로 밀려도 판이 따라간다.
 */
public final class PetToggleButtons
{
    private PetToggleButtons()
    {
    }

    public static void register()
    {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen))
            {
                return;
            }

            Screens.getWidgets(screen).add(new PetPanelWidget((BackpackScreenPosition) screen));
        });
    }
}

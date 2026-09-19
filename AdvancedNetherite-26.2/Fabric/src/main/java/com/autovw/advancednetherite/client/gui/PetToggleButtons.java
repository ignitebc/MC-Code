package com.autovw.advancednetherite.client.gui;

import com.autovw.advancednetherite.client.ClientPetData;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.common.pet.PetManager;
import com.autovw.advancednetherite.network.PetStatusEntry;
import com.autovw.advancednetherite.network.PetTogglePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.List;

/**
 * 인벤토리 화면(E키) 아래의 작은 판에 펫별 ON/OFF 버튼을 붙인다.
 * 버튼 목록은 서버가 동기화한 {@link ClientPetData}를 따르고,
 * 자리는 {@link PetPanelLayout}이 정한다.
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

            List<PetStatusEntry> pets = ClientPetData.getPets();
            BackpackScreenPosition position = (BackpackScreenPosition) screen;
            int left = position.advancednetherite$getLeftPos();
            int top = position.advancednetherite$getTopPos();

            for (int i = 0; i < pets.size(); i++)
            {
                PetStatusEntry entry = pets.get(i);
                int petNumber = i + 1;

                Button button = Button.builder(
                                buttonLabel(petNumber, entry.enabled()),
                                (Button pressedButton) -> {
                                    boolean newEnabled = ClientPetData.toggleLocally(entry.recordId());
                                    pressedButton.setMessage(buttonLabel(petNumber, newEnabled));
                                    ClientPlayNetworking.send(new PetTogglePayload(entry.recordId()));
                                })
                        .bounds(left + PetPanelLayout.buttonX(i), top + PetPanelLayout.buttonY(i),
                                PetPanelLayout.BUTTON_WIDTH, PetPanelLayout.BUTTON_HEIGHT)
                        .tooltip(buildPetTooltip(entry))
                        .build();
                Screens.getWidgets(screen).add(button);
            }
        });
    }

    private static Component buttonLabel(int petNumber, boolean enabled)
    {
        String state = enabled ? "ON" : "OFF";
        return Component.literal("펫" + petNumber + " " + state);
    }

    private static Tooltip buildPetTooltip(PetStatusEntry entry)
    {
        EntityType<DialgaPetEntity> petType = PetManager.getPetType(entry.petTypeId());
        if (petType == null)
        {
            return Tooltip.create(Component.literal(entry.petTypeId()));
        }
        return Tooltip.create(petType.getDescription());
    }
}

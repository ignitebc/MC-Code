package com.autovw.advancednetherite.client;

import com.autovw.advancednetherite.client.model.DarkDragonPetModel;
import com.autovw.advancednetherite.client.model.DialgaPetModel;
import com.autovw.advancednetherite.client.model.FairlinsPetModel;
import com.autovw.advancednetherite.client.model.GazellePetModel;
import com.autovw.advancednetherite.client.model.GomiPetModel;
import com.autovw.advancednetherite.client.model.KirbyPetModel;
import com.autovw.advancednetherite.client.model.SculkenRavenPetModel;
import com.autovw.advancednetherite.client.model.UnicornPetModel;
import com.autovw.advancednetherite.client.renderer.DarkDragonPetRenderer;
import com.autovw.advancednetherite.client.renderer.DialgaPetRenderer;
import com.autovw.advancednetherite.client.renderer.FairlinsPetRenderer;
import com.autovw.advancednetherite.client.renderer.GazellePetRenderer;
import com.autovw.advancednetherite.client.renderer.GomiPetRenderer;
import com.autovw.advancednetherite.client.renderer.KirbyPetRenderer;
import com.autovw.advancednetherite.client.renderer.SculkenRavenPetRenderer;
import com.autovw.advancednetherite.client.renderer.SuperGomiPetRenderer;
import com.autovw.advancednetherite.client.renderer.UnicornPetRenderer;
import com.autovw.advancednetherite.client.gui.PetToggleButtons;
import com.autovw.advancednetherite.core.ModEntityTypes;
import com.autovw.advancednetherite.network.PetListSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * @author Autovw
 */
public class ClientHandler implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ModelLayerRegistry.registerModelLayer(DialgaPetModel.LAYER_LOCATION, DialgaPetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.DIALGA_PET, DialgaPetRenderer::new);
        ModelLayerRegistry.registerModelLayer(UnicornPetModel.LAYER_LOCATION, UnicornPetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.UNICORN_PET, UnicornPetRenderer::new);
        ModelLayerRegistry.registerModelLayer(FairlinsPetModel.LAYER_LOCATION, FairlinsPetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.FAIRLINS_PET, FairlinsPetRenderer::new);
        ModelLayerRegistry.registerModelLayer(DarkDragonPetModel.LAYER_LOCATION, DarkDragonPetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.DARK_DRAGON_PET, DarkDragonPetRenderer::new);
        ModelLayerRegistry.registerModelLayer(KirbyPetModel.LAYER_LOCATION, KirbyPetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.KIRBY_PET, KirbyPetRenderer::new);
        ModelLayerRegistry.registerModelLayer(GazellePetModel.LAYER_LOCATION, GazellePetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.GAZELLE_PET, GazellePetRenderer::new);
        ModelLayerRegistry.registerModelLayer(SculkenRavenPetModel.LAYER_LOCATION, SculkenRavenPetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.SCULKEN_RAVEN_PET, SculkenRavenPetRenderer::new);
        ModelLayerRegistry.registerModelLayer(GomiPetModel.LAYER_LOCATION, GomiPetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.GOMI_PET, GomiPetRenderer::new);
        ModelLayerRegistry.registerModelLayer(GomiPetModel.SUPER_LAYER_LOCATION, GomiPetModel::createSuperBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.SUPER_GOMI_PET, SuperGomiPetRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(PetListSyncPayload.TYPE,
                (payload, context) -> ClientPetData.setPets(payload.pets()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientPetData.clear());
        PetToggleButtons.register();
    }
}

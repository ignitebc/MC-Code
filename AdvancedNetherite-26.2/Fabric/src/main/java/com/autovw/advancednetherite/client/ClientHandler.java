package com.autovw.advancednetherite.client;

import com.autovw.advancednetherite.client.model.DarkDragonPetModel;
import com.autovw.advancednetherite.client.model.DialgaPetModel;
import com.autovw.advancednetherite.client.model.FairlinsPetModel;
import com.autovw.advancednetherite.client.model.UnicornPetModel;
import com.autovw.advancednetherite.client.renderer.DarkDragonPetRenderer;
import com.autovw.advancednetherite.client.renderer.DialgaPetRenderer;
import com.autovw.advancednetherite.client.renderer.FairlinsPetRenderer;
import com.autovw.advancednetherite.client.renderer.UnicornPetRenderer;
import com.autovw.advancednetherite.core.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
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
    }
}

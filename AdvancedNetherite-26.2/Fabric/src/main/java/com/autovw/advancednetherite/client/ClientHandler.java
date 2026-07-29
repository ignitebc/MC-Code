package com.autovw.advancednetherite.client;

import com.autovw.advancednetherite.client.model.DialgaPetModel;
import com.autovw.advancednetherite.client.renderer.DialgaPetRenderer;
import com.autovw.advancednetherite.core.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * @author Autovw
 */
public class ClientHandler implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        EntityModelLayerRegistry.registerModelLayer(DialgaPetModel.LAYER_LOCATION, DialgaPetModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.DIALGA_PET, DialgaPetRenderer::new);
    }
}

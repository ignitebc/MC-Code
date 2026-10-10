package com.tacz.guns.client.renderer.feature;

import com.tacz.guns.GunMod;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;

/**
 * TACZ 26.2 사용자 정의 FeatureRenderer 등록 입구
 * <p>
 * 실제 API 출처: https://github.com/FabricMC/fabric-api/blob/26.2/fabric-rendering-v1/src/client/java/net/fabricmc/fabric/api/client/rendering/v1/FeatureRendererRegistry.java
 * <p>
 * 사용법: TACZ의 {@code ClientModInitializer.onInitializeClient}에서 {@link #register()}를 호출한다
 * <pre>{@code
 * public class TacZClientInit implements ClientModInitializer {
 *     @Override
 *     public void onInitializeClient() {
 *         TaczFeatureRenderers.register();
 *     }
 * }
 * }</pre>
 */
public final class TaczFeatureRenderers {

    private TaczFeatureRenderers() {
    }

    /**
     * TACZ 사용자 정의 FeatureRenderer를 모두 등록한다
     * <p>
     * 현재 등록:
     * <ul>
     *   <li>{@link GunModelFeatureRenderer} - 총기 Bedrock 모델 렌더링</li>
     *   <li>나중: AttachmentModelFeatureRenderer, MuzzleFlashRenderer 등</li>
     * </ul>
     */
    public static void register() {
        GunMod.LOGGER.info("[TACZ] Registering FeatureRenderers for 26.2...");

        // 총기 모델
        FeatureRendererRegistry.register(
                GunModelFeatureRenderer.TYPE,
                GunModelFeatureRenderer::new
        );

        // 나중: 부착물 모델(AttachmentModelFeatureRenderer)

        GunMod.LOGGER.info("[TACZ] FeatureRenderers registered.");
    }
}

package com.tacz.guns.client.render.scope;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.tacz.guns.GunMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

/**
 * 화면 밖 마스크 target의 텍스처를 <b>일반 등록 텍스처</b>처럼 꾸며 RenderSetup이 묶을 수 있게 한다.
 *
 * <h2>이 감싸기가 필요한 이유</h2>
 * {@code RenderSetup.RenderSetupBuilder#withTexture(String, Identifier)}는
 * {@code Identifier}만 받고, {@code RenderSetup#prepareTextures}는 안에서
 * <pre>textureManager.getTexture(binding.location())</pre>
 * 를 거친다(바이트코드 오프셋 156-159 확인) — 즉 <b>TextureManager에 등록된 텍스처만 묶을 수 있고</b>,
 * 이미 있는 {@code GpuTextureView}를 바로 넣을 오버로드는 없다.
 *
 * <p>하지만 {@code AbstractTexture}의 세 필드 {@code texture/textureView/sampler}는
 * 모두 {@code protected}라(바이트코드 확인 flags=0x4) 하위 클래스가 직접 쓸 수 있다.
 * 그래서 여기서는 "빈 껍데기 텍스처"를 만든다: GPU 자원을 직접 만들지 않고
 * 매 프레임 마스크 target의 view를 세 필드에 넣은 뒤 TextureManager에 등록한다.
 * 엔진은 평소처럼 Identifier로 찾아 우리 마스크를 얻는다.
 *
 * <h2>수명: close()가 빈 이유</h2>
 * 이 클래스는 그 텍스처를 <b>소유하지 않는다</b> — {@link ScopeMaskTarget}의 것이며 그쪽이 만들고 없앤다.
 * 여기서 close하면 남의 자원을 미리 풀어 버린다(TextureManager는 리로드 때
 * 등록 항목에 close를 호출한다). 그래서 아무것도 하지 않도록 재정의해 소유 경계를 분명히 한다.
 *
 * <p>같은 이유로 {@code sampler}도 매 프레임 새로 고친다: 창 크기가 바뀌면 {@link ScopeMaskTarget}이
 * target을 다시 만들어 예전 view가 무효가 되므로 새것을 다시 가리켜야 한다.
 */
@Environment(EnvType.CLIENT)
public final class ScopeMaskTextureHandle extends AbstractTexture {

    /** TextureManager 안 마스크 텍스처의 등록 이름. */
    public static final Identifier ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "scope_mask");

    private static ScopeMaskTextureHandle instance;
    private static boolean registered = false;

    private ScopeMaskTextureHandle() {
    }

    /**
     * {@link #ID}가 현재 마스크 target의 텍스처를 가리키게 한다. 매 프레임 호출한다(멱등).
     *
     * <p>반드시 마스크를 그린 <b>뒤</b>, 몸체를 그리기 <b>전</b>에 호출해야
     * 몸체가 이번 프레임의 마스크를 샘플링한다.
     *
     * @return 묶기에 성공하면 true. 마스크를 쓸 수 없으면 false(호출하는 쪽은 이를 보고 일반 렌더링으로 돌아간다)
     */
    public static boolean syncToMaskTarget() {
        var target = ScopeMaskTarget.current();
        if (target == null || !ScopeMaskTarget.isAvailable()) {
            return false;
        }
        try {
            if (instance == null) {
                instance = new ScopeMaskTextureHandle();
            }
            // 매 프레임 다시 가리킨다: 창 크기가 바뀌면 ScopeMaskTarget이 target을 다시 만들어
            // 예전 view가 함께 무효가 되므로, 캐시해 두면 끊어진 참조를 얻는다.
            instance.texture = target.getColorTexture();
            instance.textureView = target.getColorTextureView();
            // NEAREST: 마스크는 이진 데이터라 선형 필터는 가장자리에 0.5 근처의 중간값을 만들어
            // shader의 `> 0.5` 판정이 경계에서 흔들리고, 보기에는 털이 난 테두리가 된다.
            instance.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);

            if (!registered) {
                Minecraft.getInstance().getTextureManager().register(ID, instance);
                registered = true;
            }
            return true;
        } catch (Exception e) {
            GunMod.LOGGER.error("[TACZ Scope] Failed to expose mask texture; scope body clipping disabled.", e);
            return false;
        }
    }

    /**
     * 아무것도 풀지 않는다 — 텍스처는 {@link ScopeMaskTarget}의 것이다.
     *
     * <p>TextureManager는 자원 리로드 때 등록 항목에 {@code close()}를 호출하는데,
     * 여기서 정말 풀면 target이 아직 쓰는 텍스처를 미리 없애 버린다.
     */
    @Override
    public void close() {
        // 일부러 비워 둠. javadoc 참고.
    }
}

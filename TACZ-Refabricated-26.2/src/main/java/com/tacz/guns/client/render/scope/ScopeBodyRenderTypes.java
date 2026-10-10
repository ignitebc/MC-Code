package com.tacz.guns.client.render.scope;

import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.tacz.guns.GunMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * 접안렌즈 마스크로 잘리는 조준경 몸체 RenderType.
 *
 * <h2>무엇인가</h2>
 * {@code RenderTypes.entityCutout(texture)}와 <b>한 가지만 다르다</b>:
 * 조각 셰이더에 "접안렌즈 투영 안이면 discard" 단계가 하나 더 있다.
 * 원본의 {@code scope_body: stencilFunc(GL_EQUAL, 0)}에 해당한다.
 *
 * <h2>파이프라인 구성(바닐라 ENTITY_CUTOUT의 &lt;clinit&gt; 역어셈블과 항목별 대조)</h2>
 * 바닐라 {@code ENTITY_CUTOUT}(오프셋 1726-1774)은 다음과 같다:
 * <pre>
 * builder(ENTITY_SNIPPET)
 *     .withLocation("pipeline/entity_cutout")
 *     .withShaderDefine("ALPHA_CUTOUT", 0.1F)
 *     .withShaderDefine("PER_FACE_LIGHTING")
 *     .withBindGroupLayout(SAMPLER1)
 *     .withCull(false)
 * </pre>
 * 이 클래스는 이를 <b>그대로 따르고</b> 세 가지만 더한다:
 * <ul>
 *   <li>{@code withShaderDefine("SCOPE_MASK")} — fsh의 잘라내기 분기를 켠다.</li>
 *   <li>{@code ScopeMaskSampler}만 담은 bind group layout을 직접 만든다 — 마스크 샘플러를 선언한다
 *       ({@code BindGroupLayouts}에는 SAMPLER2까지만 있고 SAMPLER3가 없어,
 *       추가 샘플러는 바닐라 {@code DissolveMaskSampler}처럼 직접 만들어야 한다).</li>
 *   <li>우리 {@code scope_body} 셰이더로 바꾼다(vsh는 바닐라 entity.vsh와 바이트까지 같고,
 *       fsh에는 SCOPE_MASK 부분만 더 있다).</li>
 * </ul>
 *
 * <p>{@code SAMPLER1}을 명시해야 하는 이유: {@code ENTITY_SNIPPET}은
 * {@code SAMPLER0_SAMPLER2}(Sampler1 없음)를 쓰는데, entity.vsh는
 * {@code !NO_OVERLAY}일 때 {@code Sampler1}로 overlay를 읽는다.
 * r52는 이런 선언을 빠뜨려 {@code Missing sampler Sampler0}으로 크래시가 났다.
 *
 * <h2>실패했을 때 물러날 곳</h2>
 * 마스크를 쓸 수 없으면(꺼짐/만들지 못함/그리기 실패) 호출하는 쪽이
 * {@code RenderTypes.entityCutout}, 곧 <b>이미 PASS한 동작</b>으로 돌아가야 한다.
 * 그러면 이 기능 전체가 망가져도 "렌즈 안에 경통 안쪽 벽이 보이는" 정도로 돌아갈 뿐 더 나빠지지 않는다.
 */
@Environment(EnvType.CLIENT)
public final class ScopeBodyRenderTypes {

    /**
     * 마스크 샘플러의 이름.
     *
     * <p>일부러 {@code Sampler3}라고 하지 <b>않는다</b>: {@code BindGroupLayouts}에는
     * {@code SAMPLER3} 상수가 아예 없고(SAMPLER2까지만 있음), 번호 칸은 바닐라가 스스로 예약해 둔 것이다.
     * 추가 샘플러는 바닐라 {@code DissolveMaskSampler}처럼 설명적인 이름을 붙이고
     * layout을 직접 선언해야 한다 — {@code BindGroupLayouts.<clinit>} 오프셋 259-270 참고.
     */
    private static final String MASK_SAMPLER = "ScopeMaskSampler";

    /** 마스크 샘플러의 bind group layout. 바닐라 DISSOLVE_MASK_SAMPLER를 본떠 직접 만든다. */
    private static final BindGroupLayout MASK_SAMPLER_LAYOUT =
            BindGroupLayout.builder().withSampler(MASK_SAMPLER).build();

    private static RenderPipeline buildPipeline(String name, boolean invert) {
        var builder = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "pipeline/" + name))
                .withVertexShader(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "core/scope_body"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "core/scope_body"))
                // 아래 네 가지는 바닐라 ENTITY_CUTOUT과 완전히 같으며 하나도 빠지면 안 된다
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("PER_FACE_LIGHTING")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withCull(false)
                // 이 기능 전용: 잘라내기 분기를 켜고 마스크 샘플러를 선언한다
                .withShaderDefine("SCOPE_MASK")
                .withBindGroupLayout(MASK_SAMPLER_LAYOUT);
        if (invert) {
            // 조준선 판: 렌즈 안만 남긴다(원본 stencilFunc(EQUAL, i+1))
            builder = builder.withShaderDefine("SCOPE_MASK_INVERT");
        }
        return builder.build();
    }

    /** 몸체: 접안렌즈가 <b>덮지 않은</b> 곳에만 그린다. */
    private static final RenderPipeline CLIPPED_PIPELINE =
            buildPipeline("scope_body_clipped", false);

    /** 조준선: 접안렌즈가 <b>덮은</b> 곳에만 그린다. */
    private static final RenderPipeline RETICLE_PIPELINE =
            buildPipeline("scope_reticle_clipped", true);

    /**
     * 텍스처별 캐시.
     *
     * <p>RenderType은 묶음 합치기에 참여하므로 같은 텍스처는 같은 인스턴스를 다시 써야 한다. 아니면 호출할 때마다
     * 새 객체가 생겨 → 묶음이 폭증하고 → 프레임이 떨어진다. 조준경 텍스처 종류는 많지 않아(한 자릿수)
     * 상한 없는 HashMap을 써도 메모리 문제가 없다.
     */
    private static final Map<Identifier, RenderType> BODY_CACHE = new HashMap<>();
    private static final Map<Identifier, RenderType> RETICLE_CACHE = new HashMap<>();

    private ScopeBodyRenderTypes() {
    }

    /**
     * 몸체: 접안렌즈가 <b>덮지 않은</b> 곳에만 그린다.
     *
     * <p>원본 {@code scope_body: stencilFunc(GL_EQUAL, 0)}과 같다.
     */
    public static RenderType clipped(Identifier texture) {
        return BODY_CACHE.computeIfAbsent(texture,
                tex -> create("tacz_scope_body_clipped", CLIPPED_PIPELINE, tex));
    }

    /**
     * 조준선: 접안렌즈가 <b>덮은</b> 곳에만 그린다.
     *
     * <p>원본 {@code renderDivisionOnly: stencilFunc(GL_EQUAL, i+1)}과 같다 —
     * 조준선이 접안렌즈 투영 안으로 묶여 경통 밖 화면에 붙지 않는다.
     */
    public static RenderType reticle(Identifier texture) {
        return RETICLE_CACHE.computeIfAbsent(texture,
                tex -> create("tacz_scope_reticle_clipped", RETICLE_PIPELINE, tex));
    }

    private static RenderType create(String name, RenderPipeline pipeline, Identifier tex) {
        return RenderType.create(name,
                RenderSetup.builder(pipeline)
                        // Sampler0 = 조준경 자신의 텍스처. r52의 교훈: 파이프라인이 선언한 sampler는
                        // 모두 여기서 묶어야 하며, 하나라도 빠지면 drawIndexed 때 Missing sampler가 난다.
                        .withTexture("Sampler0", tex)
                        // 마스크 샘플러 = 접안렌즈 마스크. ScopeMaskTextureHandle이 등록한 텍스처를 가리키며,
                        // 매 프레임 현재 마스크 target의 view로 새로 고쳐진다.
                        .withTexture(MASK_SAMPLER, ScopeMaskTextureHandle.ID)
                        // useLightmap/useOverlay가 Sampler2/Sampler1을 제공한다.
                        // 바닐라 entityCutout의 RenderSetup과 같다.
                        .useLightmap()
                        .useOverlay()
                        .createRenderSetup());
    }
}

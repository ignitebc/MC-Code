package com.tacz.guns.client.render.scope;

import cn.sh1rocu.tacz.compat.iris.IrisHandPass;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.PrimitiveTopology;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.model.bedrock.BedrockCube;
import com.tacz.guns.client.model.bedrock.BedrockCubeBox;
import com.tacz.guns.config.client.RenderConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Optional;

/**
 * [Step 2 정식판] 이번 프레임의 모든 접안렌즈 형상을 화면 밖 마스크에 그린다.
 *
 * <h2>이 단계가 증명하려는 것</h2>
 * 미리 보기 칸에 <b>총을 따라 움직이는 흰 모양</b>이 나타나야 한다.
 * 이것으로 전체 방안의 가장 핵심 가정을 한 번에 검증한다:
 * <b>잘라내기 영역 = 접안렌즈 형상의 화면 투영</b>.
 *
 * <p>이것이 바로 원본 stencil의 실제 의미다({@code SCOPE_UPSTREAM_TRUTH} §1):
 * <pre>
 * renderOcularStencil: colorMask(false×4) + stencilOp(KEEP,KEEP,REPLACE)
 *     → 스텐실이 0이 아닌 영역 = 접안렌즈의 [화면 투영 모양]
 * scope_body: stencilFunc(EQUAL, 0)   // 접안렌즈가 덮지 않은 곳에만 몸체를 그림
 * </pre>
 * 모양이 맞으면 = 뒤의 "몸체가 마스크를 샘플링해 discard"가 반드시 성립한다.
 * 모양이 틀리면(총을 따라가지 않음 / 위치가 틀림) = 더 깊은 오해가 있으니 여기서 멈추는 편이 계속 쓰는 것보다 낫다.
 *
 * <h2>collector / RenderType을 쓰지 않는 이유</h2>
 * r51이 그렇게 했다 — 접안렌즈에 {@code outputTarget}이 다른 RenderType을 주고
 * 평소처럼 collector를 탔다. 그러자 엔진이 RenderType별로 묶어 실행하면서
 * "주 target → 마스크 target → 주 target" 전환을 solid 단계 안에 <b>여기저기 끼워 넣어</b>
 * {@code VK_ERROR_DEVICE_LOST}를 일으켰다.
 *
 * <p>그래서 여기서는 <b>collector를 완전히 피해</b> 정점 버퍼를 직접 만들고 단계 경계에서 한 번에 그린다.
 * 이 시점이 안전하다는 것은 앞선 빈 pass 시험으로 실측 확인했다(미리 보기 칸이 초록색으로 바뀜).
 *
 * <h2>그리기 구성({@code PreparedRenderType#drawFromBuffer} 역어셈블과 항목별 대조)</h2>
 * <pre>
 * createRenderPass(이름, 색 첨부, 비우기 색)
 * setPipeline(pipeline)
 * RenderSystem.bindDefaultUniforms(pass)               // ← 빠지면 Projection/Fog가 없다
 * setUniform("DynamicTransforms", ModelView 쓰기)      // ← 빠지면 shader가 ModelViewMat을 얻지 못한다
 * setVertexBuffer(0, vertexBuffer.slice())
 * setIndexBuffer(공유 사각형 인덱스, 종류)
 * drawIndexed(0, 0, indexCount, 1)
 * </pre>
 */
@Environment(EnvType.CLIENT)
public final class ScopeMaskRenderer {

    /**
     * 마스크 파이프라인.
     *
     * <h3>{@code ENTITY}가 아니라 {@code POSITION} 형식을 쓰는 이유</h3>
     * 마스크는 "이 픽셀이 접안렌즈에 덮였는지"만 알면 되고 텍스처·조명·법선은 <b>필요 없다</b>.
     * 가장 단순한 형식을 쓰면 장점이 세 가지 있다:
     * <ul>
     *   <li>정점 데이터가 가장 작다(정점당 12바이트).</li>
     *   <li>{@code core/position} 셰이더는 <b>sampler를 하나도 선언하지 않는다</b> —
     *       r52의 {@code Missing sampler Sampler0} 함정을 완전히 피한다.</li>
     *   <li>셰이더를 직접 쓸 필요 없이 바닐라 것을 쓰므로
     *       r46 같은 "셰이더가 선언한 uniform과 파이프라인이 맞지 않는" 위험도 없다.</li>
     * </ul>
     *
     * <h3>색에 대해</h3>
     * {@code position.fsh}는 {@code apply_fog(ColorModulator, ...)}를 출력한다.
     * {@code ColorModulator}는 DynamicTransforms가 제공하며 우리는 순백을 쓴다.
     * 그래서 접안렌즈가 덮은 곳 = 흰색, 나머지 = 비우기 색(검정). 딱 이진 마스크다.
     *
     * <p>안개는 가까운 거리(손에 든 물건은 바로 눈앞)에서 거의 줄지 않아 판독에 영향이 없다.
     * 게다가 이 단계는 <b>모양</b>만 보고 색 정밀도는 보지 않는다.
     *
     * <h3>깊이와 컬링</h3>
     * 깊이 상태는 {@code Optional.empty()}를 넘긴다: 마스크 target은 {@code useDepth=false}로
     * 만들어 깊이 첨부가 없으므로, 파이프라인도 깊이가 필요 없다고 선언해야 한다.
     *
     * <p>{@code withCull(false)}: 접안렌즈는 <b>한 겹 얇은 판</b>이라(실측 조준경 33개 중 30개의
     * 접안렌즈 z 두께 &lt; 0.15) 모델링 방향에 따라 뒷면 컬링이 통째로 지울 수 있다.
     * 마스크는 "투영 모양"만 필요하므로 앞뒷면 모두 센다.
     */
    private static final RenderPipeline MASK_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            // GLOBALS + MATRICES_PROJECTION을 직접 조합하지 않고 MATRICES_FOG_SNIPPET을 쓴다.
            //
            // 이전 판은 실제로 다음 오류가 났다:
            //     Couldn't compile pipeline tacz:pipeline/scope_mask:
            //         Unable to find shader defined uniform (Fog)
            // 원인은 core/position.fsh의 apply_fog(...)가 Fog uniform 블록을 참조하는데,
            // BindGroupLayouts.MATRICES_PROJECTION은
            // DynamicTransforms + Projection 두 uniform만 선언하기 때문이다(바이트코드 확인).
            // Fog / Globals는 [각각 독립된] layout이라 하나라도 빠지면 컴파일되지 않는다.
            //
            // 바닐라가 이 조합을 이미 감싸 두었다(RenderPipelines <clinit> 오프셋 30-57):
            //     MATRICES_FOG_SNIPPET = builder(GLOBALS_SNIPPET)
            //                              .withBindGroupLayout(MATRICES_PROJECTION)
            //                              .withBindGroupLayout(FOG)
            // 곧 Globals + DynamicTransforms + Projection + Fog이며,
            // core/position 셰이더에 필요한 전부다. 직접 조합하지 않고 그대로 다시 쓴다.
            .withLocation(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "pipeline/scope_mask"))
            .withVertexShader("core/position")
            .withFragmentShader("core/position")
            // 섞지 않고 깊이도 쓰지 않는다: 마스크는 "덮임=흰색, 안 덮임=비우기 색"인 이진 그림이라
            // 그대로 덮어쓰면 된다. Optional.empty()는 [blend를 켜지 않음]을 뜻한다
            // (ColorTargetState의 두 생성자 대조: 인자 하나짜리는 "blend 있음",
            //  인자 세 개짜리의 첫 인자는 Optional<BlendFunction>이며 empty = 혼합 끔).
            .withColorTargetState(new ColorTargetState(
                    Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
            // 마스크 target은 useDepth=false로 만들었다 — [깊이 첨부가 없다].
            //
            // 그래서 DepthStencilState 인스턴스가 아니라 Optional.empty()를 넘겨야 한다.
            // 이전 판은 new DepthStencilState(ALWAYS_PASS, false)를 넘겨 "검사도 쓰기도 안 함"을 노렸지만,
            // RenderPipeline#wantsDepthTexture()의 기준은
            //     return this.depthStencilState != null;
            // 이다. 즉 [이 필드를 설정하기만 하면 파이프라인이 깊이 첨부가 필요하다고 선언하며],
            // 안이 ALWAYS_PASS인지와 무관하다. 그런데 우리 render pass에는 깊이 첨부가 아예 없어
            // 선언과 실제가 어긋나 그리기가 버려졌다 — "로그에는 인덱스 36개를 그렸다는데 화면은 온통 검은" 현상이었다.
            //
            // 바닐라는 "깊이 첨부 없음"에 항상 Optional.empty()를 쓴다(<clinit>에 4곳,
            // 모두 TEXT_SEE_THROUGH / GUI_TEXT처럼 깊이가 필요 없는 파이프라인). 그 관례를 따른다.
            .withDepthStencilState(Optional.empty())
            // 접안렌즈는 [한 겹 얇은 판]이라(실측 조준경 33개 중 30개의 접안렌즈 z 두께 < 0.15)
            // 모델링 방향에 따라 뒷면 컬링이 통째로 지울 수 있다. 마스크는 투영 모양만 필요하므로 앞뒷면 모두 센다.
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();

    /** 정점 임시 버퍼. 매 프레임 할당하지 않도록 같은 것을 다시 쓴다. */
    private static final ByteBufferBuilder SCRATCH = new ByteBufferBuilder(4096);

    private static boolean failed = false;

    /**
     * 지금 손에 든 물건(1인칭 총기)을 렌더링하는 중인지.
     *
     * <p>{@code renderAllFeatures}는 매 프레임 여러 번 호출되는데(월드 한 번, 손 한 번),
     * 조준경은 손 렌더링 때만 나온다. 마스크는 <b>손 렌더링 때만</b> 그려야 한다:
     * 월드 렌더링 때도 돌면 target을 한 번 비워 손 렌더링 결과를 지워 버린다.
     * {@code GameRendererMixin}의 {@code renderItemInHand} HEAD/RETURN이 관리한다.</p>
     */
    private static boolean inHandPass = false;

    private ScopeMaskRenderer() {
    }

    public static void setInHandPass(boolean value) {
        inHandPass = value;
    }

    /**
     * Iris 셰이더팩을 쓰면 손은 Iris의 HandRenderer가 월드 렌더링 도중에 따로 그린다.
     * 접안렌즈 형상 등록과 스코프 몸체 그리기가 모두 그 패스에서 일어나므로, 마스크도 같은 패스에서 그려야
     * 같은 투영·모델뷰 행렬을 쓰고 몸체보다 먼저 준비된다. 바닐라 패스에서 그리면 행렬이 달라
     * 위치가 어긋나고 한 프레임 늦어져, 렌즈가 뚫리지 않고 검게 막힌다.
     */
    private static boolean isHandPass() {
        return inHandPass || IrisHandPass.isActive();
    }

    /**
     * 단계 경계에서 이번 프레임에 등록된 접안렌즈 형상을 마스크 target에 그린다.
     *
     * <p>성공하든 실패하든 마지막에 이번 프레임 목록을 비운다 — {@code finally} 참고.
     */
    public static void renderAtPhaseBoundary() {
        if (!isHandPass()) {
            // 월드 렌더링 때는 바로 건너뛰며 목록을 [비우지 않는다] —
            // 접안렌즈는 손 렌더링의 submit 단계에서 등록되고 손 렌더링은 월드 뒤에 일어나므로,
            // 그래서 이 시점에 목록은 원래 비어 있다. 굳이 비우면 오히려 해가 될 수 있다(혹시 순서가 바뀌면).
            //
            // 비우기를 놓치지 않을까? 아니다: 등록은 1인칭 손 경로에서만 일어나고,
            // 그 경로 뒤에는 반드시 inHandPass=true인 renderAllFeatures가 한 번 따라오며,
            // 그때의 finally가 마지막에 비운다.
            return;
        }
        if (!RenderConfig.SCOPE_MASK_ENABLE.get()) {
            // 기능이 꺼져 있어도 비워야 한다. 아니면 목록이 끝없이 커진다.
            ScopeMaskGeometry.clear();
            return;
        }
        try {
            if (failed || ScopeMaskGeometry.isEmpty()) {
                return;
            }
            TextureTarget target = ScopeMaskTarget.getOrCreate();
            if (target == null) {
                return;
            }
            drawMask(target);
        } catch (Exception e) {
            failed = true;
            GunMod.LOGGER.error("[TACZ Scope] Failed to render ocular mask; mask disabled.", e);
        } finally {
            // 핵심: 무조건 비운다. 위에서 return했더라도 형상을 다음 프레임까지 남기면 안 된다 —
            // 아니면 조준경을 집어넣은 뒤 마스크가 "달라붙어" 사라지지 않는다.
            ScopeMaskGeometry.clear();
        }
    }

    private static void drawMask(TextureTarget target) {
        MeshData mesh = buildMesh();
        if (mesh == null) {
            // 그릴 형상이 없음: 그래도 pass를 한 번 열어 마스크를 비운다.
            // 아니면 직전 프레임의 흰 모양이 텍스처에 남는다(target은 스스로 검게 되지 않는다).
            clearOnly(target);
            return;
        }
        try (mesh) {
            MeshData.DrawState draw = mesh.drawState();
            GpuBuffer vertexBuffer = null;
            try {
                vertexBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "tacz_scope_mask_vertices",
                        GpuBuffer.USAGE_VERTEX,
                        mesh.vertexBuffer());

                // 공유 사각형 인덱스 버퍼: QUADS를 삼각형으로 펼친다.
                // 바닐라에 있는 것을 쓰므로 인덱스를 직접 만들 필요가 없다.
                RenderSystem.AutoStorageIndexBuffer indices =
                        RenderSystem.getSequentialBuffer(draw.primitiveTopology());
                GpuBuffer indexBuffer = indices.getBuffer(draw.indexCount());

                CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
                try (RenderPass pass = encoder.createRenderPass(
                        () -> "tacz_scope_mask",
                        target.getColorTextureView(),
                        // 매 프레임 완전 검정에서 다시 시작한다. 마스크는 "이번 프레임에 접안렌즈가 덮은 곳"이라 과거 의미가 없다.
                        Optional.of(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)))) {
                    pass.setPipeline(MASK_PIPELINE);
                    // 이 두 줄은 하나도 빠지면 안 되며 PreparedRenderType#drawFromBuffer를 따라 썼다:
                    //   bindDefaultUniforms는 Projection / Fog 등 전역 uniform을 제공한다.
                    //   DynamicTransforms는 ModelViewMat과 ColorModulator를 제공한다.
                    // 하나라도 빠지면 uniform이 없어 shader가 올바른 결과를 그리지 못한다
                    // (증상은 r46의 "Unable to find shader defined uniform"과 비슷하다).
                    RenderSystem.bindDefaultUniforms(pass);
                    pass.setUniform("DynamicTransforms",
                            RenderSystem.getDynamicUniforms().writeTransform(
                                    // RenderType#prepare와 같은 행렬 출처를 쓴다:
                                    // 정점은 등록할 때 전체 모델 행렬을 이미 곱했으므로, 여기서 카메라 ModelView를 다시 곱하면
                                    // 주 렌더링 경로와 완전히 같아 마스크가 화면과 딱 맞는다.
                                    RenderSystem.getModelViewMatrixCopy(),
                                    // R = 1: 접안렌즈에 덮인 픽셀은 빨간 채널이 항상 1(마스크 본체).
                                    // G = 조준 진행도: 몸체/조준선 shader가 화면 공간에서 점점 줄이는 데 쓴다.
                                    //
                                    // uniform을 새로 더하지 않고 진행도를 색 채널에 넣은 이유:
                                    // 마스크 파이프라인은 원래 ColorModulator를 써야 하므로 초록 채널이 이미 비어 있는 운반 수단이다.
                                    // uniform을 새로 더하면 bind group layout을 또 바꿔야 하고,
                                    // 그것이 바로 r46/r52 두 번의 크래시 원인이었다. 건드리지 않을 수 있으면 건드리지 않는다.
                                    new Vector4f(1.0f, currentAimingProgress(), 1.0f, 1.0f)));
                    pass.setVertexBuffer(0, vertexBuffer.slice());
                    pass.setIndexBuffer(indexBuffer, indices.type());
                    // [인자 순서는 바이트코드를 따름] RenderPass#drawIndexed는 int 5개다.
                    // 바닐라 PreparedRenderType#drawFromBuffer 오프셋 227-237의 실제 인자는 차례로:
                    //     aload  indexCount(로컬 슬롯 6)
                    //     iconst_1
                    //     iload  firstIndex(슬롯 5)
                    //     iload  baseVertex(슬롯 4)
                    //     iconst_0
                    // 즉 drawIndexed(indexCount, 1, firstIndex, baseVertex, 0)이다.
                    // 우리 정점/인덱스는 처음부터 시작하는 한 묶음이므로 firstIndex와 baseVertex는 모두 0이다.
                    pass.drawIndexed(draw.indexCount(), 1, 0, 0, 0);
                }
            } finally {
                if (vertexBuffer != null) {
                    // 매 프레임 새로 만들고 매 프레임 풀어 준다. 버퍼 풀은 두지 않는다 — 접안렌즈 형상은 아주 적어서
                    // (조준경 하나에 cube 몇 개) 섣부른 최적화는 수명 주기 실수만 늘린다.
                    vertexBuffer.close();
                }
            }
        }
    }

    /**
     * 현재 조준 진행도(0 = 전혀 조준하지 않음, 1 = 완전히 조준함).
     *
     * <p>마스크의 초록 채널에 써서 몸체/조준선 shader가 화면 공간에서 점점 바뀌게 한다.
     * {@code BedrockAttachmentModel#currentAimingProgress}와 출처가 같고,
     * 둘 다 {@code IClientPlayerGunOperator}에서 얻는다.
     */
    private static float currentAimingProgress() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0.0f;
        }
        return Mth.clamp(IClientPlayerGunOperator.fromLocalPlayer(player)
                .getClientAimingProgress(Minecraft.getInstance().getDeltaTracker()
                        .getGameTimeDeltaPartialTick(false)), 0.0f, 1.0f);
    }

    /** 형상이 없어도 target을 검게 칠해야 한다. 아니면 직전 프레임 모양이 남는다. */
    private static void clearOnly(TextureTarget target) {
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (RenderPass pass = encoder.createRenderPass(
                () -> "tacz_scope_mask_clear",
                target.getColorTextureView(),
                Optional.of(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)))) {
            pass.pushDebugGroup(() -> "tacz_scope_mask_empty");
            pass.popDebugGroup();
        }
    }

    /**
     * 이번 프레임에 등록된 모든 접안렌즈 cube를 정점 데이터 하나로 만든다.
     *
     * @return 정점 메시. 그릴 형상이 하나도 없으면 {@code null}
     */
    private static MeshData buildMesh() {
        BufferBuilder builder = new BufferBuilder(SCRATCH, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
        for (ScopeMaskGeometry.Entry entry : ScopeMaskGeometry.entries()) {
            Matrix4f pose = entry.pose();
            for (BedrockCube cube : entry.cubes()) {
                // 인터페이스로 면을 얻으며 [instanceof 판단은 하지 않는다].
                //
                // 첫 판은 `if (cube instanceof BedrockCubeBox box)`였는데 실측하니 마스크가 온통 검었다:
                // 기본 총기 팩의 접안렌즈 육면체 161개가 [하나도 빠짐없이] BedrockCubePerFace였고
                // (모두 face_uv를 가짐), 그 instanceof가 100% 걸러 냈다.
                // 두 구현의 polygons 구조는 완전히 같으므로 능력은 인터페이스로 표현해야 한다.
                writeCube(builder, pose, cube);
            }
        }
        // 정점을 하나도 쓰지 않으면 build()가 null을 돌려주며, 호출하는 쪽은 이를 보고 "비우기만" 분기로 간다.
        return builder.build();
    }

    /**
     * 육면체 하나의 6면을 쓴다.
     *
     * <p>정점 변환은 {@link BedrockCubeBox#compile}과 <b>줄마다 같다</b>:
     * {@code pos / 16 → mul(matrix)}. 두 경로는 같은 계산법을 써야 하며,
     * 아니면 마스크가 화면과 어긋난다 — 그런 차이는 찾기가 아주 어렵다.
     */
    private static void writeCube(BufferBuilder builder, Matrix4f pose, BedrockCube cube) {
        for (var polygon : cube.getPolygons()) {
            if (polygon == null) {
                continue;
            }
            for (var vertex : polygon.vertices) {
                float x = vertex.pos.x() / 16.0F;
                float y = vertex.pos.y() / 16.0F;
                float z = vertex.pos.z() / 16.0F;
                Vector4f v = new Vector4f(x, y, z, 1.0F);
                v.mul(pose);
                builder.addVertex(v.x(), v.y(), v.z());
            }
        }
    }
}

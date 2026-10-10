package com.tacz.guns.client.render.scope;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.renderer.snapshot.BedrockRenderSnapshot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 새긴 눈금 조준선(발광 없음).
 *
 * <h2>어떤 빈틈을 메우는가</h2>
 * 예전에는 {@link IlluminatedReticleRenderer}만 등록했는데, 그 {@code matches()}는
 * <b>발광 노드가 있어야</b> 맞는다. 기본 총기 팩에는 <b>새김만 있는</b> 조준경이 6개 있다
 * — {@code division}만 있고 {@code *_illuminated}는 하나도 없다:
 * <pre>
 * scope_1873_6x(스프링필드)  scope_98k(마우저)   scope_aug_default(AUG 기본)
 * scope_contender            scope_qmk152        scope_retro_2x
 * </pre>
 * 그래서 어떤 전략에도 맞지 않아 렌즈 안에 조준선이 전혀 없었다 — 사용자가 실제로 본 문제다.
 * {@code ReticleRendererRegistry}의 "P2에서 EtchedReticleRenderer를 보충"한다는 말이
 * 바로 이 클래스였고, 그동안 만들지 않았다.
 *
 * <h2>이제야 division을 그릴 수 있는 이유</h2>
 * {@code division}에는 <b>차광판</b>이 섞여 있다: 조준선이 아니라 렌즈 밖 시야를 가리는 큰 면 몇 개다.
 * 실측 크기가 꽤 크고({@code scope_qmk152}는 한 장 면적이 6486,
 * {@code scope_1873_6x}에는 96×34짜리가 있다), 9차에 구분 없이 한 번 그렸다가
 * 큰 검은 덩어리가 화면을 덮어 10차에 되돌렸다.
 *
 * <p>원본이 통째로 그릴 수 있었던 것은 stencil이 받쳐 줬기 때문이다
 * ({@code renderDivisionOnly: stencilFunc(GL_EQUAL, i+1)}가 모든 것을 접안렌즈 원 안으로 잘랐다).
 * <b>이제 우리에게도 같은 역할이 있다</b> — 반대 잘라내기 RenderType이다.
 * 하나씩 대조했다: 이 차광판들의 XY 범위는 <b>모두 접안렌즈 투영 밖에 있다</b>
 * (예: {@code scope_retro_2x} 접안렌즈 X∈[-0.75,0.75], 차광판 X∈[-32,-8]).
 * 그래서 반대 잘라내기로 통째로 버려지고 9차의 화면 덮개가 되풀이되지 않는다.
 *
 * <p>다시 말해 이 전략은 <b>마스크가 있어야 성립한다</b>. 마스크를 쓸 수 없으면(설정 꺼짐 등)
 * 호출하는 쪽이 잘리지 않은 RenderType을 넘기고, 이때 차광판이 드러난다 —
 * 그래서 {@link #submitReticle}에 마스크가 적용되지 않으면 그리지 않는 안전장치를 두었다.
 */
public final class EtchedReticleRenderer implements IReticleRenderer {

    public static final EtchedReticleRenderer INSTANCE = new EtchedReticleRenderer();

    /**
     * 나타나기 시작하는 조준 진행도. {@link IlluminatedReticleRenderer}와 맞춰
     * 두 종류 조준선이 나타나는 때를 통일한다.
     */
    private static final float FADE_IN_START = 0.35f;

    private EtchedReticleRenderer() {
    }

    @Override
    public boolean matches(ScopeNodeSet nodes) {
        // 새김만 있는 조준경만 받는다. 발광 노드가 있으면 IlluminatedReticleRenderer에 맡긴다 —
        // 그쪽 priority는 이 클래스와 같지만 먼저 등록되어 먼저 맞는다.
        return nodes.hasEtched() && !nodes.hasIlluminated();
    }

    @Override
    public void submitReticle(Context ctx, ScopeNodeSet nodes) {
        if (!ctx.maskActive()) {
            // [안전장치] 마스크가 적용되지 않으면 절대 그리지 않는다.
            // division의 차광판은 아주 크다(scope_qmk152 한 장 면적 6486).
            // 반대 잘라내기가 없으면 통째로 화면을 덮는다 — 9차에 겪었고 10차에 되돌렸다.
            // 이 조준경들이 잠시 조준선이 없을지언정(= 수정 전 상태) 화면을 덮어서는 안 된다.
            return;
        }
        float progress = ctx.aimingProgress();
        if (progress <= FADE_IN_START) {
            return;
        }
        float alpha = (progress - FADE_IN_START) / (1.0f - FADE_IN_START);
        alpha = Math.min(1.0f, Math.max(0.0f, alpha));

        for (BedrockPart part : nodes.etchedReticle()) {
            submitOne(ctx, part, alpha);
        }
    }

    /**
     * {@code IlluminatedReticleRenderer#submitOne}과 구조가 같다.
     *
     * <p>유일한 차이: {@code part.illuminated}에 기대지 않고 새긴 눈금은 물려받은 조명을 쓴다.
     * 그래서 어두운 곳에서는 함께 어두워진다 — "유리에 새긴 선"이라는 물리적 직관에 맞는다.
     */
    private void submitOne(Context ctx, BedrockPart part, float alpha) {
        PoseStack poseStack = ctx.poseStack();

        // captureSubtree는 rootPose에 이 노드와 부모 사슬의 모든 변환이 적용되어 있기를 요구하므로,
        // 아래에서 위로 조상 사슬을 모은 뒤 위에서 아래로 적용한다.
        Deque<BedrockPart> chain = new ArrayDeque<>();
        for (BedrockPart p = part; p != null; p = p.getParent()) {
            chain.push(p);
        }

        // division은 생성자에서 setHidden(true)되고(주 렌더링 목록에 넣지 않기 위해서다),
        // 스냅숏 순회기는 visible=false를 만나면 바로 return한다. 여기서는 사슬 전체를 잠시 켜고,
        // 그린 뒤 finally에서 하나씩 되돌린다 — BedrockPart는 여러 프레임이 함께 쓰므로 되돌리지 않으면 다른 곳까지 망가진다.
        List<BedrockPart> touched = new ArrayList<>();
        List<Boolean> saved = new ArrayList<>();
        for (BedrockPart p : chain) {
            touched.add(p);
            saved.add(p.visible);
            p.visible = true;
        }

        poseStack.pushPose();
        BedrockRenderSnapshot snapshot;
        try {
            for (BedrockPart p : chain) {
                p.translateAndRotateAndScale(poseStack);
            }
            snapshot = BedrockRenderSnapshot.captureSubtree(
                    part, poseStack, ctx.displayContext(),
                    ctx.light(), ctx.overlay(),
                    1.0f, 1.0f, 1.0f, alpha);
        } finally {
            poseStack.popPose();
            for (int i = 0; i < touched.size(); i++) {
                touched.get(i).visible = saved.get(i);
            }
        }

        if (!snapshot.isEmpty()) {
            // 스냅숏 행렬에는 들어온 pose 전체가 이미 들어 있으므로, 단위 행렬에서 제출해야 루트 변환이 두 번 적용되지 않는다.
            PoseStack identity = new PoseStack();
            ctx.collector().submitCustomGeometry(
                    identity, ctx.baseRenderType(),
                    (entryPose, consumer) -> snapshot.write(consumer));
        }
    }
}

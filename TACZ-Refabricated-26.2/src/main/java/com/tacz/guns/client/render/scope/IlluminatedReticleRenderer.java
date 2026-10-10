package com.tacz.guns.client.render.scope;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.renderer.snapshot.BedrockRenderSnapshot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * P1 전략: <b>발광</b> 조준선({@code *_illuminated} 노드)만 그린다.
 *
 * <p>{@link ReticleKind#HOLOGRAPHIC}과 {@link ReticleKind#HYBRID} 두 형태를 맡는다
 * — 곧 기본 총기 팩 조준경 33개 중 31개다. 순수 새김 조준경({@code scope_98k},
 * {@code scope_retro_2x})은 뒤의 P2 새김 전략이 맡는다.</p>
 *
 * <h2>P1이 발광 층만 그려도 안전한 이유</h2>
 * {@code division} 노드에는 <b>차광판</b>이 섞여 있다: 예를 들어 {@code scope_1873_6x}의
 * {@code division}에는 cube가 10개 있고, 그중 두 장은 32×32 큰 면이다
 * ({@code origin=[-14.0625,-37.1875,-111] size=[32,32,0]}).
 * 원본은 stencil로 이를 원 밖으로 잘라냈지만, 우리에게는 stencil이 없어 구분 없이 그리면
 * <b>9차의 화면을 덮던 검은 사각형</b>이 다시 나온다(10차에 한 번 되돌렸다).
 *
 * <p>반면 {@code *_illuminated} 노드는 모두 작은 형상(도트, 가는 선)이라
 * 차광판일 수 없으므로 P1은 크기 추정 없이도 안전하게 쓸 수 있다.</p>
 *
 * <h2>"시차"에 대해: r44에서 직접 만든 근사를 지웠다</h2>
 * 예전에는 {@code applyParallax()}가 있어 조준 진행도에 따라 조준선을 광축을 따라 0.75 단위 앞으로 밀어,
 * 홀로그램 조준경의 "조준선이 무한히 먼 곳에 떠 있는" 느낌을 흉내 내려 했다. <b>그 로직은 지웠다</b>. 이유:
 * <ul>
 *   <li><b>원본에 대응하는 것이 전혀 없다.</b> 1.21.1 원본 저장소 전체에서
 *       {@code collimat} / {@code parallax} / {@code billboard}를 grep하면 <b>0건</b>이다.
 *       조준선 형상은 총몸에 단단히 붙어 있고 위치 보정을 한 적이 없다.</li>
 *   <li>플레이어가 보는 "조준선이 시점을 따라 움직이는" 현상은 <b>실제 원근의 자연스러운 부산물</b>이다
 *       — {@code division}은 원래 대물렌즈 앞 아주 먼 곳에 있다
 *       (실측 {@code scope_acog_ta31}의 {@code division_illuminated}는 z=-99.875).
 *       시점이 움직이면 먼 그것과 가까운 조준경 틀 사이에 자연히 상대 이동이 생기므로 따로 보정할 필요가 없다.</li>
 * </ul>
 * 이 설명은 뒤에 오는 사람이 같은 형상 근사를 다시 "발명"하지 않도록 남겨 둔다.
 */
public final class IlluminatedReticleRenderer implements IReticleRenderer {

    public static final IlluminatedReticleRenderer INSTANCE = new IlluminatedReticleRenderer();

    /**
     * 조준선이 나타나기 시작하는 조준 진행도. 이보다 낮으면 전혀 그리지 않는다 —
     * 조준하지 않을 때 도트가 화면에 켜져 있으면 안 된다(현실에서도 눈이 광축에 없어 보이지 않는다).
     */
    private static final float FADE_IN_START = 0.35f;

    /**
     * 시차 앞밀기의 최대 거리(모델 공간 단위, 1단위 = 1/16칸).
     *
     * <p>값 설명: 기본 총기 팩의 {@code division_illuminated} z는 대체로
     * -45 ~ -100이며(예: {@code sight_exp3}은 -45, {@code scope_acog_ta31}은 -99.875),
     * 몸체 기준으로는 몇 단위만 떠 있다. 여기서 0.75는 <b>보수적인</b> 값으로,
     * "조준선이 렌즈 앞에 떠 있는" 분리감을 주기에 충분하면서 모델을 뚫을 만큼 크지 않다.</p>
     */

    private IlluminatedReticleRenderer() {
    }

    @Override
    public boolean matches(ScopeNodeSet nodes) {
        // 발광 노드가 있으면 이 전략이 맡는다(HOLOGRAPHIC과 HYBRID 모두 여기로 온다).
        return nodes.hasIlluminated();
    }

    @Override
    public void submitReticle(Context ctx, ScopeNodeSet nodes) {
        float progress = ctx.aimingProgress();
        if (progress <= FADE_IN_START) {
            return;
        }
        // 선형 나타나기: FADE_IN_START -> 1.0을 alpha 0 -> 1로 대응시킨다.
        // 원본은 stencil로 딱 잘렸지만(전부 아니면 전무) 여기서는 부드럽게 전환해 보기가 더 매끄럽다.
        float alpha = (progress - FADE_IN_START) / (1.0f - FADE_IN_START);
        alpha = Math.min(1.0f, Math.max(0.0f, alpha));

        List<BedrockPart> reticles = nodes.illuminatedReticle();
        for (BedrockPart part : reticles) {
            submitOne(ctx, part, alpha);
        }
    }

    private void submitOne(Context ctx, BedrockPart part, float alpha) {
        PoseStack poseStack = ctx.poseStack();

        // 이 노드들은 [여러 프레임이 함께 쓴다]: visible이 다른 곳에서(생성자가 부모 division을
        // 숨김) false일 수 있고, 스냅숏 순회기는 visible=false를 만나면 바로 return한다.
        // 그래서 잠시 켜고 그린 뒤 되돌려야 한다 — 4차에 "공유 상태를 되돌리지 않아" 낭패를 봤다.
        // captureSubtree는 rootPose에 이 노드와 부모 사슬의 모든 변환이 [이미] 적용되어 있기를 요구한다
        // (자식 노드로 재귀할 때만 translateAndRotateAndScale한다).
        // 그래서 아래에서 위로 조상 사슬을 모은 뒤 위에서 아래로 적용해야 한다 — BedrockModel#getPath와 구조가 같다.
        // 이 단계를 빠뜨리면 조준선이 접안렌즈 위치가 아니라 조준경 원점에 그려진다.
        Deque<BedrockPart> chain = new ArrayDeque<>();
        for (BedrockPart p = part; p != null; p = p.getParent()) {
            chain.push(p);
        }

        // 사슬의 조상이 숨겨져 있을 수 있고(예: division_illuminated의 부모 division은 생성자에서
        // setHidden(true)된다), 스냅숏 순회기는 visible=false를 만나면 바로 return한다.
        // 여기서 사슬 전체를 잠시 보이게 하고, 그린 뒤 finally에서 하나씩 되돌린다.
        // 주의: visible 표시만 바꾸고 형상은 전혀 바꾸지 않는다 — 조상 자신의 cubes는 그려지지 않는다.
        // captureSubtree는 part라는 루트에서부터만 모으기 때문이다.
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
                    part,
                    poseStack,
                    ctx.displayContext(),
                    // 조명 값은 스냅숏 안에서 part.illuminated가 최대 밝기(15728880)로 덮어쓰므로,
                    // 여기서는 물려받은 조명을 넘기면 되고 직접 고정할 필요가 없다.
                    ctx.light(),
                    ctx.overlay(),
                    1.0f, 1.0f, 1.0f, alpha);
        } finally {
            poseStack.popPose();
            for (int i = 0; i < touched.size(); i++) {
                touched.get(i).visible = saved.get(i);
            }
        }

        // BedrockModel#submit과 같은 제출 관례를 따른다:
        // 스냅숏의 행렬에는 들어온 pose 전체가 이미 들어 있으므로 [단위 행렬]에서 제출해야 한다.
        // 아니면 루트 변환이 두 번 적용된다.
        if (!snapshot.isEmpty()) {
            PoseStack identity = new PoseStack();
            ctx.collector().submitCustomGeometry(
                    identity, ctx.baseRenderType(),
                    (entryPose, consumer) -> snapshot.write(consumer));
        }
    }
}

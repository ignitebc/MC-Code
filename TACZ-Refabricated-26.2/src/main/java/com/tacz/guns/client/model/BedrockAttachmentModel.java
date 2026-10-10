package com.tacz.guns.client.model;

import com.mojang.blaze3d.vertex.*;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.functional.BeamRenderer;
import com.tacz.guns.client.render.scope.IReticleRenderer;
import com.tacz.guns.client.render.scope.ReticleRendererRegistry;
import com.tacz.guns.client.render.scope.ScopeBodyRenderTypes;
import com.tacz.guns.client.render.scope.ScopeMaskGeometry;
import com.tacz.guns.client.render.scope.ScopeMaskTextureHandle;
import com.tacz.guns.client.render.scope.ScopeNodeSet;
import com.tacz.guns.client.model.functional.TextShowRender;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.config.client.RenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BedrockAttachmentModel extends BedrockAnimatedModel {
    private static final String SCOPE_VIEW_NODE = "scope_view";
    private static final String DIVISION_NODE = "division";
    private static final String OCULAR_NODE = "ocular";
    private static final String OCULAR_SIGHT_NODE = "ocular_sight";
    private static final String OCULAR_SCOPE_NODE = "ocular_scope";
    private static final Pattern LASER_BEAM_PATTERN = Pattern.compile("^laser_beam(_(\\d+))?$");

    /**
     * 잘라내기를 시작하는 조준 진행도 기준값. 이보다 낮으면 전혀 잘라내지 않는다.
     *
     * <p>0이 아니라 아주 작은 양수를 쓴다: {@code aimingProgress}는 보간한 실수라서
     * 총을 집어넣은 뒤 0.001 같은 잔여값에 머물 수 있고, {@code > 0} 기준이면 조준경 몸체에
     * 거의 보이지 않지만 분명히 있는 구멍이 계속 남는다.
     */
    private static final float AIM_CLIP_START = 0.02f;

    /**
     * 조준경 글자가 나타나기 시작하는 조준 진행도. {@code IlluminatedReticleRenderer.FADE_IN_START}와
     * 같은 값을 써서 글자와 조준선이 함께 나타나 보기가 통일된다. 자세한 내용은 {@link #setTextShowList} 참고.
     */
    private static final float TEXT_SHOW_AIM_START = 0.35f;

    /**
     * 발광 조준선 노드. 이름이 {@code _illuminated}로 끝나는 노드는 모두
     * {@code BedrockModel} 생성 시 {@code illuminated=true}가 되어,
     * 스냅숏 단계에서 자동으로 최대 밝기(15728880)를 받는다.
     *
     * <p>하지만 모든 {@code *_illuminated}가 조준선은 아니다 — 레이저/손전등/렌즈 반사도 이 접미사를 쓴다.
     * 실제 기본 총기 팩에 나온 것: {@code division_illuminated}, {@code dot_illuminated},
     * {@code crosshair_illuminated}, {@code cross_illuminated}, {@code red_illuminated},
     * {@code sight_division_illuminated}, {@code scope_division_illuminated},
     * 그리고 {@code laser_illuminated} / {@code flashlight_illuminated} / {@code lens_illuminated}(조준선 <b>아님</b>).
     * 그래서 여기서는 허용 목록 방식으로 "눈금/점/십자" 어근만 인정한다.</p>
     */
    private static final Pattern RETICLE_ILLUMINATED_PATTERN = Pattern.compile(
            "^(.*_)?(division|divisions|dot|cross|crosshair|reticle|red)(_\\d+)?_illuminated\\d*$");

    /** 새긴 눈금 노드(발광 없음). P1에서는 그리지 않고 P2의 새김 전략에 맡긴다. */
    private static final Pattern RETICLE_ETCHED_PATTERN = Pattern.compile(
            "^(division|divisions)(_(\\d+))?$");

    protected List<List<BedrockPart>> scopeViewPaths;
    /** 22차: 조준선(눈금) 노드 모음. 생성할 때 한 번 해석해 IReticleRenderer가 쓴다. */
    protected ScopeNodeSet reticleNodes = ScopeNodeSet.empty();

    /**
     * 접안렌즈 번호 → 그 접안렌즈가 <b>망원경</b> 계통({@code ocular_scope*})인지.
     *
     * <h2>이름 접두사가 아니라 번호로 저장해야 하는 이유</h2>
     * 원본 {@code BedrockAttachmentModel}은 생성할 때 다음을 썼다
     * <pre>
     * TreeMap&lt;Integer, OcularWrapper&gt; map;
     * int num = matcher.group(3) == null ? 1 : parseInt(matcher.group(3));
     * map.put(num, new OcularWrapper(renderer, OCULAR_SCOPE_NODE.equals(type)));
     * </pre>
     * 즉 <b>{@code ocular_xxx_N}의 N이 순번</b>이고,
     * {@code isScopeOcular}는 그 순번에 붙은 불리언 표시일 뿐이다.
     *
     * <p>이어서 {@code renderOcularAndDivision}은 <b>같은 순번</b>으로
     * 접안렌즈와 눈금을 짝짓는다: {@code ocularNodePaths.get(i)} ↔ {@code divisionNodePaths.get(i)}.
     *
     * <h2>접두사로 묶으면 반드시 틀리는 이유</h2>
     * 예전 {@code isOcularInActiveGroup}/{@code filterReticleByActiveView}는
     * "{@code sight_} 접두사 = 도트 그룹(views 값 1), {@code scope_} 접두사 = 망원경 그룹(views 값 2)"이라고 가정했다.
     * 그런데 {@code scope_standard_8x}의 이름은 다음과 같다:
     * <pre>
     * ocular_scope     -> 접미사 없음, 순번 1
     * ocular_sight_2   -> 접미사 2, 순번 2
     * </pre>
     * 즉 <b>순번 1이 오히려 망원경이고 순번 2가 도트</b>로, hamr/vudu
     * ({@code ocular_sight} = 1, {@code ocular_scope_2} = 2)와 정반대다.
     * 접두사로 views 값에 대응시키면 이 모델에서는 반드시 반대로 고른다.
     *
     * <p>또 {@code scope_vudu}의 눈금 노드는 {@code division_illuminated} /
     * {@code division_2_illuminated}라서 {@code sight_}/{@code scope_} 접두사가 아예 없다 —
     * 예전 코드는 여기서 "전체"로 물러나 두 그룹의 조준선을 함께 그렸고, 이것이 사용자가 실제로 본 현상이다.
     * 순번을 쓰면 {@code division}(1) / {@code division_2}(2)가 자연히 짝지어져 물러나는 분기가 필요 없다.
     */
    protected final java.util.NavigableMap<Integer, BedrockPart> ocularByIndex = new java.util.TreeMap<>();
    /** 접안렌즈 순번 → 망원경 계통인지. {@link #ocularByIndex}와 키가 같다. */
    protected final java.util.Map<Integer, Boolean> ocularIsScopeByIndex = new java.util.HashMap<>();
    /** 눈금 순번 → 그 눈금 하위 트리의 루트 노드. 순번 의미는 {@link #ocularByIndex}와 같다. */
    protected final java.util.NavigableMap<Integer, BedrockPart> divisionByIndex = new java.util.TreeMap<>();
    /**
     * 접안렌즈 노드. 주 화면에는 <b>그리지 않지만</b>(생성할 때 visible=false),
     * 화면 투영으로 렌즈 안쪽 마스크를 만드는 데 쓴다 — 원본 stencil 잘라내기 영역의 출처가 바로 이것이다.
     */
    protected final List<BedrockPart> ocularParts = new ArrayList<>();
    protected @Nullable List<List<BedrockPart>> laserBeamPaths;

    private @Nullable ItemStack currentGunItem;
    private @Nullable ItemStack attachmentItem;

    private boolean isScope = false;
    private boolean isSight = false;

    public BedrockAttachmentModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);
        scopeViewPaths = new ArrayList<>();
        laserBeamPaths = new ArrayList<>();
        // view의 node path 초기화
        List<BedrockPart> path = getPath(modelMap.get(SCOPE_VIEW_NODE));
        int i = 2;
        while (path != null) {
            scopeViewPaths.add(path);
            path = getPath(modelMap.get(SCOPE_VIEW_NODE + '_' + i++));
        }
        // 접안렌즈(렌즈) 노드를 숨기고 레이저 빔 노드를 모은다.
        //
        // [접안렌즈를 한 픽셀도 그리면 안 되는 이유]
        // 원본 renderOcularStencil의 첫 줄은 다음과 같다:
        //     RenderSystem.colorMask(false, false, false, false);
        //     RenderSystem.depthMask(false);
        // 접안렌즈 형상의 <b>유일한</b> 용도는 스텐실 버퍼에 값을 써서 조준경 몸체가 어느 영역이 렌즈 안인지 알게 하는 것이고,
        // 형상 자신은 색도 깊이도 쓰지 않는다. 이식할 때 colorMask 줄을 주석 처리하는 바람에
        // (26.2에는 그 API가 없다) 보이지 않아야 할 형상이 불투명 렌즈로 그대로 그려졌다 —
        // 이것이 사용자가 보고한 "조준하면 렌즈가 가린다"는 현상이다.
        //
        // ["보이지 않는 RenderType"이 아니라 visible=false인 이유]
        // r52에서 ColorTargetState.WRITE_NONE 전용 파이프라인을 만들어 접안렌즈를 제출해,
        // "형상은 남기고 보이지만 않게" 하려 했다. 그때 실제로 다음 오류로 크래시가 났다
        //     IllegalStateException: Missing sampler Sampler0
        //         at VulkanRenderPass.pushDescriptors
        // — 26.2의 RenderSetup은 파이프라인이 선언한 모든 sampler에 텍스처를 묶어야 하는데,
        // 그 파이프라인은 ENTITY_SNIPPET 기반이라 Sampler0이 필요했지만 RenderSetup에는 하나도 묶지 않았다.
        //
        // 하지만 더 근본적인 문제는 <b>그 제출 자체가 불필요했다</b>는 점이다.
        // 26.2에는 스텐실 버퍼가 없으니 접안렌즈가 스텐실을 쓰는 유일한 용도가 사라졌고,
        // "색도 깊이도 쓰지 않는" 형상이 화면에 주는 영향은 정확히 0이다.
        // 그것을 제출하면 있지도 않은 소비자를 위해 정점과 파이프라인 비용만 치르는 셈이다.
        // 그래서 올바른 방법은 그 파이프라인을 고치는 것이 아니라 이 경로를 통째로 없애는 것이다.
        //
        // 세 가지 이름을 모두 모은다: ocular / ocular_sight / ocular_scope(복합 조준경은 그룹마다 하나씩).
        Pattern ocularPattern = Pattern.compile(
                "^(" + OCULAR_NODE + "|" + OCULAR_SIGHT_NODE + "|" + OCULAR_SCOPE_NODE + ")(_(\\d+))?$");
        for (Map.Entry<String, ModelRendererWrapper> entry : modelMap.entrySet()) {
            String name = entry.getKey();
            java.util.regex.Matcher ocularMatcher = name == null ? null : ocularPattern.matcher(name);
            if (ocularMatcher != null && ocularMatcher.matches()) {
                BedrockPart part = entry.getValue().getModelRenderer();
                if (part != null) {
                    // [원본 의미대로 순번 등록] 이름 끝의 _N이 순번이며, 접미사가 없으면 1로 본다.
                    // 원본 생성자의 TreeMap<Integer, OcularWrapper>와 글자 그대로 대응하며,
                    // 뒤에서 "접안렌즈 ↔ 눈금" 짝짓기와 "현재 렌즈 그룹" 판정의 유일한 근거다.
                    String numStr = ocularMatcher.group(3);
                    int num = numStr == null ? 1 : Integer.parseInt(numStr);
                    ocularByIndex.put(num, part);
                    ocularIsScopeByIndex.put(num, OCULAR_SCOPE_NODE.equals(ocularMatcher.group(1)));
                    // [등록만 하고 숨기지 않는다] — 접안렌즈는 [그려야 하는] 보이는 형상이다.
                    //
                    // 원본 renderOcularAndDivision의 두 줄이 아주 분명하다:
                    //     // 접안렌즈 검은 덮개 렌더링
                    //     stencilFunc(GL_EQUAL, i + 1);
                    //     renderTempPart(... ocularNodePaths.get(i));
                    // 접안렌즈는 [불투명한 검은 렌즈]이며 stencil로 원 안에 잘릴 뿐이다.
                    //
                    // 예전에는 여기에 part.visible = false라고 썼다(당시에는 원본이 "접안렌즈를 절대 그리지 않는다"고 생각했지만,
                    // 그 판단은 renderOcularStencil 단계에만 맞았다 — 그 단계는 스텐실만 쓰지만,
                    // 그 뒤에 따로 그리는 단계가 있다). 그 결과 접안렌즈가 [영원히 사라졌다]:
                    // 조준하지 않을 때 경통 안이 구멍이라 대물렌즈와 몸체 안쪽 벽이 그대로 보였다
                    // — 사용자가 실제로 본 첫 번째 문제다(elcan_4x / hamr 등).
                    //
                    // 지금은 정상 렌더링한다: 조준할 때는 마스크가 잘라내고, 조준하지 않을 때는 꽉 찬 렌즈다.
                    ocularParts.add(part);
                }
            }
            if (LASER_BEAM_PATTERN.matcher(name).find()) {
                laserBeamPaths.add(getPath(entry.getValue()));
            }
        }
        // division의 node path 초기화.
        //
        // 원본의 이 반복문은 두 가지를 함께 한다: division을 숨기고(주 렌더링 목록에 넣지 않음),
        // division, division_2, division_3… 순서로 divisionNodePaths에 넣는다.
        // 그 List의 인덱스 i는 ocularNodePaths의 인덱스 i와 하나씩 대응한다 —
        // 즉 division의 순번 규칙은 접안렌즈와 같다(접미사 없음 = 1).
        // 여기서는 조준선이 순번으로 접안렌즈와 짝지을 수 있도록 divisionByIndex에도 기록한다.
        ModelRendererWrapper divisionModel = modelMap.get(DIVISION_NODE);
        path = getPath(modelMap.get(DIVISION_NODE));
        i = 2;
        while (path != null) {
            divisionModel.setHidden(true);
            BedrockPart divisionPart = divisionModel.getModelRenderer();
            if (divisionPart != null) {
                divisionByIndex.put(i - 1, divisionPart);
            }
            divisionModel = modelMap.get(DIVISION_NODE + '_' + i++);
            path = getPath(divisionModel);
        }
        // 22차: IReticleRenderer 전략에 쓰도록 조준선(눈금) 노드 모음을 해석한다.
        this.reticleNodes = resolveReticleNodes();
    }

    /**
     * 모델 트리를 훑어 조준선 노드를 "발광"과 "새김" 두 종류로 나눈다.
     *
     * <h2>divisionNodePaths를 다시 쓰지 않고 따로 훑어야 하는 이유</h2>
     * 위의 초기화는 {@code division} 전체를 {@code setHidden(true)}했는데,
     * 실측(기본 총기 팩 조준경 33개) 결과 <b>{@code division_illuminated}는
     * {@code division}의 자식 노드</b>였다:
     * <pre>
     *   scope_acog_ta31:  division(5 cubes)  ← 검은 새김 선 + 차광판
     *                     └─ division_illuminated(1 cube)  ← 발광 세로선
     *   sight_exp3:       division(0 cubes)
     *                     └─ division_illuminated(1 cube)  ← 홀로그램 도트
     * </pre>
     * 그런데 스냅숏 순회기 {@code BedrockRenderSnapshot#capturePart}는
     * {@code visible == false}를 만나면 <b>바로 return하고 자식 노드도 훑지 않는다</b>.
     * 그래서 부모가 숨겨지면 발광 조준선도 영원히 그려지지 않는다 —
     * 이것이 "렌즈를 비운 뒤 아무것도 보이지 않는" 직접적인 원인이었다.
     *
     * <p>그래서 여기서는 <b>부모·자식 관계를 건너뛰고</b> 발광 노드를 따로 모아,
     * {@code IlluminatedReticleRenderer}가 submit 단계에서 잠시 보이게 바꿔 따로 제출한다.</p>
     */
    private ScopeNodeSet resolveReticleNodes() {
        List<BedrockPart> illuminated = new ArrayList<>();
        List<BedrockPart> etched = new ArrayList<>();
        for (Map.Entry<String, ModelRendererWrapper> entry : modelMap.entrySet()) {
            String name = entry.getKey();
            if (name == null) {
                continue;
            }
            BedrockPart part = entry.getValue().getModelRenderer();
            if (part == null) {
                continue;
            }
            if (RETICLE_ILLUMINATED_PATTERN.matcher(name).matches()) {
                illuminated.add(part);
            } else if (RETICLE_ETCHED_PATTERN.matcher(name).matches()) {
                etched.add(part);
            }
        }
        if (illuminated.isEmpty() && etched.isEmpty()) {
            return ScopeNodeSet.empty();
        }
        return new ScopeNodeSet(etched, illuminated);
    }

    /**
     * [35차] 복합 조준경(both)에서 지금 쓰는 렌즈 그룹: {@code 1} = 도트/홀로그램 계통,
     * {@code 2} = 망원경 계통. 복합 조준경이 아니면 항상 {@code 0}("해당 없음, 거르지 않음").
     *
     * <p>{@code FirstPersonRenderGunEvent}가 위치를 계산하면서 함께 쓴다 —
     * 그곳은 원래 {@code views[zoomNumber]}로 {@code scope_view} 위치 그룹을 고르므로,
     * 전체 흐름에서 "지금 어느 그룹을 쓰는지" 아는 유일한 곳이다.</p>
     */
    private int activeViewGroup = 0;

    /** 렌더링 이벤트가 매 프레임 위치 계산 단계에서 쓴다. {@link #activeViewGroup} 참고. */
    public void setActiveViewGroup(int group) {
        this.activeViewGroup = group;
    }

    /**
     * 지금 쓰는 렌즈 그룹에 맞춰 조준선 노드를 거른다.
     *
     * <p><b>복합 조준경</b>에만 적용한다: 한 가지 형태의 조준경(도트만 또는 망원경만)에는 조준선이 두 벌 없으므로
     * 그대로 돌려주며, 비용도 동작 변화도 없다.</p>
     *
     * <p>이름 기준(기본 총기 팩의 both형 모델 3개가 모두 따름):
     * 노드 이름이 {@code sight_}로 시작하면 도트 그룹, {@code scope_}로 시작하면 망원경 그룹이다.
     * <b>접두사가 없는 노드(예: {@code division_illuminated}, {@code dot_illuminated})는
     * 모두 남긴다</b> — 예를 들어 {@code scope_vudu}는 {@code division_illuminated} /
     * {@code division_2_illuminated}처럼 접두사 없는 이름을 써서 접두사로 묶을 수 없으므로,
     * 이때는 잘못 지워 빈 조준선이 되느니 모두 그린다(현상 유지).</p>
     */
    private ScopeNodeSet filterReticleByActiveView(ScopeNodeSet all) {
        if (!(isScope && isSight) || activeViewGroup == 0) {
            return all;
        }
        Integer activeIndex = activeOcularIndex();
        if (activeIndex == null) {
            return all;
        }
        BedrockPart activeDivision = divisionByIndex.get(activeIndex);
        if (activeDivision == null) {
            // 이 모델의 눈금은 순번별 그룹이 없어 판단할 수 없다 — 현상 유지(모두 그림),
            // 접안렌즈 쪽의 "판단할 수 없으면 남긴다"와 같은 원칙이다.
            return all;
        }
        // [현재 순번의 division 하위 트리]에 달린 조준선 노드만 남긴다.
        //
        // 이 기준이 예전 이름 접두사 비교를 대신한다. 접두사 방식은 scope_vudu에서 바로 실패했다
        // (조준선 이름이 division_illuminated / division_2_illuminated로 접두사가 없다).
        // 그래서 예전 코드는 전체로 물러나 두 그룹 조준선을 함께 그렸다 — 사용자가 실제로 본 현상이다.
        // 하위 트리 소속으로 판단하면 모든 복합 조준경에 맞는다. 눈금 트리 자체가 그룹별로 나뉘어 있기 때문이다:
        //   division   -> sight_division_illuminated / division_2_illuminated
        //   division_2 -> scope_division_illuminated / division_illuminated
        List<BedrockPart> illuminated = filterByAncestor(all.illuminatedReticle(), activeDivision);
        List<BedrockPart> etched = filterByAncestor(all.etchedReticle(), activeDivision);
        if (illuminated.isEmpty() && etched.isEmpty()) {
            return all;
        }
        return new ScopeNodeSet(etched, illuminated);
    }

    /**
     * 지금 쓰는 렌즈 그룹에 해당하는 <b>접안렌즈 순번</b>.
     *
     * <p>{@code activeViewGroup}은 display json의 {@code views[]}에서 오며,
     * {@code 1} = 도트 계통, {@code 2} = 망원경 계통으로 약속되어 있다. 여기서 이를
     * 이 모델 안의 접안렌즈 순번으로 바꾼다 — 두 값을 <b>같다고 볼 수 없다</b>:
     * {@code scope_standard_8x}는 망원경이 순번 1({@code ocular_scope}),
     * 도트가 순번 2({@code ocular_sight_2})로 hamr/vudu와 정반대다.
     * 그래서 생성할 때 만든 {@link #ocularIsScopeByIndex} 표를 찾아야 하며,
     * "순번 = views 값"이나 "접두사 = views 값"으로 가정하면 안 된다.
     *
     * @return 맞는 접안렌즈 순번. 찾지 못하면(복합 조준경이 아니거나 이름에 그룹 정보가 없으면) {@code null}
     */
    @Nullable
    private Integer activeOcularIndex() {
        boolean wantScope = activeViewGroup == 2;
        for (Map.Entry<Integer, Boolean> entry : ocularIsScopeByIndex.entrySet()) {
            if (entry.getValue() == wantScope) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** {@code part} 자신이나 조상 중 하나가 {@code ancestor}인지. */
    private static boolean hasAncestor(BedrockPart part, BedrockPart ancestor) {
        for (BedrockPart p = part; p != null; p = p.getParent()) {
            if (p == ancestor) {
                return true;
            }
        }
        return false;
    }

    private static List<BedrockPart> filterByAncestor(List<BedrockPart> src, BedrockPart ancestor) {
        List<BedrockPart> out = new ArrayList<>();
        for (BedrockPart part : src) {
            if (hasAncestor(part, ancestor)) {
                out.add(part);
            }
        }
        return out;
    }

    /**
     * 16차: 현재 조준 진행도(0 = 전혀 조준하지 않음, 1 = 완전히 조준함).
     *
     * <p>접안렌즈의 <b>불투명 검은 덮개</b>를 그릴지 정하는 데 쓴다. 원본 1.21.1은 stencil로 이 덮개를
     * 잘라냈지만, 26.2에서는 stencil이 제거되어(9·10차에서 항목별로 확인) 덮개가 그대로 그려졌다 —
     * "렌즈가 항상 검은 텍스처"로 보였다.
     *
     * <p>TACZ 공식 홍보 이미지를 보면 <b>조준하지 않을 때는 공식 모드도 렌즈를 그리지 않고</b>,
     * 조준경 틀 안으로 배경이 그대로 비친다. 그래서 stencil이 없는 지금은
     * "조준하지 않으면 덮개를 그리지 않는" 것이 공식 모습에 가깝고 가장 합리적인 대체 전략이다.
     */
    private static float currentAimingProgress() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0f;
        }
        return IClientPlayerGunOperator.fromLocalPlayer(player)
                .getClientAimingProgress(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }



    @Nullable
    public List<BedrockPart> getScopeViewPath(int viewSwitchCount) {
        if (scopeViewPaths.isEmpty()) {
            return null;
        }
        if (viewSwitchCount >= scopeViewPaths.size()) {
            return scopeViewPaths.get(0);
        }
        return scopeViewPaths.get(viewSwitchCount);
    }

    public void setIsScope(boolean isScope) {
        this.isScope = isScope;
    }

    public void setIsSight(boolean isSight) {
        this.isSight = isSight;
    }

    public boolean isScope() {
        return isScope;
    }

    public boolean isSight() {
        return isSight;
    }

    /**
     * 총기 사용자 정의 글자 표시를 추가한다.
     *
     * <h2>여기서 조준 진행도를 판단하는 이유</h2>
     * 조준경 위 글자(MK5HD의 탄약 수와 "AMMO" 표시 등)는
     * {@code SubmitNodeCollector#submitText}의 <b>바닐라 글꼴 파이프라인</b>을 거쳐서,
     * 조준경 몸체·조준선용으로 만든 {@code scope_body.fsh}
     * ({@code entityCutout}의 변형으로 {@code SCOPE_MASK_INVERT}
     * 마스크를 샘플링해 discard)를 쓸 수 없다. 즉 <b>렌즈 안으로 잘라 넣을 수 없다</b>.
     *
     * <p>실측하면 MK5HD의 글자 노드 두 개는 월드 좌표 {@code y=22.375}에 있고,
     * 망원경 접안렌즈 {@code ocular_scope_2}는 {@code y=21.875}에 있다 —
     * 글자가 렌즈 중심보다 0.5 높고 X로 0.75 왼쪽이라 렌즈 가장자리 근처에 놓여,
     * 조준하면 원 밖으로 삐져나온다(사용자 실측: "글자가 조준선처럼 렌즈 안에만 나오지 않고 넘친다").
     *
     * <h2>원본의 동작</h2>
     * 원본 {@code renderScope}의 순서는
     * <pre>
     * stencilFunc(GL_ALWAYS, 0);          // 먼저 잘라내기를 [끈다]
     * disableItemEntityStencilTest();
     * super.render(...);                  // 글자는 여기서 그려진다
     * </pre>
     * 이다. 즉 <b>원본도 이 글자를 잘라내지 않는다</b>. 그래서 엄밀히는 이식 결함이 아니지만,
     * 원본은 stencil로 원과 몸체가 딱 맞아 넘침이 잘 보이지 않았고,
     * 우리 마스크는 화면 공간이라 경계가 더 "딱딱해서" 조금만 나와도 눈에 거슬린다.
     *
     * <h2>방법</h2>
     * 조준선과 맞춰 <b>조준할 때만 표시</b>한다.
     * {@link IlluminatedReticleRenderer}의 {@code FADE_IN_START = 0.35} 기준을 다시 쓴다 —
     * 조준하지 않으면 렌즈 안이 원래 보이지 않으니 글자도 나오지 않는 것이 자연스럽고,
     * 조준하면 시선이 광축에 맞아 글자가 원 안에 들어와 넘치지 않는다.
     *
     * <p>이것은 <b>보수적인 방법</b>이다: 글꼴 파이프라인도 마스크 경로도 건드리지 않고
     * 제출 전에 조건 하나만 더한다. 대신 지향 사격 중에는 조준경의 탄약 수를 볼 수 없지만 —
     * 원래 "조준경에 눈을 대야 잘 보이는" 정보이므로 직관에 맞는다.
     *
     * <p><b>조준경</b>에만 적용한다는 점에 주의한다: {@code BedrockGunModel}의 같은 이름 메서드에는 이 조건을 넣지 않는다.
     * 총몸의 글자(탄창 수 등)는 항상 보여야 하기 때문이다.
     */
    public void setTextShowList(Map<String, TextShow> textShowList) {
        textShowList.forEach((name, textShow) -> this.setFunctionalRenderer(name,
                bedrockPart -> {
                    // 조준하지 않았거나 막 조준하기 시작했으면 제출하지 않아 글자가 렌즈 밖으로 넘치지 않게 한다.
                    if (currentAimingProgress() <= TEXT_SHOW_AIM_START) {
                        return null;
                    }
                    return new TextShowRender(this, textShow, currentGunItem);
                }));
    }


    /**
     * 호환 오버로드: 텍스처 없음. 이때는 <b>렌즈 안 잘라내기를 하지 않으며</b>, 동작이 Step 2 이전과 같다.
     *
     * <p>인벤토리 미리 보기({@code AttachmentItemRenderer}) 같은 경우에 쓴다 —
     * 그런 경우는 1인칭 조준이 아니므로 잘라낼 필요가 없다.
     */
    public void submit(@Nullable ItemStack attachmentItem,
                       ItemStack currentGunItem,
                       PoseStack poseStack,
                       ItemDisplayContext transformType,
                       SubmitNodeCollector collector,
                       RenderType renderType,
                       int light,
                       int overlay) {
        submit(attachmentItem, currentGunItem, poseStack, transformType, collector,
                renderType, (Identifier) null, light, overlay);
    }

    /**
     * 백엔드에 의존하지 않는 collector 경로. 고급 조준경 stencil 동작은 전용 조준경/PIP 단계 전까지
     * 일반 모델 형상으로 의도적으로 낮춰 처리한다.
     *
     * @param texture 이 조준경의 텍스처. 넘겨야 몸체가 "접안렌즈 마스크 잘라내기"를 쓸 수 있다.
     *                {@code null}이면 호출하는 쪽이 잘라내기에 관심이 없다는 뜻이며 항상 원래 RenderType을 쓴다.
     */
    public void submit(@Nullable ItemStack attachmentItem,
                       ItemStack currentGunItem,
                       PoseStack poseStack,
                       ItemDisplayContext transformType,
                       SubmitNodeCollector collector,
                       RenderType renderType,
                       @Nullable Identifier texture,
                       int light,
                       int overlay) {
        this.currentGunItem = currentGunItem;
        this.attachmentItem = attachmentItem;

        // [45차 재구성] 예전에는 여기에 "visible을 바꿔 stencil을 흉내 내는" 로직이 두 개 있었는데 모두 지웠다:
        //   ① 접안렌즈 덮개(shouldDrawOcularMask) — 조준 진행도에 따라 ocular를 그릴지 정함
        //   ② 몸체 제거(cullScopeBody) — 조준할 때 scope_body 전체를 숨김
        //
        // 지운 근거(원본 1.21.1을 줄마다 대조해 확인. SCOPE_UPSTREAM_TRUTH_2026-07-27.md 참고):
        //
        // <b>원본에는 "검은 접안렌즈를 그리는" 일이 처음부터 없었다.</b>
        // renderOcularStencil의 첫 줄이 colorMask(false,false,false,false)다 —
        // 접안렌즈는 <b>스텐실 값만 쓰고 색은 전혀 쓰지 않는다</b>. 그때 colorMask를 주석 처리한 뒤로
        // 보이지 않아야 할 형상이 검은 조각으로 그대로 그려졌고, 그것을 메우려고 다시 "덮개 스위치"를 더했다.
        // 그 보완 자체가 원본을 잘못 읽은 데서 나온 것이었다.
        //
        // <b>"몸체 제거"도 마찬가지로 stencil 의미에 오도되어 나온 개념이다.</b>
        // 원본 renderScope 4단계 stencilFunc(EQUAL,0)의 의미는
        // "몸체는 접안렌즈 원 [바깥]에만 그린다"는 것이다 — <b>화면 공간을 둘로 나누는</b> 일이지
        // "몸체를 통째로 지우는" 것이 아니다. 우리에게는 stencil이 없어 전역 불리언 스위치로 떨어졌고,
        // 그 결과 도트/복합 조준경까지 피해를 입었다(r34, r35 두 번 모두 이 잘못된 개념에 덧대기만 했다).
        //
        // 두 로직의 공통 전제(stencil 영역 잘라내기)가 26.2에 없으므로,
        // 올바른 방법은 계속 값을 조정하는 것이 아니라 <b>둘을 없애는 것</b>이다:
        // 모든 형상을 모델 그대로 제출하고 super.submit이 한꺼번에 처리하게 한다.
        //
        // 비용과 이득:
        //   - 비용: 렌즈 안에서 경통 안쪽 벽이 보인다(영역 잘라내기 능력이 없다. 26.2의 고정 제약).
        //   - 이득: "형태별 표시 스위치"가 더는 없고, 도트/망원경/복합 조준경이
        //           <b>완전히 같은</b> 경로를 타므로 내장 조준경에 문제가 생겨도 숨을 우회로가 없다.
        //
        // 진짜 해법은 화면 공간 원형 잘라내기(shader discard)이며, 매개변수는
        // SCOPE_UPSTREAM_TRUTH §6.3에 적어 두었지만 원 중심 좌표 계산법을 먼저 따로 검증해야 한다 — 아직 하지 않았다.

        // [Step 2] 단계 경계의 마스크 pass가 쓰도록 접안렌즈 형상을 등록한다.
        //
        // 1인칭에서만 등록한다: 렌즈 안 잘라내기는 조준하는 본인에게만 의미가 있고,
        // 3인칭으로 남의 총을 볼 때는 필요 없다("렌즈 안"이라는 것 자체가 없다).
        //
        // [순서 요구] 반드시 super.submit [전]이어야 한다 —
        // 아래에서 RenderType을 고를 때 "이번 프레임에 접안렌즈가 등록됐는지"를 보고 잘라내기 판을 쓸지 정한다.
        //
        // [조준 조건] 실제로 조준할 때만 잘라낸다. 조준하지 않을 때 몸체는 온전해야 한다 —
        // 아니면 지향 사격 중 조준경 가운데에 구멍이 나는데, 이는 원본 동작이 아니다.
        // 원본도 잘라내기 반지름에 aimingProgress를 곱한다(renderOcularAndDivision:
        // `rad *= getClientAimingProgress(...)`). 진행도가 0이면 반지름도 0 = 잘라내지 않음.
        boolean maskable = transformType != null && transformType.firstPerson()
                && !ocularParts.isEmpty()
                && currentAimingProgress() > AIM_CLIP_START;
        if (maskable) {
            registerOcularMaskGeometry(poseStack);
        }

        // 접안렌즈(ocular*)가 super.submit에 들어갈 때 원본 규칙대로 <b>검은 조각을 그릴지</b> 정해야 한다.
        //
        // 원본은 renderOcularAndDivision에서만 접안렌즈를 그리고, 순수 도트 조준경(renderSight)은
        // 그것을 아예 부르지 않으며, 복합 조준경(selective=true)도 망원경 그룹에만 그린다.
        // 자세한 내용은 shouldDrawOcularBlackout 주석 참고.
        //
        // 그리면 안 되는 접안렌즈를 잠시 visible=false로 두고, submit이 끝나면 finally에서 되돌린다 —
        // BedrockPart는 여러 프레임이 함께 쓰는 객체라 되돌리지 않으면 3인칭과 인벤토리 미리 보기까지 망가진다.
        //
        // [Step 3] 몸체에 "접안렌즈 마스크로 잘리는" RenderType을 쓴다.
        // 원본 scope_body의 stencilFunc(GL_EQUAL, 0)을 재현한다: 접안렌즈가 덮지 않은 곳에만 몸체를 그린다.
        //
        // 하나라도 조건이 맞지 않으면 원래 renderType(= RenderTypes.entityCutout)으로 돌아간다.
        // 즉 이미 PASS한 동작이다. 최악이라도 "렌즈 안에 경통 안쪽 벽이 보이는" 정도이고 더 나빠지지 않는다.
        List<BedrockPart> hiddenOculars = new ArrayList<>();
        if (transformType != null && transformType.firstPerson()) {
            for (BedrockPart ocular : ocularParts) {
                if (ocular.visible && !shouldDrawOcularBlackout(ocular)) {
                    ocular.visible = false;
                    hiddenOculars.add(ocular);
                }
            }
        }
        try {
            super.submit(poseStack, transformType, collector,
                    resolveBodyRenderType(renderType, texture, maskable), light, overlay);
        } finally {
            for (BedrockPart ocular : hiddenOculars) {
                ocular.visible = true;
            }
        }

        if (transformType != null && transformType.firstPerson() && !reticleNodes.isEmpty()) {
            ScopeNodeSet active = filterReticleByActiveView(reticleNodes);
            IReticleRenderer reticle = ReticleRendererRegistry.select(active);
            if (reticle != null && !active.isEmpty()) {
                // 조준선은 [반대 잘라내기] 판을 쓴다: 접안렌즈가 덮은 곳에만 그린다.
                //
                // 원본 renderDivisionOnly와 같은 역할이다:
                //     stencilFunc(GL_EQUAL, i + 1)   // 스텐실 값 i+1 = i번째 접안렌즈의 투영 영역
                // 즉 조준선은 [접안렌즈 투영 안으로 묶인다].
                //
                // 이 묶음이 없으면 조준선이 경통 밖으로 넘쳐 스티커처럼 화면에 붙고,
                // 조준경 틀과 함께 커지지 않는다 — 사용자가 실제로 본 두 번째 문제다.
                //
                // 방향을 헷갈리지 않는다: 몸체는 "덮이면 discard", 조준선은 "안 덮이면 discard"다.
                // 둘은 같은 마스크와 같은 shader를 쓰며 SCOPE_MASK_INVERT로 구분한다.
                RenderType reticleType = resolveReticleRenderType(renderType, texture, maskable);
                // maskActive: 이번 프레임에 조준선이 실제로 반대 잘라내기를 탔는지.
                // EtchedReticleRenderer가 이를 보고 division(큰 차광판 포함)을 그려도 되는지 정한다.
                boolean maskActive = reticleType != renderType;
                reticle.submitReticle(new IReticleRenderer.Context(
                        poseStack, collector, transformType, reticleType,
                        light, overlay, currentAimingProgress(), maskActive), active);
            }
        }

        if (laserBeamPaths != null) {
            for (var entry : laserBeamPaths) {
                BeamRenderer.renderLaserBeam(attachmentItem, poseStack, transformType, entry, collector);
            }
        }
    }

    /**
     * 단계 경계의 마스크 pass가 그리도록 접안렌즈 형상을 {@link ScopeMaskGeometry}에 등록한다.
     *
     * <h2>부모 사슬을 직접 따라가야 하는 이유</h2>
     * 접안렌즈는 생성할 때 {@code visible = false}가 되어 주 렌더링 경로가 아예 지나가지 않으므로
     * 이미 계산된 행렬을 얻을 수 없다. 그래서 여기서
     * {@code BedrockRenderSnapshot#captureSubtree}의 호출 방식을 재현한다:
     * 아래에서 위로 조상 사슬을 모은 뒤 → 위에서 아래로 하나씩 {@code translateAndRotateAndScale}해,
     * 주 렌더링과 <b>완전히 같은</b> 모델 행렬을 얻는다.
     *
     * <p>{@code IlluminatedReticleRenderer#submitOne}과 구조가 같다 — 그 경로는
     * 여러 차례 실측으로 검증되었으므로(조준선 위치 정확) 같은 방식을 쓰고 새로 만들지 않는다.
     *
     * <h2>visible을 바꾸지 않는 이유</h2>
     * 조준선 쪽에서 {@code visible}을 잠시 켜는 것은
     * {@code captureSubtree}를 타는데 순회기가 {@code visible=false}를 만나면 바로 return하기 때문이다.
     * 여기서는 <b>순회기를 타지 않고</b> {@code part.cubes}를 바로 꺼내므로
     * {@code visible}을 건드릴 필요가 없다 — 여러 프레임이 공유하는 상태를 바꾸는 곳이 하나 줄면 위험도 하나 준다.
     */
    private void registerOcularMaskGeometry(PoseStack poseStack) {
        for (BedrockPart ocular : ocularParts) {
            if (!isOcularInActiveGroup(ocular)) {
                // 복합 조준경: [지금 쓰는] 그룹의 접안렌즈만 마스크에 넣는다.
                // 아니면 다른 그룹의 렌즈도 몸체를 뚫는다 — 저배율 도트를 쓸 때
                // 고배율 망원경 둘레가 이유 없이 투명해진다(사용자가 실제로 본 세 번째 문제).
                continue;
            }
            java.util.Deque<BedrockPart> chain = new java.util.ArrayDeque<>();
            for (BedrockPart p = ocular; p != null; p = p.getParent()) {
                chain.push(p);
            }
            poseStack.pushPose();
            try {
                // 먼저 조상 사슬(ocular 자신 제외)을 적용한 뒤 재귀에 넘긴다 — 재귀는 각
                // 노드(ocular 포함)에 스스로 변환을 한 번씩 적용하며, BedrockPart#render 구조와 같다.
                for (BedrockPart p : chain) {
                    if (p != ocular) {
                        p.translateAndRotateAndScale(poseStack);
                    }
                }
                // [여기서 조준 전환을 처리하지 않는다] —
                // 마스크 형상은 항상 접안렌즈의 실제 자세로 등록하고, 전환은 [화면 공간]에서 처리한다
                // (ScopeMaskRenderer의 maskProgress와 scope_body.fsh 참고).
                //
                // 예전에는 여기서 진행도에 따라 3D 형상을 키웠는데, 실제로는 렌즈 영역이 화면 밖에서
                // "날아" 들어왔고 제자리에서 작게 시작해 커지지 않았다. 원인: 원근 투영에서 3D 물체를 키우면
                // 크기뿐 아니라 투영 [위치]도 바뀐다.
                //
                // 원본은 이렇게 하지 않았다 — 원 중심은 접안렌즈 투영 중심에 고정하고 반지름만 진행도에 따라 키웠다:
                //     centerX/centerY = getBedrockPartCenter(...)   // 고정
                //     rad = 80 * modifier * aimingProgress          // 반지름만 바뀜
                // 그것은 순수한 2차원 연산이다. 우리 쪽에서 대응하는 2차원 연산은 shader에서
                // 화면 거리에 따라 마스크를 줄이는 것이며, 형상 자체는 움직이지 않는다.
                collectMaskGeometry(ocular, poseStack);
            } finally {
                poseStack.popPose();
            }
        }
    }

    /**
     * 몸체에 쓸 RenderType을 정한다: 마스크로 잘리는 판인지, 원래 그대로인지.
     *
     * <h2>조건이 이렇게 많은 이유</h2>
     * 이 경로의 어느 단계에서 문제가 생겨도 최악은 "렌즈 안에 경통 안쪽 벽이 보이는" 상태로 돌아가는 것뿐이다 —
     * 그것은 <b>이미 PASS한 상태</b>다. 잘라내기 기능이 고장 났다고 조준경 전체를 못 쓰게 해서는 안 된다.
     * 그래서 모든 조건은 예외를 던지지 않고 "맞지 않으면 되돌아간다".
     *
     * @param original 호출하는 쪽이 준 원래 RenderType({@code RenderTypes.entityCutout(텍스처)})
     * @param texture  이 조준경의 텍스처. {@code null}이면 같은 잘라내기 판을 만들 수 없어 바로 되돌아간다
     * @param maskable 이번 프레임에 접안렌즈 형상을 등록했는지(1인칭 + 이 조준경에 접안렌즈가 실제로 있음)
     */
    private RenderType resolveBodyRenderType(RenderType original,
                                             @Nullable Identifier texture,
                                             boolean maskable) {
        if (!maskable) {
            // 3인칭이거나 이 부착물에 접안렌즈가 아예 없음(손잡이, 소음기 등) — 잘라낼 필요가 없다.
            return original;
        }
        if (texture == null) {
            // 텍스처를 얻지 못하면 같은 잘라내기 RenderType을 만들 수 없다. 잘라내지 않을지언정 틀린 텍스처를 그리면 안 된다.
            return original;
        }
        if (!RenderConfig.SCOPE_MASK_ENABLE.get()) {
            // 기능 전체 스위치. Step 3 동안 디버그 스위치에 묶여 기본으로 꺼져 있었다:
            // 호환성 문제가 생기면 플레이어가 스위치를 꺼서 버전을 되돌리지 않고도 이미 동작이 확인된 상태로 돌아갈 수 있다.
            return original;
        }
        // shader가 샘플링하도록 마스크 target의 텍스처를 TextureManager에 붙인다.
        // 마스크를 그리지 못했으면(target 생성 실패 / 그리기 실패) false를 돌려주며, 이때는 반드시 되돌아가야 한다.
        // 아니면 shader가 오래되었거나 잘못된 텍스처를 샘플링한다.
        if (!ScopeMaskTextureHandle.syncToMaskTarget()) {
            return original;
        }
        return ScopeBodyRenderTypes.clipped(texture);
    }

    /**
     * 조준선에 쓸 RenderType을 정한다: 렌즈 안으로 묶이는 판인지, 원래 그대로인지.
     *
     * <p>조건은 {@link #resolveBodyRenderType}과 <b>완전히 같고</b>, 마지막에
     * 반대 잘라내기 판으로 바꿀 뿐이다. 둘은 함께 움직여야 한다: 몸체만 잘리고 조준선이 안 잘리면
     * 조준선이 렌즈 밖에 떠 있고, 조준선만 잘리고 몸체가 안 잘리면 렌즈 안이 몸체로 덮인다.
     */
    private RenderType resolveReticleRenderType(RenderType original,
                                                @Nullable Identifier texture,
                                                boolean maskable) {
        if (!maskable || texture == null || !RenderConfig.SCOPE_MASK_ENABLE.get()) {
            return original;
        }
        if (!ScopeMaskTextureHandle.syncToMaskTarget()) {
            return original;
        }
        return ScopeBodyRenderTypes.reticle(texture);
    }

    /**
     * 이 접안렌즈가 <b>지금 쓰는</b> 렌즈 그룹에 속하는지.
     *
     * <h2>필요한 이유</h2>
     * 복합 조준경(예: {@code scope_hamr}, {@code scope_mk5hd})에는 접안렌즈가 두 개 있다:
     * {@code ocular_sight}(도트 계통)와 {@code ocular_scope}(망원경 계통).
     * 하지만 플레이어는 한 번에 한 그룹만 쓴다.
     *
     * <p>두 그룹의 접안렌즈를 모두 마스크에 넣으면 쓰지 않는 그룹도 몸체를 뚫는다 —
     * "일부 조준경의 효과가 일부 빠진" 것처럼 보인다(사용자 실측: 저배율 도트 상태에서
     * 고배율 망원경 둘레의 몸체가 이유 없이 투명해짐).
     *
     * <h2>판정 기준의 신뢰성</h2>
     * 실측 결과 기본 총기 팩의 복합 조준경 4개 접안렌즈 이름은 <b>모두</b> 접두사가 있다:
     * <pre>
     * scope_hamr         ocular_sight / ocular_scope_2
     * scope_mk5hd        ocular_sight / ocular_scope_2
     * scope_standard_8x  ocular_sight_2 / ocular_scope
     * scope_vudu         ocular_sight / ocular_scope_2
     * </pre>
     * 이 점은 조준선 쪽보다 훨씬 믿을 만하다 — 조준선에는 {@code scope_vudu}처럼 접두사 없는 이름
     * ({@code division_illuminated})이 있어 전체로 물러날 수밖에 없지만, 접안렌즈에는 그런 문제가 없다.
     *
     * <p>{@code views[]} 값 약속({@code FirstPersonRenderGunEvent}가 씀):
     * {@code 1} = 도트 그룹, {@code 2} = 망원경 그룹이며 {@code sight}/{@code scope}
     * 접두사와 하나씩 대응한다(display json 실측: hamr/vudu 모두 {@code views=[2,1]}).
     */
    private boolean isOcularInActiveGroup(BedrockPart ocular) {
        if (activeViewGroup == 0 || !(isScope && isSight)) {
            // 복합 조준경이 아님(한 가지 형태) — 그룹 문제가 없으므로 모두 포함한다.
            return true;
        }
        Integer activeIndex = activeOcularIndex();
        if (activeIndex == null) {
            // 이 모델의 접안렌즈에는 그룹 정보가 없다 — 판단할 수 없으므로 남긴다.
            // 조준선 거르기와 같은 원칙: 잘못 지워 빈 마스크가 되느니 더 그린다.
            return true;
        }
        return ocularByIndex.get(activeIndex) == ocular;
    }

    /**
     * 지금 접안렌즈를 <b>불투명 검은 렌즈</b>로 그려야 하는지.
     *
     * <h2>원본 규칙(renderSight / renderScope / renderBoth를 줄마다 대조)</h2>
     * "검은 조각 그리기" 단계는 <b>{@code renderOcularAndDivision} 안에만 있고</b>,
     * 세 렌더링 경로가 이를 호출하는 방식은 다음과 같다:
     * <pre>
     * renderScope (순수 망원경)   -> renderOcularAndDivision(selective=false)  검은 조각 그림
     * renderBoth  (복합)          -> renderOcularAndDivision(selective=true)   [망원경 그룹]만 검은 조각 그림
     * renderSight (순수 도트)     -> [아예 호출하지 않음]                      검은 조각을 절대 그리지 않음
     * </pre>
     * {@code selective=true} 분기의 if/else가 분명히 보여 준다:
     * <pre>
     * if (selective &amp;&amp; !isScopeOcular.get(i)) {
     *     // 망원경이 아닌 그룹: 눈금만 그리고 [접안렌즈 검은 조각은 건너뜀]
     *     renderTempPart(... divisionNodePaths.get(i));
     * } else {
     *     // 접안렌즈 검은 덮개 렌더링
     *     renderTempPart(... ocularNodePaths.get(i));
     *     ...
     * }
     * </pre>
     *
     * <h2>이것이 바로 사용자가 보고한 세 번째 문제인 이유</h2>
     * 실제 도트/홀로그램 조준경의 렌즈는 <b>빛이 통과하므로</b> 검은 바탕이 있으면 안 된다.
     * 고배율 망원경만 광로가 막혀 있어 광축에 맞추지 않으면 검게 보인다.
     * 예전에는 모든 {@code ocular*}를 구분 없이 {@code super.submit}에 넘겨 정상 렌더링해서,
     * 저배율 도트 조준경({@code sight_*})과 복합 조준경의 도트 그룹이 <b>조준하지 않을 때</b>
     * 있으면 안 될 검은 덮개를 쓰고 있었다.
     *
     * <p>판단 기준은 "조준경 전체가 scope인지"가 아니라 <b>그 접안렌즈가 망원경 계통인지</b>라는 점에 주의한다:
     * 복합 조준경의 두 접안렌즈는 따로 판단하며, 도트 쪽은 절대 검은 조각을 그리지 않는다.
     */
    private boolean shouldDrawOcularBlackout(BedrockPart ocular) {
        if (isSight && !isScope) {
            // 순수 도트/홀로그램 조준경: 원본 renderSight는 검은 조각을 절대 그리지 않는다.
            return false;
        }
        if (!(isScope && isSight)) {
            // 순수 망원경: renderScope는 selective=false로 모두 검은 조각을 그린다.
            return true;
        }
        // 복합 조준경: 망원경 계통 접안렌즈만 검은 조각을 그린다.
        for (Map.Entry<Integer, BedrockPart> entry : ocularByIndex.entrySet()) {
            if (entry.getValue() == ocular) {
                return Boolean.TRUE.equals(ocularIsScopeByIndex.get(entry.getKey()));
            }
        }
        return true;
    }

    /**
     * 노드 하나와 그 <b>모든 자식 노드</b>의 육면체를 재귀로 모은다.
     *
     * <h2>재귀가 꼭 필요한 이유(첫 판이 여기서 넘어졌다)</h2>
     * {@code BedrockModel#loadNewModel}에는 놓치기 쉬운 분기가 있다:
     * <b>{@code rotation}이 있는 cube는 {@code bone.cubes}에 들어가지 않고</b>,
     * 새로 만든 {@code BedrockPart}로 감싸여 <b>자식 노드</b>로 붙는다
     * (cube 하나가 스스로 돌려면 자기 변환 원점이 있어야 하기 때문이다).
     *
     * <p>기본 총기 팩 실측: 접안렌즈 육면체 161개 중 <b>101개가 rotation을 가진다</b>(63%).
     * 첫 판은 {@code ocular.cubes}만 읽어 이 63%를 모두 빠뜨렸고 —
     * 거기에 {@code instanceof BedrockCubeBox}가 나머지 37%도 걸러내,
     * 결국 정점이 하나도 등록되지 않아 마스크가 온통 검고 로그도 아무것도 찍지 않았다
     * ({@code isEmpty()}가 먼저 return했기 때문이다).
     *
     * <p>여기의 순회 구조는 일부러 {@link BedrockPart#render}와 맞췄다:
     * 각 노드에서 먼저 {@code translateAndRotateAndScale}하고, 자기 cubes를 모은 뒤,
     * 자식 노드로 재귀한다. <b>{@code visible}은 확인하지 않는다</b> —
     * 접안렌즈 자신이 {@code visible=false}라서(주 화면에는 그리지 않음),
     * 보이는지로 거르면 다시 하나도 모으지 못한다.
     */
    private void collectMaskGeometry(BedrockPart part, PoseStack poseStack) {
        poseStack.pushPose();
        try {
            part.translateAndRotateAndScale(poseStack);
            if (!part.cubes.isEmpty()) {
                // Entry 생성자는 행렬을 복사한다 — 꼭 그래야 한다:
                // poseStack은 곧 popPose되지만 이 데이터는 단계 경계에서 쓰일 때까지 살아 있어야 한다.
                ScopeMaskGeometry.add(poseStack.last().pose(), part.cubes);
            }
            for (BedrockPart child : part.children) {
                collectMaskGeometry(child, poseStack);
            }
        } finally {
            poseStack.popPose();
        }
    }
}

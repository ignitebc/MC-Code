package com.tacz.guns.client.render.scope;

import com.tacz.guns.client.model.bedrock.BedrockPart;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * 조준경 모델 하나에서 "조준선 층"과 관련된 노드 모음.
 *
 * <p>{@code BedrockAttachmentModel}을 만들 때 한 번 해석해 캐시하고,
 * 그 뒤로는 매 프레임 읽기만 해서 모델 트리를 반복해 훑지 않는다.</p>
 *
 * <h2>따로 떼어 낸 이유</h2>
 * 조준선 그리기 전략(홀로그램 / 새김 / 혼합)은 "이 조준경에 어떤 조준선 노드가 있는지" 알아야 하는데,
 * 이 판정과 실제로 그리는 방법은 별개다 — 떼어 내면 {@link IReticleRenderer}의
 * 각 구현이 이 객체만 읽고 모델 트리는 건드리지 않아도 되며, 외부 총기 팩도 같은 해석 결과를 다시 쓸 수 있다.
 */
public final class ScopeNodeSet {

    private static final ScopeNodeSet EMPTY =
            new ScopeNodeSet(Collections.emptyList(), Collections.emptyList());

    /** 새긴 눈금 노드({@code division} / {@code divisions} 등, 빛나지 <b>않음</b>). */
    private final List<BedrockPart> etchedReticle;

    /** 발광 조준선 노드(이름이 {@code _illuminated}로 끝나며 최대 밝기가 강제된다). */
    private final List<BedrockPart> illuminatedReticle;

    private final ReticleKind kind;

    /**
     * 이 노드 하위 트리에 <b>실제로 형상이 있는지</b>(자신이나 자손 중 하나라도 cube를 가짐).
     *
     * <h2>"노드가 있는지"만 볼 수 없는 이유</h2>
     * 실측 기본 총기 팩에는 <b>비어 있는 자리 표시 조준선 노드</b>가 있다:
     * <pre>
     * scope_contender(컨텐더 4x):
     *     division            4 cubes  &lt;- 모두 16x84 / 52x16 [차광판]이며 조준선이 아님
     *     └─ dot_illuminated  0 cubes  &lt;- 빈 노드, 형상이 전혀 없음
     * </pre>
     * 예전 기준은 {@code illuminatedReticle.isEmpty()}만 봐서 "발광 조준선이 있다"고 판단했고,
     * {@link ReticleRendererRegistry}가 이를 {@link IlluminatedReticleRenderer}에 맡겼다
     * (그 {@code matches()}가 바로 {@code hasIlluminated()}다).
     * 그 전략은 <b>발광 노드만 그리는데</b> — 이 노드는 비어 있어 한 픽셀도 그리지 못했고,
     * "컨텐더 4x 렌즈 안에 조준선이 전혀 없다"로 나타났다(사용자 실측).
     *
     * <p>더 나쁜 점은 이것이 동시에 {@link EtchedReticleRenderer}를 <b>막았다</b>는 것이다
     * (그쪽은 {@code hasEtched() && !hasIlluminated()}를 요구한다).
     * 그래서 {@code division} 경로로도 가지 못했다.
     *
     * <p><b>실제 형상</b>으로 판정하도록 바꾼 뒤에는: {@code dot_illuminated}가 cube가 없어
     * "발광 조준선 없음"으로 판정되고, 이 조준경은 자동으로 ETCHED 분기로 가 새김 전략이 {@code division}을 그린다.
     * 그리고 {@code division}의 차광판 몇 장은 XY가 접안렌즈 투영에서 멀리 떨어져 있어
     * (X∈[-42,-26]∪[26,42], 접안렌즈는 X∈[-0.75,0.75]뿐)
     * 반대 잘라내기로 통째로 버려지므로 9차의 화면 덮개가 되풀이되지 않는다.
     *
     * <p>또 이 기준은 {@code sight_t1}/{@code sight_t2}에도 <b>안전</b>하다:
     * 그 {@code division_illuminated} 자신도 cube가 0개지만
     * 자식 노드 {@code bone}에 cube가 2개 있어 재귀로 세면 여전히 "발광 조준선 있음"으로 판정되어
     * 동작이 바뀌지 않는다. 이것이 {@code part.cubes}만 보지 않고 <b>재귀</b>해야 하는 이유다.
     */
    private static boolean hasGeometry(BedrockPart part) {
        if (part == null) {
            return false;
        }
        if (!part.cubes.isEmpty()) {
            return true;
        }
        for (BedrockPart child : part.children) {
            if (hasGeometry(child)) {
                return true;
            }
        }
        return false;
    }

    /** "빈 자리 표시 노드"를 걸러 내고 실제 형상이 있는 것만 남긴다. */
    private static List<BedrockPart> withGeometry(List<BedrockPart> src) {
        if (src == null || src.isEmpty()) {
            return Collections.emptyList();
        }
        List<BedrockPart> out = new java.util.ArrayList<>(src.size());
        for (BedrockPart part : src) {
            if (hasGeometry(part)) {
                out.add(part);
            }
        }
        return out;
    }

    public ScopeNodeSet(List<BedrockPart> etchedReticle, List<BedrockPart> illuminatedReticle) {
        // [실제 형상으로 거름] 빈 자리 표시 노드는 모두 세지 않는다. 자세한 내용은 hasGeometry 설명 참고.
        this.etchedReticle = withGeometry(etchedReticle);
        this.illuminatedReticle = withGeometry(illuminatedReticle);
        boolean hasEtched = !this.etchedReticle.isEmpty();
        boolean hasIlluminated = !this.illuminatedReticle.isEmpty();
        if (hasEtched && hasIlluminated) {
            this.kind = ReticleKind.HYBRID;
        } else if (hasIlluminated) {
            this.kind = ReticleKind.HOLOGRAPHIC;
        } else if (hasEtched) {
            this.kind = ReticleKind.ETCHED;
        } else {
            this.kind = ReticleKind.NONE;
        }
    }

    public static ScopeNodeSet empty() {
        return EMPTY;
    }

    public List<BedrockPart> etchedReticle() {
        return etchedReticle;
    }

    public List<BedrockPart> illuminatedReticle() {
        return illuminatedReticle;
    }

    public ReticleKind kind() {
        return kind;
    }

    public boolean hasEtched() {
        return !etchedReticle.isEmpty();
    }

    public boolean hasIlluminated() {
        return !illuminatedReticle.isEmpty();
    }

    public boolean isEmpty() {
        return kind == ReticleKind.NONE;
    }

    /** 로그로 살펴보기 쉽게: 형태와 두 종류 노드의 수를 돌려준다. */
    @Override
    public String toString() {
        return "ScopeNodeSet{" + kind + ", etched=" + etchedReticle.size()
                + ", illuminated=" + illuminatedReticle.size() + '}';
    }

    /** 첫 발광 노드를 얻고, 없으면 null(P1 단계에서 자주 쓰는 지름길). */
    @Nullable
    public BedrockPart firstIlluminated() {
        return illuminatedReticle.isEmpty() ? null : illuminatedReticle.get(0);
    }
}

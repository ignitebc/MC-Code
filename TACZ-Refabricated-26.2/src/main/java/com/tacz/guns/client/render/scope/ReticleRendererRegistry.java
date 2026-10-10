package com.tacz.guns.client.render.scope;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * {@link IReticleRenderer}의 레지스트리이자 선택기.
 *
 * <h2>용도</h2>
 * 외부 총기 팩 / 애드온 모드는 클라이언트 초기화 때 자신의 조준선 전략을 등록하고,
 * {@link IReticleRenderer#priority()}로 내장 구현을 덮어쓸 수 있다:
 * <pre>{@code
 * ReticleRendererRegistry.register(new MyHoloReticleRenderer());  // priority() > 0
 * }</pre>
 *
 * <h2>선택 규칙</h2>
 * {@code priority()}가 큰 것부터 돌며 {@code matches(nodes)}가 참인 첫 구현을 돌려준다.
 * 내장 구현의 우선순위는 모두 0이므로 {@code > 0}으로 등록한 것은 무엇이든 먼저 맞는다.
 */
public final class ReticleRendererRegistry {

    private static final List<IReticleRenderer> RENDERERS = new ArrayList<>();

    static {
        // 발광 조준선: HOLOGRAPHIC과 HYBRID를 맡는다(기본 총기 팩 조준경 25개).
        register(IlluminatedReticleRenderer.INSTANCE);
        // 순수 새긴 눈금: division만 있고 *_illuminated가 하나도 없는 나머지 조준경 6개를 맡는다
        // (스프링필드 scope_1873_6x / 마우저 scope_98k / AUG 기본 scope_aug_default /
        //   scope_contender / scope_qmk152 / scope_retro_2x).
        // 둘 다 priority가 0이고 matches()로 서로 배타적이다: Illuminated는 발광 노드가 있어야 하고,
        // Etched는 "새김이 있고 발광이 없어야" 하므로 겹치지 않는다.
        register(EtchedReticleRenderer.INSTANCE);
    }

    private ReticleRendererRegistry() {
    }

    /** 전략 하나를 등록한다. 스레드 안전하지 않으므로 클라이언트 초기화 단계에서만 호출한다. */
    public static synchronized void register(IReticleRenderer renderer) {
        if (renderer == null || RENDERERS.contains(renderer)) {
            return;
        }
        RENDERERS.add(renderer);
        RENDERERS.sort(Comparator.comparingInt(IReticleRenderer::priority).reversed());
    }

    /**
     * 주어진 노드 모음에 쓸 전략을 고른다.
     *
     * @return 맞는 전략. 맞는 전략이 없으면 {@code null}(호출하는 쪽은 그리기를 건너뛴다)
     */
    @Nullable
    public static IReticleRenderer select(ScopeNodeSet nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        for (IReticleRenderer renderer : RENDERERS) {
            if (renderer.matches(nodes)) {
                return renderer;
            }
        }
        return null;
    }
}

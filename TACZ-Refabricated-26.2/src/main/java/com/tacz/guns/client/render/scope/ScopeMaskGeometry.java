package com.tacz.guns.client.render.scope;

import com.tacz.guns.client.model.bedrock.BedrockCube;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 이번 프레임에 마스크에 써야 할 접안렌즈 형상 목록.
 *
 * <h2>바로 그리지 않고 "수집기"가 필요한 이유</h2>
 * 26.2의 그리기는 <b>두 단계</b>다: 모델 코드는 {@code submit}에서 "제출"만 하고,
 * 실제 그리기는 나중의 {@code FeatureRenderDispatcher#renderAllFeatures}에서 일어난다.
 * 그런데 우리 마스크 pass는 <b>단계 경계</b>에서 열어야 한다(r51에서 장치 손실을 겪은 뒤
 * 안전하다고 확인된 유일한 시점이며, 앞선 빈 pass 시험으로 실측 확인했다).
 *
 * <p>두 시점이 다르므로 사이에 둘 곳이 필요하다:
 * {@code BedrockAttachmentModel#submit}이 여기에 접안렌즈 형상을 <b>등록</b>하고,
 * 단계 경계의 마스크 pass가 <b>꺼내서 그린다</b>.
 *
 * <p>이렇게 하면 바닐라 사용 제약 — "<b>한꺼번에</b>, 단계 경계에서 target을 바꾼다" — 도 함께 지킨다.
 * 한 프레임에 조준경이 여러 개일 수 있지만(주 손/보조 손, 복합 조준경 두 그룹), 모두 모은 뒤 한 번에 그리며
 * pass도 하나만 연다. r51은 조준경마다 target 전환을 한 번씩 일으켜 크래시가 났다.
 *
 * <h2>좌표 공간</h2>
 * 등록되는 행렬은 <b>이미 곱해진 전체 모델 행렬</b>(PoseStack 루트 변환과 부모 사슬 전체 포함)이며,
 * {@code BedrockRenderSnapshot.DrawCommand#pose}와 같은 공간이다.
 * 정점을 쓸 때는 {@code pos/16 → mul(matrix)}만 하며,
 * {@code BedrockCubeBox#compile}의 계산법과 줄마다 같아 두 경로가 어긋나지 않는다.
 *
 * <h2>수명</h2>
 * 매 프레임 {@code clear()}를 한 번 한다. **마스크를 그리지 못했더라도 무조건 비워야 한다** —
 * 아니면 조준하지 않을 때 직전 프레임 형상이 남아 점점 쌓인다.
 */
@Environment(EnvType.CLIENT)
public final class ScopeMaskGeometry {

    /**
     * 마스크에 쓸 육면체 묶음과 그것들이 함께 쓰는 모델 행렬.
     *
     * @param pose  전체 모델 행렬(루트 변환과 부모 사슬 포함)
     * @param cubes 그 행렬 아래의 육면체
     */
    public record Entry(Matrix4f pose, List<BedrockCube> cubes) {
        public Entry {
            // 방어적 복사: BedrockPart는 여러 프레임이 함께 쓰고 애니메이션이 바꾸는데,
            // 이 목록은 단계 경계에서 쓰일 때까지 살아 있어야 하므로 중간에 바뀌면 엉뚱한 위치에 그린다.
            pose = new Matrix4f(pose);
            cubes = List.copyOf(cubes);
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private ScopeMaskGeometry() {
    }

    public static void add(Matrix4f pose, List<BedrockCube> cubes) {
        if (cubes.isEmpty()) {
            return;
        }
        ENTRIES.add(new Entry(pose, cubes));
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static boolean isEmpty() {
        return ENTRIES.isEmpty();
    }

    public static void clear() {
        ENTRIES.clear();
    }
}

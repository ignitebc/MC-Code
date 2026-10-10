package com.tacz.guns.client.render.scope;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.tacz.guns.GunMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

/**
 * 조준경 "접안렌즈 마스크"의 화면 밖 렌더링 대상.
 *
 * <h2>무엇을 해결하는가</h2>
 * 원본 1.21.1은 stencil로 "몸체를 접안렌즈 원 <b>바깥</b>에만 그리기"를 했다:
 * <pre>
 * renderOcularStencil(...)              // ocular 형상 자체로 스텐실 값을 씀
 *     colorMask(false,false,false,false);  //   스텐실만 쓰고 색은 쓰지 않음
 * scope_body: stencilFunc(GL_EQUAL, 0)  // 접안렌즈가 [덮지 않은] 곳에만 몸체를 그림
 * </pre>
 * 잘라내기 영역은 어떤 기하학적 원이 아니라 <b>접안렌즈 형상의 화면 투영</b>이라는 점에 주의한다
 * (r46은 여기서 틀려 화면 중심에 고정된 원을 그렸고, 조준경 본체 전체를 지워 버렸다).
 *
 * <p>26.2의 렌더링 추상층(Vulkan 백엔드 포함)에는 stencil이 전혀 없다.
 * 같은 효과를 내려면 {@code ocular}를 따로 화면 밖 텍스처에 그려 마스크로 삼고,
 * 몸체 shader가 그것을 샘플링해 discard를 정하게 한다. 이 클래스는 그 텍스처의 수명을 맡는다.</p>
 *
 * <h2>진행 상황</h2>
 * <ul>
 *   <li><b>Step 1(PASS)</b>: target을 만들고 순수 빨강으로 비운 뒤 화면 구석에 blit했다.
 *       "화면 밖 target을 만들고, 비우고, 텍스처를 화면으로 다시 샘플링할 수 있다"를 증명했다.
 *       그때의 화면 비우기 메서드는 이번에 지웠다 — 목표를 이뤘으니 물러나며 죽은 코드를 남기지 않는다.</li>
 *   <li><b>Step 2-probe(PASS)</b>: <b>단계 경계</b>에서 이 target을 가리키는 빈 render pass를 열고
 *       순수 초록으로 비워, "OutputTarget을 넘나드는 pass 전환이 단계 경계에서 안전한가"를 검증했다
 *       — r51이 {@code VK_ERROR_DEVICE_LOST}를 겪은 뒤 따로 검증하지 않은 유일한 가정이었다.
 *       실측 통과했고 시험 코드는 지웠다.</li>
 *   <li><b>Step 2(현재)</b>: {@link ScopeMaskRenderer}가 같은 시점에
 *       이번 프레임의 모든 접안렌즈 형상을 이 target에 그린다. 미리 보기 칸에
 *       <b>총을 따라 움직이는 흰 모양</b>이 나타나야 한다 — 이 단계로 전체 방안의 핵심 가정을 검증한다:
 *       <b>잘라내기 영역 = 접안렌즈 형상의 화면 투영</b>.</li>
 * </ul>
 *
 * <p>이렇게 잘게 나눈 이유: 작업 환경에서는 컴파일도 화면 확인도 할 수 없었고, 마스크 버그는 보통
 * "온통 검정"이나 "아무것도 없음"으로 나타나 어느 단계가 고장 났는지 구분하기 어렵다. 단계마다 <b>눈으로 판정할 수 있게</b>
 * 설계해야 다음 단계에 믿을 만한 기준이 생긴다.</p>
 */
@Environment(EnvType.CLIENT)
public final class ScopeMaskTarget {

    /** 주 프레임 버퍼 대비 마스크 해상도 배율. 1.0 = 같은 해상도. */
    private static final float SCALE = 1.0f;

    @Nullable
    private static TextureTarget target;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    /** 한 번 오류가 나면 영구히 끈다. 매 프레임 로그를 쏟거나 예외를 반복해 던지지 않게 한다. */
    private static boolean failed = false;

    private ScopeMaskTarget() {
    }

    /**
     * 마스크 target을 얻는다(필요하면 만들거나 다시 만든다).
     *
     * <p>창 크기가 바뀌면 다시 만든다 — {@code RenderTarget#resize}가 있지만,
     * 다시 만드는 편이 더 단순하고 여기서는 매 프레임 int 두 개만 비교하므로 비용은 무시할 만하다.</p>
     *
     * @return 쓸 수 있는 target. 실패했거나 크기가 잘못되면 {@code null}
     */
    @Nullable
    public static TextureTarget getOrCreate() {
        if (failed) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        // GUI 배율을 적용한 크기가 아니라 프레임 버퍼의 실제 픽셀 크기를 쓴다는 점에 주의한다.
        int w = Math.max(1, (int) (mc.getWindow().getWidth() * SCALE));
        int h = Math.max(1, (int) (mc.getWindow().getHeight() * SCALE));
        try {
            if (target == null || w != lastWidth || h != lastHeight) {
                if (target != null) {
                    target.destroyBuffers();
                }
                // useDepth=false: 마스크는 "이 픽셀이 접안렌즈에 덮였는지"만 보면 되고
                // 깊이는 필요 없다. 깊이 텍스처가 하나 줄어 그래픽 메모리도 아낀다.
                target = new TextureTarget("tacz_scope_mask", w, h, false, GpuFormat.RGBA8_UNORM);
                lastWidth = w;
                lastHeight = h;
            }
            return target;
        } catch (Exception e) {
            failed = true;
            GunMod.LOGGER.error("[TACZ Scope] Failed to create scope mask target; feature disabled.", e);
            close();
            return null;
        }
    }

    /** 현재 target을 쓸 수 있는지(디버그 겹침 화면이 그릴지 판단하는 데 쓴다). */
    public static boolean isAvailable() {
        return !failed && target != null;
    }

    @Nullable
    public static RenderTarget current() {
        return target;
    }

    /** 자원 해제. 지금은 어떤 수명 주기 콜백에도 연결하지 않았고 뒤 단계용으로 남겨 둔다. */
    public static void close() {
        if (target != null) {
            try {
                target.destroyBuffers();
            } catch (Exception ignored) {
                // 닫기에 실패해도 손쓸 방법이 없으므로 무시한다
            }
            target = null;
        }
        lastWidth = -1;
        lastHeight = -1;
    }
}

package com.tacz.guns.mixin.client;

import com.tacz.guns.client.render.scope.ScopeMaskRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code renderAllFeatures}의 <b>단계 경계</b>에 조준경 마스크 pass를 끼워 넣는다.
 *
 * <h2>반드시 이 위치여야 하는 이유</h2>
 * 26.2의 그리기 구조(바이트코드 확인):
 * <pre>
 * renderAllFeatures(storage) {
 *     PreparedFrame f = prepareFrame(storage);   // 준비만 하고 그리지 않는다
 *     f.executeSolid();                          // ← 각 executeXxx 안에서만 pass를 열고 닫는다
 *     f.executeTranslucent();
 *     f.executeTranslucentAfterTerrain();
 *     f.executeAlwaysOnTop();
 *     f.close();
 * }
 * </pre>
 * 즉 <b>단계와 단계 사이는 어떤 render pass 안에도 있지 않으므로</b>
 * {@code CommandEncoder#createRenderPass} 첫머리의 단언을 만족한다:
 * <pre>
 * if (this.isInRenderPass) throw new IllegalStateException(
 *     "Close the existing render pass before creating a new one!");
 * </pre>
 *
 * <p>이것이 바로 r51 실패의 반대다. 그때는 {@code ocular}에 outputTarget이 다른
 * RenderType을 주어 collector로 보냈는데, 엔진이 RenderType별로 묶어 실행해서
 * "주 target → 마스크 target → 주 target" 전환이 solid 단계 안에 <b>흩어져 끼어들었고</b>
 * {@code VK_ERROR_DEVICE_LOST}가 났다. 바닐라 자체의 다중 target 전환은 언제나
 * <b>묶어서, 단계 경계에서</b> 한다 — 이 mixin은 그 방식으로 돌아간다.</p>
 *
 * <h2>기반은 검증됨</h2>
 * 지난 차수에 빈 pass 탐침으로 이 시점만 따로 검증했고(실측 미리보기 칸이 초록색으로 바뀜),
 * "단계 경계에서 OutputTarget 전환"이 r51의 장치 손실을 되풀이하지 않음을 증명했다.
 * 결론이 굳어졌으니 탐침은 물러났고, 이번 차수에는 {@link ScopeMaskRenderer}가 실제 형상을 그린다.
 *
 * <h2>주입 지점 선택</h2>
 * {@code HEAD}가 아니라 {@code INVOKE + executeSolid}를 쓴다:
 * {@code HEAD}에서는 {@code prepareFrame}이 아직 돌지 않았고,
 * {@code prepareFrame} 안에 {@code stagedVertexBuffer.upload()}가 있다.
 * 마스크 형상을 실제로 그릴 때는 upload <b>이후</b>에야 정점 데이터를 얻을 수 있다.
 * 지금 위치를 바로 잡아 두어 나중에 다시 옮기지 않게 한다.
 *
 * <p>{@code shift = BEFORE}는 마스크가 solid 전에 끝나도록 보장한다 — 조준경 몸체는 solid 단계에서 그려지므로,
 * 마스크를 샘플링할 때 이미 준비되어 있어야 한다.</p>
 */
@Mixin(FeatureRenderDispatcher.class)
public abstract class FeatureRenderDispatcherMixin {

    @Inject(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeSolid()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void tacz$scopeMaskAtPhaseBoundary(SubmitNodeStorage storage, CallbackInfo ci) {
        // [Step 2] 실제 접안렌즈 마스크를 그린다.
        //
        // 지난 차수의 빈 pass 탐침이 이 시점이 안전함을 증명했고(실측 미리보기 칸이 초록색으로 바뀜),
        // 결론이 굳어진 뒤 탐침은 지워 죽은 코드를 남기지 않았다.
        ScopeMaskRenderer.renderAtPhaseBoundary();
    }
}

package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.client.other.KeepingItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin implements KeepingItemRenderer {
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private ItemStack mainHandItem;
    @Unique
    private ItemStack tacz$KeepItem;
    @Unique
    private long tacz$KeepTimeMs;
    @Unique
    private long tacz$KeepTimestamp;

    /**
     * 26.2 이전: renderHandsWithItems → submitHandsWithItems
     * 새 시그니처(26.2): submitHandsWithItems(float, PoseStack, SubmitNodeCollector, LocalPlayer, int)
     */
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    public void beforeHandRender(float pPartialTicks, PoseStack pMatrixStack, net.minecraft.client.renderer.SubmitNodeCollector pCollector, LocalPlayer pPlayerEntity, int pCombinedLight, CallbackInfo ci) {
        BeforeRenderHandEvent.CALLBACK.invoker().post(new BeforeRenderHandEvent(pMatrixStack));
    }

    /**
     * 1인칭 총기 렌더링 입구. <b>"카메라 기준 총 위치/크기가 틀림 + 움직일 때 떨림"을 고치는 핵심이다.</b>
     *
     * <p><b>문제 배경</b></p>
     *
     * <p>원본 1.21.1은 SimpleBedrockModel의 {@code RenderHandEvent}에 기댄다. SBM의 mixin은
     * ({@code Sh1roCu/SimpleBedrockModel-Fabric} 소스 대조) {@code ItemInHandRenderer#renderArmWithItem}의
     * <b>HEAD</b>에 주입해 {@code ci.cancel()}한다.
     * 즉 TACZ가 받는 PoseStack은 <b>submitHandsWithItems의 시점 되흔들림만 거친</b>,
     * <b>팔 변환을 아직 하나도 거치지 않은</b> 깨끗한 행렬이다.</p>
     *
     * <p>26.2로 이식하면서 클라이언트 ItemModel({@code tacz:dynamic_item}) 경로로 바꿔,
     * 렌더링이 {@code renderItem(...)} 안에서 일어난다 — 그때는 바닐라가 이미 다음을 더 적용했다:</p>
     * <ol>
     *   <li>{@code applyItemArmTransform}: {@code translate(±0.56, -0.52 + 장비 높이*-0.6, -0.72)}
     *       — 이것이 "위치가 어긋남"의 직접 원인이며 조준 시 특히 두드러진다;</li>
     *   <li>{@code swingArm(...)} / {@code SpearAnimations.firstPersonAttack(...)}
     *       휘두르기 애니메이션 — TACZ 자체 애니메이션 상태 기계와 겹쳐 <b>이동/달리기 중 손과 총이 떨리고 애니메이션이 끊기는</b> 것으로 나타난다;</li>
     *   <li>장비 전환의 {@code inverseArmHeight} 손 들기 애니메이션 — 이 역시 TACZ의 총 꺼내기/집어넣기 애니메이션과 부딪힌다.</li>
     * </ol>
     *
     * <p><b>수정</b>: {@code submitArmWithItem}의 HEAD에서 가로채 취소하고, 여기서 바로
     * TACZ의 1인칭 렌더링을 호출한다 — SBM의 주입 지점, 취소 의미와 완전히 같아 1.21.1과
     * 같은 의미의 깨끗한 PoseStack을 얻는다.</p>
     *
     * <p>주의: {@code submitHandsWithItems}의
     * {@code mulPose(XP(viewXRot - xBob) * 0.1)} / {@code mulPose(YP(viewYRot - yBob) * 0.1)}
     * 시점 되흔들림은 <b>그대로 남는다</b>(이 메서드 전에 실행됨). 이것이 바로
     * {@code GunItemRendererWrapper#renderFirstPerson} 첫머리의 "바닐라 지연 효과 되돌리기"가 기대하는 입력이다.</p>
     */
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void tacz$submitArmWithGun(net.minecraft.client.player.AbstractClientPlayer player,
                                       float frameInterp,
                                       float xRot,
                                       net.minecraft.world.InteractionHand hand,
                                       float attack,
                                       ItemStack itemStack,
                                       float inverseArmHeight,
                                       PoseStack poseStack,
                                       net.minecraft.client.renderer.SubmitNodeCollector collector,
                                       int lightCoords,
                                       CallbackInfo ci) {
        if (!(player instanceof LocalPlayer localPlayer)) {
            return;
        }

        // [5차] "지금이 정말 1인칭인지"를 명시적으로 판정해야 한다.
        //
        // 증상: 3인칭으로 총을 들면 몸에 쓸데없고 일그러진 팔 두 개가 나타난다. 총이 아닌 아이템으로 바꾸면 사라지고,
        //      1인칭에서 이 상태를 "가로채" 계속 남길 수 있다.
        //
        // 근본 원인: ItemInHandRenderer 인스턴스는 전역에서 함께 쓴다. GameRenderer#renderItemInHand에는
        //      자체 PIP/화면 밖 경로 일부가 여전히 submitArmWithItem으로 들어올 수 있다. 일단 들어오면 TACZ는
        //      renderFirstPerson -> Left/RightHandRender -> AvatarRenderer#renderHand로 가고,
        //      후자는 공유 PlayerModel을 바로 고쳐 쓰며(arm.visible=true, zRot=±0.1, 소매 표시 여부)
        //      되돌리지 않는다.
        //
        //      핵심: submitModelPart가 저장하는 것은 [살아 있는 ModelPart 참조]이며(RenderHelper 주석 참고),
        //      실제 그리기는 나중의 renderAllFeatures에서 일어나므로 강제로 켜진 이 팔 부품들이
        //      3인칭 플레이어 엔티티 위에 한 번 더 그려진다 — 그것이 그 "쓸데없고 일그러진" 팔 두 개다.
        //
        // 수정: 진짜 1인칭일 때만 넘겨받고, 3인칭은 모두 바닐라에 넘긴다.
        //      TACZ의 3인칭 총기는 renderByItem + PlayerModelMixin의 팔 자세가 맡는다.
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }
        // 렌더링 연장: 총을 바꿀 때 이전 총의 모델을 유지한다. 1.21.1 동작과 같다.
        ItemStack renderStack = itemStack;
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
            ItemStack kept = KeepingItemRenderer.getRenderer().getCurrentItem();
            if (kept != null && !kept.isEmpty()) {
                renderStack = kept;
            }
        }
        if (com.tacz.guns.api.item.IGun.getIGunOrNull(renderStack) == null) {
            return;
        }

        // 원본 의미: 주 손에 총을 들면 보조 손은 일반 렌더링을 타지 않는다(보조 손 총은 HumanoidOffhandRender가 등에 멘 모습으로 표시).
        if (hand == net.minecraft.world.InteractionHand.OFF_HAND) {
            ci.cancel();
            return;
        }

        var renderer = cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry.INSTANCE.get(renderStack.getItem());
        if (!(renderer instanceof com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer<?, ?> geoRenderer)) {
            return;
        }

        net.minecraft.world.item.ItemDisplayContext ctx =
                player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT
                        ? net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        : net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (geoRenderer.needReInit(renderStack)) {
            geoRenderer.tryInit(renderStack, localPlayer, frameInterp);
        }
        poseStack.pushPose();
        geoRenderer.renderFirstPerson(localPlayer, renderStack, ctx, poseStack, collector, lightCoords, frameInterp);
        poseStack.popPose();
        ci.cancel();
    }

    /**
     * <b>일부러 비워 둔다</b> — 원본 1.21.1과 완전히 같다.
     *
     * <h2>여기서 아무것도 하지 않아야 하는 이유</h2>
     * 원본의 같은 이름 주입 지점은 통째로 <b>주석 처리</b>되어 있다({@code ItemInHandRendererMixin} 38-59번째 줄,
     * 줄마다 대조함). 즉 TACZ는 바닐라의 장비 진행도에 한 번도 끼어든 적이 없다.
     * 이식할 때 이 부분이 실행 코드로 "복원"되어 오히려 총 바꾸기 애니메이션 버그를 만들었다.
     *
     * <h2>그것이 총 바꾸기 애니메이션을 끊거나 빨라지게 한 이유</h2>
     * {@code mainHandHeight} / {@code oMainHandHeight}는 바로 바닐라
     * {@code ItemInHandRenderer#tick}이 <b>손 바꾸기 애니메이션</b>을 진행하는 데 쓰는 상태 값이다:
     * <pre>
     * // 바닐라 tick(): tick마다 목표값에 다가가며 "내려가기-올라오기" 전환을 만든다
     * this.oMainHandHeight = this.mainHandHeight;
     * this.mainHandHeight += Mth.clamp(target - this.mainHandHeight, -0.4F, 0.4F);
     * </pre>
     * 그리고 {@code mainHandItem}은 "지금 어느 총을 그릴지", 언제 새 총으로 바꿀지를 정한다.
     *
     * <p>원래 구현은 HEAD에서 이 세 값을 <b>tick마다 강제로 고정</b>했다
     * (높이는 항상 1.0, 아이템은 항상 현재 주 손 아이템):
     * <ul>
     *   <li>높이가 고정됨 → 바닐라의 전환 보간이 의미를 잃어 애니메이션이 <b>끊기거나 즉시 끝나는</b> 것으로 나타난다;</li>
     *   <li>{@code mainHandItem}이 곧바로 새 총으로 바뀜 → 예전 총의 집어넣기 애니메이션이 끝나기도 전에 바뀌어
     *       <b>애니메이션이 보이지 않는</b> 것으로 나타난다;</li>
     *   <li>두 총을 빠르게 연달아 바꾸면 {@code tacz$KeepItem}의 시간 창과 이곳의 강제 쓰기가 서로 부딪혀
     *       (keep 창 안에서는 keepItem을, 창 밖에서는 곧바로 새 아이템을 씀) <b>비정상적으로 빨라진다</b>.</li>
     * </ul>
     * 이는 사용자가 실측한 "서로 다른 두 총을 계속 바꾸면 애니메이션이 끊기거나 비정상적으로 빨라지고 아예 보이지 않음"과 정확히 맞다.
     *
     * <p>TACZ 자체 총 바꾸기 애니메이션은 상태 기계가 맡는다({@code LocalPlayerDraw#doPutAway} →
     * {@code AnimateGeoItemRenderer#tryExit}가 {@code INPUT_PUT_AWAY}를 일으키고,
     * {@code TickAnimationEvent}/{@code needReInit}가 {@code INPUT_DRAW}를 이끈다).
     * 바닐라의 장비 진행도를 바꿀 <b>필요도 없고 바꿔서도 안 된다</b>.
     *
     * <p>통째로 지우지 않고 빈 주입 지점을 남기는 이유는 위 설명을 지키기 위해서다 —
     * 나중에 누군가 "빈 메서드를 보고 그냥 구현해 버리는" 일을 막는다.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    public void cancelEquippedProgress(CallbackInfo ci) {
    }

    @Unique
    @Override
    public void keep(ItemStack itemStack, long timeMs) {
        long time = System.currentTimeMillis() - tacz$KeepTimestamp;
        if (time < tacz$KeepTimeMs) {
            return;
        }
        this.tacz$KeepTimeMs = timeMs;
        this.tacz$KeepTimestamp = System.currentTimeMillis();
        this.tacz$KeepItem = itemStack;
        this.mainHandItem = itemStack;
    }

    @Override
    public ItemStack getCurrentItem() {
        if (Minecraft.getInstance().player == null) {
            return mainHandItem;
        }
        if (tacz$KeepItem != null) {
            long time = System.currentTimeMillis() - tacz$KeepTimestamp;
            if (time < tacz$KeepTimeMs) {
                return tacz$KeepItem;
            } else {
                tacz$KeepItem = null;
            }
        }
        return mainHandItem;
    }
}

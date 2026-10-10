package com.tacz.guns.mixin.client;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.third.InnerThirdPersonManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {
    @Shadow
    @Final
    public ModelPart leftSleeve;
    @Shadow
    @Final
    public ModelPart rightSleeve;

    public PlayerModelMixin(ModelPart part) {
        super(part);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At(value = "TAIL"))
    private void setRotationAnglesTail(AvatarRenderState renderState, CallbackInfo ci) {
        // [34차 수정] 총을 집어넣은 뒤 PAL 3인칭 동작이 마지막 프레임에서 멈춤.
        //
        // 증상: Player Animation Library를 설치하면 총을 든 3인칭 동작은 맞지만,
        // 총이 아닌 아이템이나 빈손으로 바꾸는 순간 플레이어가 마지막으로 총을 들었던 자세를 영원히 유지한다(서기/걷기/달리기/엎드리기 모두 멈춤).
        //
        // 근본 원인은 <b>여기의 이른 return이 "애니메이션 멈춤" 경로까지 함께 막은 것</b>이다.
        // PAL을 실제로 멈추는 것은 InnerThirdPersonManager#setRotationAnglesHead의 앞 몇 줄이다:
        //     IGun iGun = IGun.getIGunOrNull(mainHandItem);
        // 즉 "손에 든 것이 총이 아님"이 바로 <b>호출을 반드시 들여보내야 하는</b> 경우다 — 들어가야만
        // stopAllAnimation이 일어난다. 그런데 예전 코드는 mixin 층에서 `if (getIGunOrNull == null) return;`을 해서,
        // 총을 집어넣은 순간부터 InnerThirdPersonManager가 한 번도 호출되지 않았고,
        // PAL의 controller 네 개(LOWER/LOOP_UPPER/ONCE_UPPER/ROTATION)
        // 어느 것도 fade-out 지시를 받지 못해 마지막 반복 애니메이션을 계속 재생했다.
        //
        // 이것이 사용자가 말한 "엎드렸을 때는 좀 특이함"도 설명한다: 엎드린 자세는 LIE/LIE_MOVE 애니메이션을 타서,
        // 멈춘 뒤 "엎드린 채 선 모습"으로 나타나지만 본질은 다른 상태와 같은 원인이다.
        //
        // 수정: 총을 들었는지 판단하는 일을 InnerThirdPersonManager에 <b>넘겨</b> 스스로 하게 하고,
        // 이 메서드는 "살아 있는 엔티티가 있으면 넘겨주기"만 맡는다. stopAllAnimation 안에서 controller 네 개
        // 모두에 `controller.isActive()` 확인이 있으므로(PalAnimationManager#stop),
        // 반복 호출해도 결과가 같아 프레임마다 fade-out을 다시 일으키지 않는다.
        // [36차 수정] 원본의 `ageInTicks == 0` 확인을 되살린다.
        //
        // 지난 차수에 "총을 집어넣은 뒤 동작이 멈춤"을 고치려고 이른 return 전체를 지우고 무조건 넘기도록 바꾸면서,
        // <b>원본에 원래 있던 이 확인까지 함께 지웠다</b>(원본 HumanoidModelMixin L31-33:
        //     if (ageInTicks == 0) { return; }
        // ). 이 누락 하나가 사용자가 알린 새 버그 두 개를 함께 일으켰다:
        //
        // <b>① 3인칭으로 총을 든 채 나갔다가 저장 파일에 다시 들어오면 반드시 충돌.</b>
        // ageInTicks = tickCount + partialTick(EntityRenderer#extractRenderState 오프셋 69-75
        // 바이트코드 확인: Entity.tickCount를 읽어 EntityRenderState.ageInTicks에 씀).
        // 월드에 막 들어온 첫 프레임은 tickCount == 0이며, 이때 엔티티는 ClientLevel에 들어갔지만
        // 아직 tick을 한 번도 돌지 않았다 — TACZ의 ShooterDataHolder / AttachmentCacheProperty는
        // 모두 tick에서 지연 초기화되고, PAL의 controller도 아직
        // ANIMATION_DATA_FACTORY가 플레이어에게 붙이지 않았다. 이 프레임에 전체 애니메이션 사슬을 돌리면,
        // 반쯤 초기화된 상태 여럿과 부딪힌다. 원본 확인의 <b>유일한 역할</b>이 바로 이 프레임을 건너뛰는 것이다.
        //
        // "3인칭 + 총 들기"에서만 일어나는 이유: 1인칭에서는 PlayerModel이 몸체를 그리지 않아
        // 여기까지 오지 않고, 빈손일 때는 InnerThirdPersonManager가 getIGunOrNull==null에서
        // 바로 return해 뒤쪽 애니메이션 코드에 닿지 않는다. 두 조건 중 하나라도 빠지면 안 된다 — 사용자 실측과 정확히 맞다.
        //
        // <b>② 총을 집어넣은 뒤 이동 상태에 총 든 동작이 없고, 사격/재장전 때 잠깐 돌아옴.</b>
        // 이 확인이 지워진 뒤 GUI 안의 작은 플레이어 모델(인벤토리/가방 미리보기, ageInTicks는 항상 0)도
        // 프레임마다 들어와 stopAllAnimation을 한 번씩 돌렸다. 이것은 월드의 실제 플레이어와 <b>같은
        // PAL controller를 함께 쓴다</b>(controller는 플레이어 엔티티로 찾으며 렌더링 상황을 구분하지 않음).
        // 그래서 가방 미리보기가 프레임마다 막 재생된 반복 애니메이션을 fade-out시켰고 —
        // 걷기/달리기/수영의 총 든 동작이 시작되지 않는 것으로 나타났다.
        // 반면 사격/재장전은 ONCE_UPPER의 triggerAnimation(한 번짜리, 우선순위가 더 높음)을 타서
        // fade-out되기 전에 먼저 끝까지 재생되므로 "잠깐 돌아왔다가 다시 사라짐"이 된다.
        //
        // 수정한 뒤에도 지난 차수의 "총을 집어넣으면 멈춤"은 그대로 고쳐진다: ageInTicks != 0인 정상 렌더링 프레임은
        // 여전히 InnerThirdPersonManager로 넘어가고, 그것이 iGun == null일 때
        // stopAllAnimation을 호출한다 — 멈춤 로직은 사라지지 않았고, 0번째 프레임과 GUI 미리보기 프레임에서 잘못 일어나지 않을 뿐이다.
        // <b>if/else 하나가 아니라 "두 부분"인 이유</b>: 원본은 사실 이 두 일을 <b>두 개의</b> mixin으로 나눴고,
        // 각자 반대 확인을 가졌다. 26.2는 setupAnim 시그니처가 합쳐져서(모두
        // setupAnim(AvatarRenderState)) 한 메서드에 모였다:
        //   원본 PlayerModelMixin   : if (ageInTicks == 0 && 총을 듦) 팔만 0으로 되돌림
        //   원본 HumanoidModelMixin : if (ageInTicks == 0) return; 그 뒤에야 애니메이션 실행
        // 지난 차수에 둘을 한 부분으로 뭉치고 확인을 지워, 애니메이션 로직도 0번째 프레임에 돌게 되었다.
        if (renderState.ageInTicks == 0F) {
            // 0번째 프레임(1인칭 손 렌더링 / GUI 작은 모델): 기본 팔 회전만 지우고
            // 어떤 애니메이션 상태도 건드리지 않는다. 총 들기 판단은 원본 의미를 따른다.
            if (IGun.getIGunOrNull(renderState.getMainHandItemStack()) != null) {
                tacz$resetAll(this.rightArm);
                tacz$resetAll(this.leftArm);
            }
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.getEntity(renderState.id) instanceof LivingEntity livingEntity) {
            InnerThirdPersonManager.setRotationAnglesHead(
                    livingEntity,
                    this.rightArm,
                    this.leftArm,
                    this.body,
                    this.head,
                    renderState.walkAnimationSpeed
            );
        }

        // [8차] 여기서는 <b>일부러</b> 소매 자세를 더는 동기화하지 않는다.
        //
        // 증상: 3인칭(인벤토리의 작은 플레이어 모델 포함)에서 손에 "한 겹 더 있고 팔과 어긋난" 스킨이 나타났다.
        //
        // 근본 원인: 1.21.1에서 leftSleeve/rightSleeve는 PlayerModel의 <b>형제</b> 부품이라
        //       팔을 자동으로 따라가지 않으므로 원본은 `sleeve.copyFrom(arm)`을 명시적으로 해야 했다.
        //       26.2는 이를 <b>자식</b> 부품으로 바꿨다(디컴파일한 PlayerModel 생성자로 확인):
        //           this.leftSleeve  = this.leftArm.getChild("left_sleeve");
        //           this.rightSleeve = this.rightArm.getChild("right_sleeve");
        //       자식 부품은 렌더링할 때 <b>부모 변환을 자동으로 물려받는다</b>.
        //
        //       이식할 때 이 복사를 남겨 두어(loadPose(arm.storePose())로 씀),
        //       팔의 변환을 소매에 <b>한 번 더 겹쳐</b> 적용한 셈이 되었다 — 소매가 팔 기준으로 두 배 어긋나
        //       "손의 여러 겹 스킨이 맞지 않음/일그러진 손이 하나 더 있음"으로 보였다.
        //
        // 또한 바닐라의 PlayerModel#setupAnim은 프레임마다 소매의 visible만 설정하고
        // 자세는 전적으로 부모-자식 상속에 맡기므로 여기서 끼어들면 안 된다.
    }

    @Unique
    private void tacz$resetAll(ModelPart part) {
        part.xRot = 0.0F;
        part.yRot = 0.0F;
        part.zRot = 0.0F;
    }
}

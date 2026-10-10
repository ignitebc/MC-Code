package com.tacz.guns.mixin.client;

import com.tacz.guns.client.animation.third.InnerThirdPersonManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * [42차: 영구 폐기로 확정, 더는 등록을 시도하지 않음]
 *
 * <p>이 mixin의 역할은 <b>플레이어가 아닌 인간형 생물</b>(좀비, 주민, 스켈레톤 등)이 총을 들었을 때도
 * TACZ의 3인칭 팔 자세를 적용하는 것이다. 플레이어 자신의 자세는 이미 등록된
 * {@code PlayerModelMixin}({@code setupAnim(AvatarRenderState)}에 주입)이 맡으며 영향을 받지 않는다.</p>
 *
 * <h2>26.2에서 바로 등록할 수 없는 이유</h2>
 * 선언한 주입 지점 {@code setupAnim(LivingEntity,FFFFF)V}는 1.21.1의 예전 시그니처다.
 * 26.2의 {@code HumanoidModel}에는 오버로드가 두 개만 남았다(제네릭 시그니처 대조):
 * <pre>
 *   setupAnim(HumanoidRenderState)V     // 실제 구현
 *   setupAnim(Object)V                  // EntityModel의 브리지 메서드
 * </pre>
 * 바로 등록하면 대상을 찾지 못해 <b>시작할 때 충돌한다</b>.
 *
 * <h2>{@code setupAnim(HumanoidRenderState)}로 고쳐 쓰지도 않는 이유</h2>
 * 시그니처를 바꾸기는 쉽지만 엔티티를 얻을 수 없다 — 이것은 26.2 render-state 구조의 강제 제약이다:
 * <ul>
 *   <li>{@code InnerThirdPersonManager#setRotationAnglesHead}에는
 *       {@code LivingEntity}가 필요하다. {@code IGunOperator}의 조준/재장전 상태와 주 손 아이템을 읽기 위해서다;</li>
 *   <li>그러나 {@code HumanoidRenderState}부터 최상위 클래스 {@code EntityRenderState}까지
 *       <b>엔티티 참조도 엔티티 ID도 없다</b>(필드마다 대조: {@code entityType} / {@code x,y,z} /
 *       {@code ageInTicks} 같은 스냅숏 데이터뿐).
 *       {@code PlayerModelMixin}이 동작하는 것은 {@code AvatarRenderState}가
 *       {@code id} 필드를 <b>추가로</b> 가져 엔티티를 거꾸로 찾을 수 있기 때문이다 — 인간형 공용 상태에는 이 필드가 없다.</li>
 * </ul>
 * 우회하려면 "좌표 + 종류"로 엔티티를 거꾸로 찾는 방식을 직접 만들어야 하는데, 믿을 수도 없고(같은 좌표에 엔티티 여럿)
 * 프레임마다 렌더링 중에 엔티티를 찾아야 해서 들이는 비용에 비해 얻는 것이 너무 적다.
 *
 * <h2>영향 범위는 작다</h2>
 * 총기 <b>아이템 자체</b>는 플레이어가 아닌 인간형의 손에서도 정상으로 그려진다 —
 * 그것은 {@code ItemInHandLayerMixin}의 몫이며, 그것이 주입하는
 * {@code submit(...ArmedEntityRenderState...)}는 모든 인간형 생물을 다루고 이미 등록되어 정상 동작한다.
 * 이 mixin이 없어 영향을 받는 것은 <b>팔 자세</b>뿐이다: 좀비가 총을 들면 TACZ의 총 든 자세가 아니라
 * 바닐라 팔 자세가 된다. 보기의 세부 사항일 뿐 어떤 게임 방식에도 영향이 없다.
 *
 * <p>소스는 참고용으로만 남긴다. 나중에 Mojang이 render state에 엔티티 참조를 추가하면 이를 바탕으로 다시 쓸 수 있다.</p>
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow
    @Final
    public ModelPart head;
    @Shadow
    @Final
    public ModelPart body;
    @Shadow
    @Final
    public ModelPart leftArm;
    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "TAIL"))
    private void setRotationAnglesHead(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (ageInTicks == 0) {
            return;
        }
        InnerThirdPersonManager.setRotationAnglesHead(entityIn, rightArm, leftArm, body, head, limbSwingAmount);
    }
}

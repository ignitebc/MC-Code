package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.renderer.other.HumanoidOffhandRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2 맞춤 설명(1.21.1 원본의 같은 이름 mixin과 디컴파일한 {@code ItemInHandLayer}를 대조).
 *
 * <p><b>오랫동안 잘못 퍼진 결론 하나를 바로잡는다</b>: {@code submitArmWithItem}을 취소해도 팔이 사라지지 <em>않는다</em>.
 * 디컴파일 소스를 보면 이 메서드는 <b>손에 든 아이템</b>을 렌더링에 제출하는 것({@code item.submit(...)})만 맡고,
 * 팔 자체는 {@code PlayerModel}/{@code HumanoidModel}이 엔티티 모델 단계에서 그리므로 둘은 서로 관계가 없다.
 * 그래서 "3인칭 팔이 사라짐"을 이 mixin 탓으로 돌리는 것은 정확하지 않다. 이 mixin을 취소했을 때 실제로 사라지는 것은
 * <b>보조 손 아이템</b>이며, 이는 원본이 일부러 그렇게 한 것이다(주 손에 총을 들면 보조 손 아이템을 그리지 않음). 그리고
 * {@link HumanoidOffhandRender}가 "몸에 멘" 자세로 다시 그려 준다.</p>
 *
 * <p>이번 차수에 고친 실제 결함 세 가지:</p>
 * <ol>
 *   <li><b>취소 조건이 틀렸다.</b> 예전 코드는 {@code arm == HumanoidArm.LEFT}로 판단했지만 {@code LEFT}가
 *       보조 손과 같지는 않다 — 왼손잡이 플레이어의 주 손이 바로 {@code LEFT}다. 원본은
 *       "주 손에 총을 들었고 &amp;&amp; 현재 arm이 주 손이 아님"을 썼다. 여기서는 {@code state.mainArm}으로 판정하도록 바꿔
 *       왼손잡이 플레이어의 <b>주 손 총기가 그려지지 않는</b> 문제를 고쳤다.</li>
 *   <li><b>{@code isSelf}를 false로만 두고 true로 두지 않았다.</b> 원본은 {@code renderArmWithItem}의 HEAD에서
 *       "그리는 대상이 로컬 플레이어"이면 {@code true}로 두는데, 예전 이식본이 이 부분을 빠뜨려 3인칭에서
 *       탄피 배출/총구 화염의 자기 판정이 항상 false였다. 원본대로 되살렸다.</li>
 *   <li><b>{@code HumanoidOffhandRender.renderGun}이 빈 구현이었다.</b> 26.2의
 *       extract → submit 두 단계 방식으로 다시 구현했다. 그 클래스 주석 참고.</li>
 * </ol>
 */
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;FF)V", at = @At(value = "TAIL"))
    private void submitTail(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, ArmedEntityRenderState state, float p_117185_, float p_117186_, CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
        HumanoidOffhandRender.renderGun(state, poseStack, collector, packedLight);
    }

    @Inject(method = "submitArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void submitArmWithItemHead(ArmedEntityRenderState state, ItemStackRenderState itemState, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, CallbackInfo ci) {
        // 원본 의미: 로컬 플레이어를 그릴 때 자기 탄피 배출/총구 화염 판정을 켠다.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && state instanceof AvatarRenderState avatarState && avatarState.id == minecraft.player.getId()) {
            MuzzleFlashRender.isSelf = true;
            ShellRender.isSelf = true;
        }

        // 원본 의미: 주 손에 총을 들면 "보조 손" 아이템의 일반 렌더링을 취소하고, HumanoidOffhandRender가 등에 멘 자세로 그린다.
        // 보조 손은 반드시 mainArm으로 판정해야 하며 LEFT로 고정하면 안 된다(왼손잡이 플레이어의 주 손이 LEFT다).
        ItemStack mainHand = state.getMainHandItemStack();
        if (mainHand != null && IGun.getIGunOrNull(mainHand) != null && arm != state.mainArm) {
            ci.cancel();
        }
    }

    @Inject(method = "submitArmWithItem", at = @At(value = "TAIL"))
    private void submitArmWithItemTail(ArmedEntityRenderState state, ItemStackRenderState itemState, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
    }
}

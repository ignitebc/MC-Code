package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.client.MonsterLevelLabel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 모든 엔티티 렌더러의 공통 단계에 몬스터 레벨 표시를 붙인다.
 * <p>
 * 하위 렌더러는 상태 추출과 그리기 모두 이 클래스의 메서드를 super로 호출하므로 한곳에서 처리된다.
 * 이름표 거리 계산이 상태 추출 안에서 끝나므로 추출의 마지막에서 레벨을 담는다.
 */
@Mixin(EntityRenderer.class)
abstract class MonsterLevelLabelMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void serverutilities$extractMonsterLevel(Entity entity, EntityRenderState state, float partialTick,
                                                     CallbackInfo ci) {
        MonsterLevelLabel.extract(entity, state, partialTick);
    }

    @Inject(method = "submit", at = @At("TAIL"))
    private void serverutilities$submitMonsterLevel(EntityRenderState state, PoseStack poseStack,
                                                    SubmitNodeCollector collector, CameraRenderState camera,
                                                    CallbackInfo ci) {
        MonsterLevelLabel.submit(state, poseStack, collector, camera);
    }
}

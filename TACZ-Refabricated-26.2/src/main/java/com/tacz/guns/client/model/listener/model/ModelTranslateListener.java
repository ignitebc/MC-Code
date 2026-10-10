package com.tacz.guns.client.model.listener.model;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.resource.pojo.model.BonesItem;

import javax.annotation.Nullable;

public class ModelTranslateListener implements AnimationListener {
    private final ModelRendererWrapper rendererWrapper;
    private final @Nullable BonesItem bonesItem;

    public ModelTranslateListener(BedrockAnimatedModel model, ModelRendererWrapper rendererWrapper, String nodeName) {
        this.rendererWrapper = rendererWrapper;
        // 현재 node가 루트 node(곧 shouldRender에 들어 있음)면 bonesItem을 얻어 뒤의 상대 이동 offset 계산에 쓴다.
        if (model.getShouldRender().contains(rendererWrapper.getModelRenderer())) {
            this.bonesItem = model.getIndexBones().get(nodeName);
        } else {
            this.bonesItem = null;
        }
    }

    @Override
    public void update(float[] values, boolean blend) {
        if (blend) {
            // 구속 그룹 애니메이션은 특수한 값이라 섞지 않는다
            rendererWrapper.addOffsetX(values[0]);
            rendererWrapper.addOffsetY(-values[1]);
            rendererWrapper.addOffsetZ(values[2]);
        } else {
            rendererWrapper.setOffsetX(values[0]);
            rendererWrapper.setOffsetY(-values[1]);
            rendererWrapper.setOffsetZ(values[2]);
        }
    }

    @Override
    public float[] initialValue() {
        // offset이 0으로 전환되게 하는 것이 목표다
        float[] recover = new float[3];
        if (bonesItem != null) {
            recover[0] = bonesItem.getPivot().get(0) / 16f;
            recover[1] = -bonesItem.getPivot().get(1) / 16f;
            recover[2] = bonesItem.getPivot().get(2) / 16f;
        } else {
            recover[0] = rendererWrapper.getRotationPointX() / 16f;
            recover[1] = rendererWrapper.getRotationPointY() / 16f;
            recover[2] = rendererWrapper.getRotationPointZ() / 16f;
        }
        return recover;
    }

    @Override
    public ObjectAnimationChannel.ChannelType getType() {
        return ObjectAnimationChannel.ChannelType.TRANSLATION;
    }
}

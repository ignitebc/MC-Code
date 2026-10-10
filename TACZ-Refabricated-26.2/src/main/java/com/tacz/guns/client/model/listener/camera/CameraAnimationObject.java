package com.tacz.guns.client.model.listener.camera;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.AnimationListenerSupplier;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import org.joml.Quaternionf;

import static com.tacz.guns.client.model.BedrockAnimatedModel.CAMERA_NODE_NAME;

public class CameraAnimationObject implements AnimationListenerSupplier {
    /**
     * 이 사원수에 담긴 회전은 카메라가 아니라 월드 상자의 회전이다(둘은 서로 반대다)
     */
    public Quaternionf rotationQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);

    /**
     * 카메라 노드가 루트이면 cameraRenderer는 비어 있다
     */
    public ModelRendererWrapper cameraRenderer;

    @Override
    public AnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
        if (!nodeName.equals(CAMERA_NODE_NAME)) {
            return null;
        }
        if (type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
            return new CameraRotateListener(this);
        }
        return null;
    }
}


package com.tacz.guns.client.model.listener.camera;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.util.math.MathUtil;
import org.joml.Quaternionf;

public class CameraRotateListener implements AnimationListener {
    private final CameraAnimationObject camera;

    public CameraRotateListener(CameraAnimationObject camera) {
        this.camera = camera;
    }

    @Override
    public void update(float[] values, boolean blend) {
        if (values.length == 4) {
            values = MathUtil.toEulerAngles(values);
        }
        float xRot = values[0];
        float yRot = values[1];
        float zRot = -values[2];
        // 키프레임에 저장된 회전 값은 카메라가 아니라 월드 상자의 회전 값이다
        // 결국 rotationQuaternion에 넣어야 하는 것은 카메라의 회전(곧 월드 상자 회전의 반대)이다
        if (blend) {
            float[] q = MathUtil.toQuaternion(xRot, yRot, zRot);
            Quaternionf quaternion = MathUtil.toQuaternion(q);
            MathUtil.blendQuaternion(camera.rotationQuaternion, quaternion);
        } else {
            MathUtil.toQuaternion(xRot, yRot, zRot, camera.rotationQuaternion);
        }
    }

    @Override
    public float[] initialValue() {
        return MathUtil.toQuaternion(camera.cameraRenderer.getRotateAngleX(), camera.cameraRenderer.getRotateAngleY(), camera.cameraRenderer.getRotateAngleZ());
    }

    @Override
    public ObjectAnimationChannel.ChannelType getType() {
        return ObjectAnimationChannel.ChannelType.ROTATION;
    }
}

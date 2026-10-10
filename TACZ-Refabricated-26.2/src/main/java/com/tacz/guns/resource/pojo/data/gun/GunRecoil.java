package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import javax.annotation.Nullable;

public class GunRecoil {
    private static final SplineInterpolator INTERPOLATOR = new SplineInterpolator();

    @SerializedName("pitch")
    @Nullable
    private GunRecoilKeyFrame[] pitch;

    @SerializedName("yaw")
    @Nullable
    private GunRecoilKeyFrame[] yaw;

    public GunRecoilKeyFrame[] getPitch() {
        return pitch;
    }

    public void setPitch(@Nullable GunRecoilKeyFrame[] pitch) {
        this.pitch = pitch;
    }

    public GunRecoilKeyFrame[] getYaw() {
        return yaw;
    }

    public void setYaw(@Nullable GunRecoilKeyFrame[] yaw) {
        this.yaw = yaw;
    }

    /**
     * 무작위로 값을 고르고 크기를 조정한 카메라 수직 반동의 스플라인 보간 함수를 돌려준다.
     *
     * @param modifier 부착물의 반동 수정값
     * @return 스플라인 보간 함수
     */
    @Nullable
    public PolynomialSplineFunction genPitchSplineFunction(float modifier) {
        return getSplineFunction(pitch, modifier);
    }

    /**
     * 무작위로 값을 고르고 크기를 조정한 카메라 수평 반동의 스플라인 보간 함수를 돌려준다.
     *
     * @param modifier 부착물의 반동 수정값
     * @return 스플라인 보간 함수
     */
    @Nullable
    public PolynomialSplineFunction genYawSplineFunction(float modifier) {
        return getSplineFunction(yaw, modifier);
    }

    private PolynomialSplineFunction getSplineFunction(GunRecoilKeyFrame[] keyFrames, float modifier) {
        if (keyFrames == null || keyFrames.length == 0) {
            return null;
        }
        double[] values = new double[keyFrames.length + 1];
        double[] times = new double[keyFrames.length + 1];
        times[0] = 0;
        values[0] = 0;
        for (int i = 0; i < keyFrames.length; i++) {
            times[i + 1] = keyFrames[i].getTime() * 1000 + 30;
        }
        for (int i = 0; i < keyFrames.length; i++) {
            float[] value = keyFrames[i].getValue();
            values[i + 1] = (value[0] + Math.random() * (value[1] - value[0])) * modifier;
        }
        return INTERPOLATOR.interpolate(times, values);
    }
}

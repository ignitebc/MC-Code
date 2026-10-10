package com.tacz.guns.api.client.animation.interpolator;

import com.tacz.guns.api.client.animation.AnimationChannelContent;

/**
 * 사원수용 선형 보간.
 */
public class SLerp implements Interpolator {
    private AnimationChannelContent content;

    @Override
    public void compile(AnimationChannelContent content) {
        this.content = content;
    }

    @Override
    public float[] interpolate(int indexFrom, int indexTo, float alpha) {
        // 회전 값이 8개면 뒤의 4개는 Post 값이며 보간 시작점으로 쓴다
        int offset = content.values[indexFrom].length == 8 ? 4 : 0;
        float ax = content.values[indexFrom][offset];
        float ay = content.values[indexFrom][1 + offset];
        float az = content.values[indexFrom][2 + offset];
        float aw = content.values[indexFrom][3 + offset];
        float bx = indexFrom == indexTo ? content.values[indexFrom][offset] : content.values[indexTo][0];
        float by = indexFrom == indexTo ? content.values[indexFrom][1 + offset] : content.values[indexTo][1];
        float bz = indexFrom == indexTo ? content.values[indexFrom][2 + offset] : content.values[indexTo][2];
        float bw = indexFrom == indexTo ? content.values[indexFrom][3 + offset] : content.values[indexTo][3];

        float[] result = new float[4];

        float dot = ax * bx + ay * by + az * bz + aw * bw;
        if (dot < 0) {
            bx = -bx;
            by = -by;
            bz = -bz;
            bw = -bw;
            dot = -dot;
        }
        float epsilon = 1e-6f;
        float s0, s1;
        if ((1.0 - dot) > epsilon) {
            float omega = (float) Math.acos(dot);
            float invSinOmega = 1.0f / (float) Math.sin(omega);
            s0 = (float) Math.sin((1.0 - alpha) * omega) * invSinOmega;
            s1 = (float) Math.sin(alpha * omega) * invSinOmega;
        } else {
            s0 = 1.0f - alpha;
            s1 = alpha;
        }
        float rx = s0 * ax + s1 * bx;
        float ry = s0 * ay + s1 * by;
        float rz = s0 * az + s1 * bz;
        float rw = s0 * aw + s1 * bw;
        result[0] = rx;
        result[1] = ry;
        result[2] = rz;
        result[3] = rw;
        return result;
    }

    @Override
    public SLerp clone() {
        try {
            SLerp sLerp = (SLerp) super.clone();
            sLerp.content = this.content;
            return sLerp;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

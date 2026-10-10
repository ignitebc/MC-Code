package com.tacz.guns.api.client.animation.interpolator;

import com.tacz.guns.api.client.animation.AnimationChannelContent;

public class Linear implements Interpolator {
    private AnimationChannelContent content;

    @Override
    public void compile(AnimationChannelContent content) {
        this.content = content;
    }

    @Override
    public float[] interpolate(int indexFrom, int indexTo, float alpha) {
        // 애니메이션 값이 6개면 뒤의 3개는 Post 값이며 보간 시작점으로 쓴다
        int offset = content.values[indexFrom].length == 6 ? 3 : 0;
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            if (indexFrom == indexTo) {
                result[i] = content.values[indexFrom][i + offset];
            } else {
                result[i] = content.values[indexFrom][i + offset] * (1 - alpha) + content.values[indexTo][i] * alpha;
            }
        }
        return result;
    }

    @Override
    public Linear clone() {
        try {
            Linear linear = (Linear) super.clone();
            linear.content = this.content;
            return linear;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

package com.tacz.guns.api.client.animation;

import java.util.Arrays;

public class AnimationChannelContent {
    public float[] keyframeTimeS;
    /**
     * 애니메이션 값. 배열의 첫 번째 차원은 위의 keyframeTime 순서와 하나씩 대응하고,
     * 두 번째 차원은 이동·회전·크기 값이다. 사원수라면 배열 길이가 8 또는 4다(8이면 앞 넷은 Pre 값, 뒤 넷은 Post 값).
     * 세 축 값이라면 배열 길이가 6 또는 3이며, Pre와 Post는 위와 같다.
     */
    public float[][] values;
    /**
     * 일반 보간기를 쓰는 Channel에서는 이 애니메이션 값이 의미가 없다. CustomInterpolator 전용이다
     */
    public LerpMode[] lerpModes;

    public AnimationChannelContent() {
    }

    public AnimationChannelContent(AnimationChannelContent source) {
        if (source.keyframeTimeS != null) {
            this.keyframeTimeS = Arrays.copyOf(source.keyframeTimeS, source.keyframeTimeS.length);
        }
        if (source.values != null) {
            // 애니메이션 값을 깊은 복사한다
            this.values = Arrays.stream(source.values)
                    .map(values -> Arrays.copyOf(values, values.length))
                    .toArray(float[][]::new);
        }
        if (source.lerpModes != null) {
            this.lerpModes = Arrays.copyOf(source.lerpModes, source.lerpModes.length);
        }
    }

    public enum LerpMode {
        LINEAR, SPHERICAL_LINEAR, CATMULLROM, SPHERICAL_SQUAD
    }
}

package com.tacz.guns.api.client.animation;

import com.tacz.guns.api.client.animation.interpolator.Interpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectAnimationChannel {
    public final ChannelType type;
    private final List<AnimationListener> listeners = new ArrayList<>();
    /**
     * 노드 이름
     */
    public String node;
    /**
     * 키프레임을 포함한 이 트랙의 내용
     */
    public AnimationChannelContent content;
    public Interpolator interpolator;
    /**
     * 애니메이션 전환에 쓰는 변수다.
     * 무엇을 하는지 모르면 바꾸지 않는다
     */
    boolean transitioning = false;

    public ObjectAnimationChannel(ChannelType type) {
        this.type = type;
        this.content = new AnimationChannelContent();
    }

    public ObjectAnimationChannel(ChannelType type, AnimationChannelContent content) {
        this.type = type;
        this.content = content;
    }

    public void addListener(AnimationListener listener) {
        if (listener.getType().equals(type)) {
            listeners.add(listener);
        } else {
            throw new RuntimeException("trying to add wrong type of listener to channel.");
        }
    }

    public void removeListener(AnimationListener listener) {
        listeners.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public List<AnimationListener> getListeners() {
        return listeners;
    }

    public float getEndTimeS() {
        if (content.keyframeTimeS.length == 0) {
            return 0;
        }
        return content.keyframeTimeS[content.keyframeTimeS.length - 1];
    }

    /**
     * 입력 시간으로 계산하고 결과를 모든 AnimationListener에 알린다
     *
     * @param timeS 절대 시간(초)
     */
    public void update(float timeS, boolean blend) {
        if (!transitioning) {
            float[] result = getResult(timeS);
            for (AnimationListener listener : listeners) {
                listener.update(result, blend);
            }
        }
    }

    public float[] getResult(float timeS) {
        int indexFrom = computeIndex(timeS);
        int indexTo = Math.min(content.keyframeTimeS.length - 1, indexFrom + 1);
        float alpha = computeAlpha(timeS, indexFrom);
        return interpolator.interpolate(indexFrom, indexTo, alpha);
    }

    private int computeIndex(float timeS) {
        int index = Arrays.binarySearch(content.keyframeTimeS, timeS);
        if (index >= 0) {
            return index;
        }
        return Math.max(0, -index - 2);
    }

    private float computeAlpha(float timeS, int indexFrom) {
        if (timeS <= content.keyframeTimeS[0]) {
            return 0.0f;
        }
        if (timeS >= content.keyframeTimeS[content.keyframeTimeS.length - 1]) {
            return 1.0f;
        }
        float local = timeS - content.keyframeTimeS[indexFrom];
        float delta = content.keyframeTimeS[indexFrom + 1] - content.keyframeTimeS[indexFrom];
        // 방어 코드: 두 키프레임의 시간이 같으면 delta=0이므로 바로 다음 프레임으로 넘어간다
        if (delta <= 0.0f) {
            return 1.0f;
        }
        return local / delta;
    }

    public enum ChannelType {
        /**
         * 이동
         */
        TRANSLATION,
        /**
         * 회전
         */
        ROTATION,
        /**
         * 크기
         */
        SCALE
    }
}

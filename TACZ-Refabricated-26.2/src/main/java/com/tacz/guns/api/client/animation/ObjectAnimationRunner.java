package com.tacz.guns.api.client.animation;

import com.tacz.guns.util.math.MathUtil;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ObjectAnimationRunner {
    @Nonnull
    private final ObjectAnimation animation;
    protected long transitionTimeNs;
    /**
     * 애니메이션 전환용. 전환 시작 애니메이션의 값을 담으며, 아래 transitionFromChannels와 하나씩 대응한다
     */
    protected ArrayList<float[]> valueFrom;
    /**
     * 애니메이션 전환용. 전환 시작점에서 원위치로 돌려야 하는 channel의 값을 담으며, 아래 recoverChannels와 하나씩 대응한다
     */
    protected ArrayList<float[]> valueRecover;
    /**
     * 애니메이션 전환용. 전환 시작 애니메이션의 channel을 담는다
     */
    protected ArrayList<ObjectAnimationChannel> transitionFromChannels;
    /**
     * 애니메이션 전환용. 전환 목표 애니메이션의 channel을 담으며, 순서는 위와 대응한다
     */
    protected ArrayList<ObjectAnimationChannel> transitionToChannels;
    /**
     * 애니메이션 전환용. 전환 시작 애니메이션에서 원위치로 돌려야 하는 channel을 담는다
     */
    protected ArrayList<ObjectAnimationChannel> recoverChannels;
    private boolean running = false;
    private boolean pausing = false;
    private long lastUpdateNs;
    /**
     * 현재 애니메이션 재생 진행도
     */
    private long progressNs;
    private boolean isTransitioning = false;
    @Nullable
    private ObjectAnimationRunner transitionTo;
    private long transitionProgressNs;

    public ObjectAnimationRunner(@Nonnull ObjectAnimation animation) {
        this.animation = Objects.requireNonNull(animation);
    }

    public @Nonnull ObjectAnimation getAnimation() {
        return animation;
    }

    public @Nullable ObjectAnimationRunner getTransitionTo() {
        return transitionTo;
    }

    public boolean isTransitioning() {
        return isTransitioning;
    }

    public void run() {
        if (!running) {
            running = true;
            lastUpdateNs = System.nanoTime();
        }
        pausing = false;
    }

    public void pause() {
        running = false;
        pausing = true;
    }

    public void hold() {
        progressNs = (long) (animation.getMaxEndTimeS() * 1e9) + 1;
        running = false;
    }

    public void stop() {
        progressNs = (long) (animation.getMaxEndTimeS() * 1e9) + 2;
        running = false;
    }

    public void reset() {
        progressNs = 0;
    }

    public long getProgressNs() {
        return progressNs;
    }

    public void setProgressNs(long progressNs) {
        this.progressNs = progressNs;
    }

    public void transition(ObjectAnimationRunner transitionTo, long transitionTimeNS) {
        if (this.transitionTo == null) {
            this.valueFrom = new ArrayList<>();
            this.valueRecover = new ArrayList<>();
            this.transitionFromChannels = new ArrayList<>();
            this.transitionToChannels = new ArrayList<>();
            this.recoverChannels = new ArrayList<>();
            this.transitionTo = transitionTo;
            this.running = false;
            for (Map.Entry<String, List<ObjectAnimationChannel>> entry : animation.getChannels().entrySet()) {
                List<ObjectAnimationChannel> toChannels = transitionTo.animation.getChannels().get(entry.getKey());
                if (toChannels != null) {
                    // 전환 목표 애니메이션의 같은 node에 같은 종류(이동·회전·크기)의 데이터가 있으면 갱신용 목록에 넣는다.
                    for (ObjectAnimationChannel channel : entry.getValue()) {
                        Optional<ObjectAnimationChannel> toChannel =
                                toChannels.stream().filter(c -> c.type.equals(channel.type)).findAny();
                        float[] value = channel.getResult(progressNs / 1e9f);
                        if (channel.type == ObjectAnimationChannel.ChannelType.ROTATION && value.length == 3) {
                            value = MathUtil.toQuaternion(value[0], value[1], value[2]);
                        }
                        if (toChannel.isPresent()) {
                            valueFrom.add(value);
                            transitionFromChannels.add(channel);
                            transitionToChannels.add(toChannel.get());
                            // 전환 목표 channel이 모델을 갱신하지 않게 하고, 시작점 channel에서 한꺼번에 갱신한다.
                            toChannel.get().transitioning = true;
                        } else {
                            valueRecover.add(value);
                            recoverChannels.add(channel);
                        }
                    }
                } else {
                    // 전환 목표 애니메이션의 같은 node에 데이터가 없으면 원위치로 전환한다.
                    for (ObjectAnimationChannel channel : entry.getValue()) {
                        float[] value = channel.getResult(progressNs / 1e9f);
                        if (channel.type == ObjectAnimationChannel.ChannelType.ROTATION && value.length == 3) {
                            value = MathUtil.toQuaternion(value[0], value[1], value[2]);
                        }
                        valueRecover.add(value);
                        recoverChannels.add(channel);
                    }
                }
            }
        } else if (isTransitioning) {
            ArrayList<float[]> newValueFrom = new ArrayList<>();
            ArrayList<float[]> newValueRecover = new ArrayList<>();
            ArrayList<ObjectAnimationChannel> newTransitionFromChannels = new ArrayList<>();
            ArrayList<ObjectAnimationChannel> newTransitionToChannels = new ArrayList<>();
            ArrayList<ObjectAnimationChannel> newRecoverChannels = new ArrayList<>();
            // 전환 중이면 지금 계산한 보간값을 저장해 다음 전환의 시작점으로 쓴다
            for (int i = 0; i < transitionFromChannels.size(); i++) {
                assert this.transitionTo != null;
                ObjectAnimationChannel fromChannel = transitionFromChannels.get(i);
                ObjectAnimationChannel toChannel = transitionToChannels.get(i);
                float[] from = valueFrom.get(i);
                float[] to = toChannel.getResult(this.transitionTo.progressNs / 1e9f);
                float[] result;
                float progress = easeOutCubic((float) transitionProgressNs / transitionTimeNs);
                if (fromChannel.type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
                    result = new float[3];
                    lerp(from, to, progress, result);
                } else if (fromChannel.type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
                    result = new float[4];
                    if (to.length == 3) {
                        to = MathUtil.toQuaternion(to[0], to[1], to[2]);
                    }
                    slerp(from, to, progress, result);
                } else { // 크기
                    result = new float[3];
                    lerp(from, to, progress, result);
                }

                List<ObjectAnimationChannel> newToChannels = transitionTo.animation.getChannels().get(fromChannel.node);
                if (newToChannels != null) {
                    Optional<ObjectAnimationChannel> newToChannel =
                            newToChannels.stream().filter(c -> c.type.equals(fromChannel.type)).findAny();
                    if (newToChannel.isPresent()) {
                        newValueFrom.add(result);
                        newTransitionFromChannels.add(fromChannel);
                        newTransitionToChannels.add(newToChannel.get());
                        // 전환 목표 channel이 모델을 갱신하지 않게 하고, 시작점 channel에서 한꺼번에 갱신한다.
                        newToChannel.get().transitioning = true;
                    } else {
                        newValueRecover.add(result);
                        newRecoverChannels.add(fromChannel);
                    }
                } else {
                    newValueRecover.add(result);
                    newRecoverChannels.add(fromChannel);
                }
                toChannel.transitioning = false;
            }
            this.valueFrom = newValueFrom;
            this.valueRecover = newValueRecover;
            this.transitionToChannels = newTransitionToChannels;
            this.transitionFromChannels = newTransitionFromChannels;
            this.recoverChannels = newRecoverChannels;
            this.transitionTo = transitionTo;
        }
        this.transitionTimeNs = transitionTimeNS;
        this.transitionProgressNs = 0;
        this.isTransitioning = true;
    }

    public long getTransitionTimeNs() {
        return transitionTimeNs;
    }

    public long getTransitionProgressNs() {
        return transitionProgressNs;
    }

    public void setTransitionProgressNs(long progressNs) {
        this.transitionProgressNs = progressNs;
    }

    public void stopTransition() {
        this.isTransitioning = false;
        for (ObjectAnimationChannel channel : transitionToChannels) {
            channel.transitioning = false;
        }
        this.transitionTimeNs = 0;
        this.transitionProgressNs = 0;
        this.transitionFromChannels = null;
        this.transitionToChannels = null;
        this.recoverChannels = null;
        this.valueFrom = null;
        this.valueRecover = null;
    }

    private void updateProgress(long alphaProgress) {
        if (running) {
            progressNs += alphaProgress;
        }
        switch (animation.playType) {
            case PLAY_ONCE_HOLD -> {
                if (progressNs / 1e9 > animation.getMaxEndTimeS()) {
                    hold();
                }
            }
            case PLAY_ONCE_STOP -> {
                if (progressNs / 1e9 > animation.getMaxEndTimeS()) {
                    stop();
                }
            }
            case LOOP -> {
                if (progressNs / 1e9 > animation.getMaxEndTimeS()) {
                    if (animation.getMaxEndTimeS() == 0) {
                        progressNs = 0;
                    } else {
                        progressNs = progressNs % (long) (animation.getMaxEndTimeS() * 1e9);
                    }
                }
            }
        }
    }

    public void update(boolean blend) {
        long fromTimeNs = progressNs;
        long currentNs = System.nanoTime();
        long alphaProgress = currentNs - lastUpdateNs;
        updateProgress(alphaProgress);
        lastUpdateNs = currentNs;
        if (isTransitioning) {
            transitionProgressNs += alphaProgress;
            if (transitionProgressNs >= transitionTimeNs) {
                stopTransition();
            } else {
                float transitionProgress = (float) transitionProgressNs / transitionTimeNs;
                updateTransition(easeOutCubic(transitionProgress), blend);
            }
        } else {
            animation.update(blend, progressNs);
            ObjectAnimationSoundChannel soundChannel = animation.getSoundChannel();
            if (soundChannel != null && Minecraft.getInstance().player != null) {
                soundChannel.playSound(fromTimeNs / 1e9, progressNs / 1e9, Minecraft.getInstance().player, 16, 1, 1);
            }
        }
    }

    public void updateSoundOnly() {
        long fromTimeNs = progressNs;
        long currentNs = System.nanoTime();
        updateProgress(currentNs - lastUpdateNs);
        lastUpdateNs = currentNs;
        ObjectAnimationSoundChannel soundChannel = animation.getSoundChannel();
        if (soundChannel != null && Minecraft.getInstance().player != null) {
            soundChannel.playSound(fromTimeNs / 1e9, progressNs / 1e9, Minecraft.getInstance().player, 16, 1, 1);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPausing() {
        return pausing;
    }

    public boolean isHolding() {
        return progressNs == (long) (getAnimation().getMaxEndTimeS() * 1e9) + 1;
    }

    public boolean isStopped() {
        return progressNs == (long) (getAnimation().getMaxEndTimeS() * 1e9) + 2;
    }

    /**
     * 애니메이션 전환 중에는 계산한 보간값을 현재 Runner의 ObjectAnimation에 든 channel로 모델에 적용한다.
     * 그래서 transitionTo의 해당 channel 갱신 기능을 잠시 꺼야 한다(available 변수를 false로 설정).
     */
    private void updateTransition(float progress, boolean blend) {
        assert transitionTo != null;
        for (int i = 0; i < transitionToChannels.size(); i++) {
            ObjectAnimationChannel fromChannel = transitionFromChannels.get(i);
            ObjectAnimationChannel toChannel = transitionToChannels.get(i);

            float[] from = valueFrom.get(i);
            float[] to = toChannel.getResult(transitionTo.progressNs / 1e9f);
            float[] result;

            if (fromChannel.type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
                result = new float[3];
                lerp(from, to, progress, result);
            } else if (fromChannel.type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
                result = new float[4];
                if (to.length == 3) {
                    to = MathUtil.toQuaternion(to[0], to[1], to[2]);
                }
                slerp(from, to, progress, result);
            } else { // 크기
                result = new float[3];
                lerp(from, to, progress, result);
            }
            for (AnimationListener listener : fromChannel.getListeners()) {
                listener.update(result, blend);
            }

        }
        if (animation.playType != ObjectAnimation.PlayType.PLAY_ONCE_STOP) { // PLAY_ONCE_STOP이면 애니메이션이 끝난 뒤 자기 키프레임을 갱신하면 안 되므로 복귀 전환을 하지 않는다
            for (int i = 0; i < recoverChannels.size(); i++) {
                ObjectAnimationChannel channel = recoverChannels.get(i);
                float[] from = valueRecover.get(i);
                float[] result;
                if (channel.type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
                    result = new float[3];
                    float[] to = new float[]{0, 0, 0};
                    for (AnimationListener listener : channel.getListeners()) {
                        lerp(from, to, progress, result);
                        listener.update(result, blend);
                    }
                } else if (channel.type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
                    result = new float[4];
                    float[] to = new float[]{0, 0, 0, 1};
                    for (AnimationListener listener : channel.getListeners()) {
                        slerp(from, to, progress, result);
                        listener.update(result, blend);
                    }
                } else if (channel.type.equals(ObjectAnimationChannel.ChannelType.SCALE)) {
                    result = new float[3];
                    float[] to = new float[]{1, 1, 1};
                    for (AnimationListener listener : channel.getListeners()) {
                        lerp(from, to, progress, result);
                        listener.update(result, blend);
                    }
                }
            }
        }
    }

    private float easeOutCubic(double x) {
        return (float) (1 - Math.pow(1 - x, 4));
    }

    private void lerp(float[] from, float[] to, float alpha, float[] result) {
        for (int i = 0; i < result.length; i++) {
            result[i] = from[i] * (1 - alpha) + to[i] * alpha;
        }
    }

    private void slerp(float[] from, float[] to, float alpha, float[] result) {
        float ax = from[0];
        float ay = from[1];
        float az = from[2];
        float aw = from[3];
        float bx = to[0];
        float by = to[1];
        float bz = to[2];
        float bw = to[3];

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
    }
}

package com.tacz.guns.api.client.animation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * {@link ObjectAnimation}을 실행할 {@link ObjectAnimationRunner} 인스턴스를 만든다
 */
public class ObjectAnimation {
    /**
     * 애니메이션 이름
     */
    public final String name;
    /**
     * 이 맵의 키는 노드 이름이다
     */
    private final Map<String, List<ObjectAnimationChannel>> channels = new HashMap<>();
    private @Nullable ObjectAnimationSoundChannel soundChannel;
    /**
     * 재생 방식
     */
    public @Nonnull PlayType playType = PlayType.PLAY_ONCE_HOLD;
    /**
     * 모든 트랙의 최대 종료 시간 {@link ObjectAnimationChannel#getEndTimeS()}
     */
    private float maxEndTimeS = 0f;

    protected ObjectAnimation(@Nonnull String name) {
        this.name = Objects.requireNonNull(name);
    }

    /**
     * 원본 객체 애니메이션의 복사본을 만든다.
     * 새 애니메이션의 값은 원본과 같지만,
     * 애니메이션 리스너는 하나도 들어 있지 않다.
     */
    public ObjectAnimation(ObjectAnimation source) {
        this.name = source.name;
        this.playType = source.playType;
        this.maxEndTimeS = source.maxEndTimeS;
        for (Map.Entry<String, List<ObjectAnimationChannel>> entry : source.channels.entrySet()) {
            List<ObjectAnimationChannel> newList = new ArrayList<>();
            for (ObjectAnimationChannel channel : entry.getValue()) {
                ObjectAnimationChannel newChannel = new ObjectAnimationChannel(channel.type, channel.content);
                newChannel.node = channel.node;
                newChannel.interpolator = channel.interpolator;
                newList.add(newChannel);
            }
            this.channels.put(entry.getKey(), newList);
        }
        if (source.soundChannel != null) {
            this.soundChannel = new ObjectAnimationSoundChannel(source.soundChannel.content);
        }
    }

    protected void addChannel(ObjectAnimationChannel channel) {
        channels.compute(channel.node, (node, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(channel);
            return list;
        });
        if (channel.getEndTimeS() > maxEndTimeS) {
            maxEndTimeS = channel.getEndTimeS();
        }
    }

    protected void setSoundChannel(@Nonnull ObjectAnimationSoundChannel soundChannel) {
        if (soundChannel.getEndTimeS() > maxEndTimeS) {
            maxEndTimeS = (float) soundChannel.getEndTimeS();
        }
        this.soundChannel = soundChannel;
    }

    public Map<String, List<ObjectAnimationChannel>> getChannels() {
        return channels;
    }

    @Nullable
    public ObjectAnimationSoundChannel getSoundChannel() {
        return this.soundChannel;
    }

    public void applyAnimationListeners(AnimationListenerSupplier supplier) {
        for (List<ObjectAnimationChannel> channelList : channels.values()) {
            for (ObjectAnimationChannel channel : channelList) {
                AnimationListener listener = supplier.supplyListeners(channel.node, channel.type);
                if (listener != null) {
                    channel.addListener(listener);
                }
            }
        }
    }

    /**
     * 모든 리스너를 호출해 관련 값을 갱신하게 한다
     */
    public void update(boolean blend, float timeNs) {
        for (List<ObjectAnimationChannel> channels : channels.values()) {
            for (ObjectAnimationChannel channel : channels) {
                channel.update(timeNs / 1e9f, blend);
            }
        }
    }

    public float getMaxEndTimeS() {
        return maxEndTimeS;
    }

    public enum PlayType {
        /**
         * 한 번 재생하고 마지막 프레임에 멈춘다
         */
        PLAY_ONCE_HOLD,
        /**
         * 한 번 재생하고 정지한다
         */
        PLAY_ONCE_STOP,
        /**
         * 반복 재생
         */
        LOOP
    }
}

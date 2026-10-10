package com.tacz.guns.api.client.animation;

public interface AnimationListener {
    /**
     * @param values ChannelType이 TRANSLATION이면 길이가 3이고 xyz 이동량을 담는다.
     *               ChannelType이 ROTATION이면 길이가 4 또는 3이다. 4면 사원수다.
     *               ChannelType이 SCALE이면 길이가 3이고 xyz 크기를 담는다.
     * @param blend  섞을 때는 애니메이션 값을 덮어쓰지 않고 누적해야 한다.
     */
    void update(float[] values, boolean blend);

    float[] initialValue();

    ObjectAnimationChannel.ChannelType getType();
}

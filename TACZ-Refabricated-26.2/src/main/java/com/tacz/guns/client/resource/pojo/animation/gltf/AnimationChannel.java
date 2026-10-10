package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AnimationChannel {
    /**
     * 대상 값을 계산하는 데 쓰는, 이 애니메이션 안 샘플러의
     * 인덱스. (필수)
     */
    private Integer sampler;
    /**
     * 애니메이션되는 속성의 설명자. (필수)
     */
    private AnimationChannelTarget target;

    /**
     * 대상 값을 계산하는 데 쓰는, 이 애니메이션 안 샘플러의
     * 인덱스. (필수)
     *
     * @return sampler
     */
    public Integer getSampler() {
        return this.sampler;
    }

    /**
     * 대상 값을 계산하는 데 쓰는, 이 애니메이션 안 샘플러의
     * 인덱스. (필수)
     *
     * @param sampler 설정할 sampler
     * @throws NullPointerException 주어진 값이 <code>null</code>일 때
     */
    public void setSampler(Integer sampler) {
        if (sampler == null) {
            throw new NullPointerException((("Invalid value for sampler: " + sampler) + ", may not be null"));
        }
        this.sampler = sampler;
    }

    /**
     * 애니메이션되는 속성의 설명자. (필수)
     *
     * @return target
     */
    public AnimationChannelTarget getTarget() {
        return this.target;
    }

    /**
     * 애니메이션되는 속성의 설명자. (필수)
     *
     * @param target 설정할 target
     * @throws NullPointerException 주어진 값이 <code>null</code>일 때
     */
    public void setTarget(AnimationChannelTarget target) {
        if (target == null) {
            throw new NullPointerException((("Invalid value for target: " + target) + ", may not be null"));
        }
        this.target = target;
    }
}

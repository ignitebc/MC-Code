package com.tacz.guns.client.resource.pojo.animation.gltf;

import java.util.ArrayList;
import java.util.List;

public class Animation {
    private String name;
    /**
     * 애니메이션 채널 배열. 애니메이션 채널은 애니메이션 샘플러와
     * 애니메이션 대상 속성을 묶는다. 같은 애니메이션의 서로 다른
     * 채널은 같은 대상을 가지면 **안 된다**.
     * (필수)<br>
     * 최소 항목 수: 1<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;애니메이션 샘플러와 애니메이션 대상 속성을 묶는
     * 애니메이션 채널. (선택)
     */
    private List<AnimationChannel> channels;

    /**
     * 애니메이션 샘플러 배열. 애니메이션 샘플러는 타임스탬프와
     * 출력 값 시퀀스를 묶고 보간 알고리즘을
     * 정의한다. (필수)<br>
     * 최소 항목 수: 1<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;타임스탬프와 출력 값 시퀀스를 묶고
     * 보간 알고리즘을 정의하는 애니메이션 샘플러. (선택)
     */
    private List<AnimationSampler> samplers;

    /**
     * 애니메이션 채널 배열. 애니메이션 채널은 애니메이션 샘플러와
     * 애니메이션 대상 속성을 묶는다. 같은 애니메이션의 서로 다른
     * 채널은 같은 대상을 가지면 **안 된다**.
     * (필수)<br>
     * 최소 항목 수: 1<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;애니메이션 샘플러와 애니메이션 대상 속성을 묶는
     * 애니메이션 채널. (선택)
     *
     * @return channels
     */
    public List<AnimationChannel> getChannels() {
        return this.channels;
    }

    /**
     * 애니메이션 채널 배열. 애니메이션 채널은 애니메이션 샘플러와
     * 애니메이션 대상 속성을 묶는다. 같은 애니메이션의 서로 다른
     * 채널은 같은 대상을 가지면 **안 된다**.
     * (필수)<br>
     * 최소 항목 수: 1<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;애니메이션 샘플러와 애니메이션 대상 속성을 묶는
     * 애니메이션 채널. (선택)
     *
     * @param channels 설정할 channels
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setChannels(List<AnimationChannel> channels) {
        if (channels == null) {
            throw new NullPointerException((("Invalid value for channels: " + channels) + ", may not be null"));
        }
        if (channels.size() < 1) {
            throw new IllegalArgumentException("Number of channels elements is < 1");
        }
        this.channels = channels;
    }

    /**
     * 주어진 channels를 추가한다. 이 인스턴스의 channels는 이전 요소 전체에
     * 새 요소를 더한 목록으로
     * 바뀐다.
     *
     * @param element 요소
     * @throws NullPointerException 주어진 요소가 <code>null</code>일 때
     */
    public void addChannels(AnimationChannel element) {
        if (element == null) {
            throw new NullPointerException("The element may not be null");
        }
        List<AnimationChannel> oldList = this.channels;
        List<AnimationChannel> newList = new ArrayList<AnimationChannel>();
        if (oldList != null) {
            newList.addAll(oldList);
        }
        newList.add(element);
        this.channels = newList;
    }

    /**
     * 주어진 channels를 제거한다. 이 인스턴스의 channels는 제거한 요소를 뺀
     * 이전 요소 전체를 담은 목록으로
     * 바뀐다.
     *
     * @param element 요소
     * @throws NullPointerException 주어진 요소가 <code>null</code>일 때
     */
    public void removeChannels(AnimationChannel element) {
        if (element == null) {
            throw new NullPointerException("The element may not be null");
        }
        List<AnimationChannel> oldList = this.channels;
        List<AnimationChannel> newList = new ArrayList<>();
        if (oldList != null) {
            newList.addAll(oldList);
        }
        newList.remove(element);
        this.channels = newList;
    }

    /**
     * 애니메이션 샘플러 배열. 애니메이션 샘플러는 타임스탬프와
     * 출력 값 시퀀스를 묶고 보간 알고리즘을
     * 정의한다. (필수)<br>
     * 최소 항목 수: 1<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;타임스탬프와 출력 값 시퀀스를 묶고
     * 보간 알고리즘을 정의하는 애니메이션 샘플러. (선택)
     *
     * @return samplers
     */
    public List<AnimationSampler> getSamplers() {
        return this.samplers;
    }

    /**
     * 애니메이션 샘플러 배열. 애니메이션 샘플러는 타임스탬프와
     * 출력 값 시퀀스를 묶고 보간 알고리즘을
     * 정의한다. (필수)<br>
     * 최소 항목 수: 1<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;타임스탬프와 출력 값 시퀀스를 묶고
     * 보간 알고리즘을 정의하는 애니메이션 샘플러. (선택)
     *
     * @param samplers 설정할 samplers
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setSamplers(List<AnimationSampler> samplers) {
        if (samplers == null) {
            throw new NullPointerException((("Invalid value for samplers: " + samplers) + ", may not be null"));
        }
        if (samplers.size() < 1) {
            throw new IllegalArgumentException("Number of samplers elements is < 1");
        }
        this.samplers = samplers;
    }

    /**
     * 주어진 samplers를 추가한다. 이 인스턴스의 samplers는 이전 요소 전체에
     * 새 요소를 더한 목록으로
     * 바뀐다.
     *
     * @param element 요소
     * @throws NullPointerException 주어진 요소가 <code>null</code>일 때
     */
    public void addSamplers(AnimationSampler element) {
        if (element == null) {
            throw new NullPointerException("The element may not be null");
        }
        List<AnimationSampler> oldList = this.samplers;
        List<AnimationSampler> newList = new ArrayList<>();
        if (oldList != null) {
            newList.addAll(oldList);
        }
        newList.add(element);
        this.samplers = newList;
    }

    /**
     * 주어진 samplers를 제거한다. 이 인스턴스의 samplers는 제거한 요소를 뺀
     * 이전 요소 전체를 담은 목록으로
     * 바뀐다.
     *
     * @param element 요소
     * @throws NullPointerException 주어진 요소가 <code>null</code>일 때
     */
    public void removeSamplers(AnimationSampler element) {
        if (element == null) {
            throw new NullPointerException("The element may not be null");
        }
        List<AnimationSampler> oldList = this.samplers;
        List<AnimationSampler> newList = new ArrayList<>();
        if (oldList != null) {
            newList.addAll(oldList);
        }
        newList.remove(element);
        this.samplers = newList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

package com.tacz.guns.api.client.animation.gltf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AnimationModel {
    /**
     * 이 애니메이션의
     * {@link Channel} 인스턴스들
     */
    private final List<Channel> channels = new ArrayList<>();
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 주어진 {@link Channel}을 추가한다
     *
     * @param channel {@link Channel}
     */
    public void addChannel(Channel channel) {
        Objects.requireNonNull(channel, "The channel may not be null");
        this.channels.add(channel);
    }

    public List<Channel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

    public enum Interpolation {
        /**
         * 계단식 보간
         */
        STEP,

        /**
         * 선형 보간
         */
        LINEAR,

        /**
         * 스플라인 보간
         */
        SPLINE
    }


    /**
     * @param input         입력 데이터
     * @param interpolation 보간 방식
     * @param output        출력 데이터
     */
    public record Sampler(AccessorModel input, Interpolation interpolation, AccessorModel output) {
        /**
         * 기본 생성자
         *
         * @param input         입력
         * @param interpolation 보간
         * @param output        출력
         */
        public Sampler(
                AccessorModel input,
                Interpolation interpolation,
                AccessorModel output) {
            this.input = Objects.requireNonNull(
                    input, "The input may not be null");
            this.interpolation = Objects.requireNonNull(
                    interpolation, "The interpolation may not be null");
            this.output = Objects.requireNonNull(
                    output, "The output may not be null");
        }
    }

    /**
     * @param sampler   샘플러
     * @param nodeModel 노드 모델
     * @param path      경로
     */
    public record Channel(Sampler sampler, NodeModel nodeModel, String path) {

        /**
         * 기본 생성자
         *
         * @param sampler   샘플러
         * @param nodeModel 노드 모델
         * @param path      경로
         */
        public Channel(
                Sampler sampler,
                NodeModel nodeModel,
                String path) {
            this.sampler = Objects.requireNonNull(
                    sampler, "The sampler may not be null");
            this.nodeModel = nodeModel;
            this.path = Objects.requireNonNull(
                    path, "The path may not be null");

        }
    }
}

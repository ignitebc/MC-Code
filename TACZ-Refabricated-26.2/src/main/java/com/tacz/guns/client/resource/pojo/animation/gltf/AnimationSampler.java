package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AnimationSampler {
    /**
     * 키프레임 타임스탬프를 담은 accessor의 인덱스. (필수)
     */
    private Integer input;

    /**
     * 보간 알고리즘. (선택)<br>
     * 기본값: "LINEAR"<br>
     * 허용 값: [LINEAR, STEP, CUBICSPLINE]
     */
    private String interpolation;

    /**
     * 키프레임 출력 값을 담은 accessor의 인덱스.
     * (필수)
     */
    private Integer output;

    /**
     * 키프레임 타임스탬프를 담은 accessor의 인덱스. (필수)
     *
     * @return input
     */
    public Integer getInput() {
        return this.input;
    }

    /**
     * 키프레임 타임스탬프를 담은 accessor의 인덱스. (필수)
     *
     * @param input 설정할 input
     * @throws NullPointerException 주어진 값이 <code>null</code>일 때
     */
    public void setInput(Integer input) {
        if (input == null) {
            throw new NullPointerException((("Invalid value for input: " + input) + ", may not be null"));
        }
        this.input = input;
    }

    /**
     * 보간 알고리즘. (선택)<br>
     * 기본값: "LINEAR"<br>
     * 허용 값: [LINEAR, STEP, CUBICSPLINE]
     *
     * @return interpolation
     */
    public String getInterpolation() {
        return this.interpolation;
    }

    /**
     * 보간 알고리즘. (선택)<br>
     * 기본값: "LINEAR"<br>
     * 허용 값: [LINEAR, STEP, CUBICSPLINE]
     *
     * @param interpolation 설정할 interpolation
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setInterpolation(String interpolation) {
        if (interpolation == null) {
            this.interpolation = interpolation;
            return;
        }
        if (((!"LINEAR".equals(interpolation)) && (!"STEP".equals(interpolation))) && (!"CUBICSPLINE".equals(interpolation))) {
            throw new IllegalArgumentException((("Invalid value for interpolation: " + interpolation) + ", valid: [LINEAR, STEP, CUBICSPLINE]"));
        }
        this.interpolation = interpolation;
    }

    /**
     * interpolation의 기본값을 돌려준다<br>
     *
     * @return interpolation 기본값
     * @see #getInterpolation
     */
    public String defaultInterpolation() {
        return "LINEAR";
    }

    /**
     * 키프레임 출력 값을 담은 accessor의 인덱스.
     * (필수)
     *
     * @return output
     */
    public Integer getOutput() {
        return this.output;
    }

    /**
     * 키프레임 출력 값을 담은 accessor의 인덱스.
     * (필수)
     *
     * @param output 설정할 output
     * @throws NullPointerException 주어진 값이 <code>null</code>일 때
     */
    public void setOutput(Integer output) {
        if (output == null) {
            throw new NullPointerException((("Invalid value for output: " + output) + ", may not be null"));
        }
        this.output = output;
    }
}

package com.tacz.guns.client.resource.pojo.animation.gltf;

public class BufferView {
    /**
     * 버퍼의 인덱스. (필수)
     */
    private Integer buffer;
    /**
     * 버퍼 안 바이트 오프셋. (선택)<br>
     * 기본값: 0<br>
     * 최솟값: 0 (포함)
     */
    private Integer byteOffset;
    /**
     * bufferView의 바이트 길이. (필수)<br>
     * 최솟값: 1 (포함)
     */
    private Integer byteLength;
    /**
     * 바이트 단위 간격(stride). (선택)<br>
     * 최솟값: 4 (포함)<br>
     * 최댓값: 252 (포함)
     */
    private Integer byteStride;
    /**
     * 이 buffer view와 함께 쓸 GPU 버퍼 종류를 알려 주는
     * 힌트. (선택)<br>
     * 허용 값: [34962, 34963]
     */
    private Integer target;

    /**
     * 버퍼의 인덱스. (필수)
     *
     * @return buffer
     */
    public Integer getBuffer() {
        return this.buffer;
    }

    /**
     * 버퍼의 인덱스. (필수)
     *
     * @param buffer 설정할 buffer
     * @throws NullPointerException 주어진 값이 <code>null</code>일 때
     */
    public void setBuffer(Integer buffer) {
        if (buffer == null) {
            throw new NullPointerException((("Invalid value for buffer: " + buffer) + ", may not be null"));
        }
        this.buffer = buffer;
    }

    /**
     * 버퍼 안 바이트 오프셋. (선택)<br>
     * 기본값: 0<br>
     * 최솟값: 0 (포함)
     *
     * @return byteOffset
     */
    public Integer getByteOffset() {
        return this.byteOffset;
    }

    /**
     * 버퍼 안 바이트 오프셋. (선택)<br>
     * 기본값: 0<br>
     * 최솟값: 0 (포함)
     *
     * @param byteOffset 설정할 byteOffset
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setByteOffset(Integer byteOffset) {
        if (byteOffset == null) {
            this.byteOffset = byteOffset;
            return;
        }
        if (byteOffset < 0) {
            throw new IllegalArgumentException("byteOffset < 0");
        }
        this.byteOffset = byteOffset;
    }

    /**
     * byteOffset의 기본값을 돌려준다<br>
     *
     * @return byteOffset 기본값
     * @see #getByteOffset
     */
    public Integer defaultByteOffset() {
        return 0;
    }

    /**
     * bufferView의 바이트 길이. (필수)<br>
     * 최솟값: 1 (포함)
     *
     * @return byteLength
     */
    public Integer getByteLength() {
        return this.byteLength;
    }

    /**
     * bufferView의 바이트 길이. (필수)<br>
     * 최솟값: 1 (포함)
     *
     * @param byteLength 설정할 byteLength
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setByteLength(Integer byteLength) {
        if (byteLength == null) {
            throw new NullPointerException((("Invalid value for byteLength: " + byteLength) + ", may not be null"));
        }
        if (byteLength < 1) {
            throw new IllegalArgumentException("byteLength < 1");
        }
        this.byteLength = byteLength;
    }

    /**
     * 바이트 단위 간격(stride). (선택)<br>
     * 최솟값: 4 (포함)<br>
     * 최댓값: 252 (포함)
     *
     * @return byteStride
     */
    public Integer getByteStride() {
        return this.byteStride;
    }

    /**
     * 바이트 단위 간격(stride). (선택)<br>
     * 최솟값: 4 (포함)<br>
     * 최댓값: 252 (포함)
     *
     * @param byteStride 설정할 byteStride
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setByteStride(Integer byteStride) {
        if (byteStride == null) {
            this.byteStride = byteStride;
            return;
        }
        if (byteStride > 252) {
            throw new IllegalArgumentException("byteStride > 252");
        }
        if (byteStride < 4) {
            throw new IllegalArgumentException("byteStride < 4");
        }
        this.byteStride = byteStride;
    }

    /**
     * 이 buffer view와 함께 쓸 GPU 버퍼 종류를 알려 주는
     * 힌트. (선택)<br>
     * 허용 값: [34962, 34963]
     *
     * @return target
     */
    public Integer getTarget() {
        return this.target;
    }

    /**
     * 이 buffer view와 함께 쓸 GPU 버퍼 종류를 알려 주는
     * 힌트. (선택)<br>
     * 허용 값: [34962, 34963]
     *
     * @param target 설정할 target
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setTarget(Integer target) {
        if (target == null) {
            this.target = target;
            return;
        }
        if ((target != 34962) && (target != 34963)) {
            throw new IllegalArgumentException((("Invalid value for target: " + target) + ", valid: [34962, 34963]"));
        }
        this.target = target;
    }
}

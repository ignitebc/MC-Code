package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AccessorSparseValues {
    /**
     * 희소 값을 담은 bufferView의 인덱스. 참조하는 buffer
     * view에는 `target`이나 `byteStride` 속성을 정의하면
     * **안 된다**. (필수)
     */
    private Integer bufferView;
    /**
     * bufferView 시작 위치 기준 바이트 오프셋.
     * (선택)<br>
     * 기본값: 0<br>
     * 최솟값: 0 (포함)
     */
    private Integer byteOffset;

    /**
     * 희소 값을 담은 bufferView의 인덱스. 참조하는 buffer
     * view에는 `target`이나 `byteStride` 속성을 정의하면
     * **안 된다**. (필수)
     *
     * @return bufferView
     */
    public Integer getBufferView() {
        return this.bufferView;
    }

    /**
     * 희소 값을 담은 bufferView의 인덱스. 참조하는 buffer
     * view에는 `target`이나 `byteStride` 속성을 정의하면
     * **안 된다**. (필수)
     *
     * @param bufferView 설정할 bufferView
     * @throws NullPointerException 주어진 값이 <code>null</code>일 때
     */
    public void setBufferView(Integer bufferView) {
        if (bufferView == null) {
            throw new NullPointerException((("Invalid value for bufferView: " + bufferView) + ", may not be null"));
        }
        this.bufferView = bufferView;
    }

    /**
     * bufferView 시작 위치 기준 바이트 오프셋.
     * (선택)<br>
     * 기본값: 0<br>
     * 최솟값: 0 (포함)
     *
     * @return byteOffset
     */
    public Integer getByteOffset() {
        return this.byteOffset;
    }

    /**
     * bufferView 시작 위치 기준 바이트 오프셋.
     * (선택)<br>
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
}

package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AccessorSparseIndices {
    /**
     * 희소 인덱스를 담은 buffer view의 인덱스. 참조하는
     * buffer view에는 `target`이나 `byteStride` 속성을 정의하면 **안 된다**.
     * buffer view와 선택 항목인 `byteOffset`은 **반드시**
     * `componentType` 바이트 길이에 맞춰 정렬되어야 한다. (필수)
     */
    private Integer bufferView;
    /**
     * buffer view 시작 위치 기준 바이트 오프셋.
     * (선택)<br>
     * 기본값: 0<br>
     * 최솟값: 0 (포함)
     */
    private Integer byteOffset;
    /**
     * 인덱스 데이터 형식. (필수)<br>
     * 허용 값: [5121, 5123, 5125]
     */
    private Integer componentType;

    /**
     * 희소 인덱스를 담은 buffer view의 인덱스. 참조하는
     * buffer view에는 `target`이나 `byteStride` 속성을 정의하면 **안 된다**.
     * buffer view와 선택 항목인 `byteOffset`은 **반드시**
     * `componentType` 바이트 길이에 맞춰 정렬되어야 한다. (필수)
     *
     * @return bufferView
     */
    public Integer getBufferView() {
        return this.bufferView;
    }

    /**
     * 희소 인덱스를 담은 buffer view의 인덱스. 참조하는
     * buffer view에는 `target`이나 `byteStride` 속성을 정의하면 **안 된다**.
     * buffer view와 선택 항목인 `byteOffset`은 **반드시**
     * `componentType` 바이트 길이에 맞춰 정렬되어야 한다. (필수)
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
     * buffer view 시작 위치 기준 바이트 오프셋.
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
     * buffer view 시작 위치 기준 바이트 오프셋.
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

    /**
     * 인덱스 데이터 형식. (필수)<br>
     * 허용 값: [5121, 5123, 5125]
     *
     * @return componentType
     */
    public Integer getComponentType() {
        return this.componentType;
    }

    /**
     * 인덱스 데이터 형식. (필수)<br>
     * 허용 값: [5121, 5123, 5125]
     *
     * @param componentType 설정할 componentType
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setComponentType(Integer componentType) {
        if (componentType == null) {
            throw new NullPointerException((("Invalid value for componentType: " + componentType) + ", may not be null"));
        }
        if (((componentType != 5121) && (componentType != 5123)) && (componentType != 5125)) {
            throw new IllegalArgumentException((("Invalid value for componentType: " + componentType) + ", valid: [5121, 5123, 5125]"));
        }
        this.componentType = componentType;
    }
}

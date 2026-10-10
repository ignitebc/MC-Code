package com.tacz.guns.client.resource.pojo.animation.gltf;

public class Accessor {
    /**
     * bufferView의 인덱스. (선택)
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
     * accessor 구성 요소의 데이터 형식. (필수)<br>
     * 허용 값: [5120, 5121, 5122, 5123, 5125, 5126]
     */
    private Integer componentType;
    /**
     * 정수 데이터 값을 쓰기 전에 정규화할지 여부.
     * (선택)<br>
     * 기본값: false
     */
    private Boolean normalized;
    /**
     * 이 accessor가 참조하는 요소 수. (필수)<br>
     * 최솟값: 1 (포함)
     */
    private Integer count;
    /**
     * accessor 요소가 스칼라, 벡터, 행렬 중 무엇인지 지정한다.
     * (필수)<br>
     * 허용 값: [SCALAR, VEC2, VEC3, VEC4, MAT2, MAT3, MAT4]
     */
    private String type;
    /**
     * 이 accessor의 구성 요소별 최댓값. (선택)<br>
     * 최소 항목 수: 1<br>
     * 최대 항목 수: 16<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     */
    private Number[] max;
    /**
     * 이 accessor의 구성 요소별 최솟값. (선택)<br>
     * 최소 항목 수: 1<br>
     * 최대 항목 수: 16<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     */
    private Number[] min;
    /**
     * 초깃값과 다른 요소를 담는 희소 저장소.
     * (선택)
     */
    private AccessorSparse sparse;

    /**
     * bufferView의 인덱스. (선택)
     *
     * @return bufferView
     */
    public Integer getBufferView() {
        return this.bufferView;
    }

    /**
     * bufferView의 인덱스. (선택)
     *
     * @param bufferView 설정할 bufferView
     */
    public void setBufferView(Integer bufferView) {
        if (bufferView == null) {
            this.bufferView = bufferView;
            return;
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
     * accessor 구성 요소의 데이터 형식. (필수)<br>
     * 허용 값: [5120, 5121, 5122, 5123, 5125, 5126]
     *
     * @return componentType
     */
    public Integer getComponentType() {
        return this.componentType;
    }

    /**
     * accessor 구성 요소의 데이터 형식. (필수)<br>
     * 허용 값: [5120, 5121, 5122, 5123, 5125, 5126]
     *
     * @param componentType 설정할 componentType
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setComponentType(Integer componentType) {
        if (componentType == null) {
            throw new NullPointerException((("Invalid value for componentType: " + componentType) + ", may not be null"));
        }
        if ((((((componentType != 5120) && (componentType != 5121)) && (componentType != 5122)) && (componentType != 5123)) && (componentType != 5125)) && (componentType != 5126)) {
            throw new IllegalArgumentException((("Invalid value for componentType: " + componentType) + ", valid: [5120, 5121, 5122, 5123, 5125, 5126]"));
        }
        this.componentType = componentType;
    }

    /**
     * 정수 데이터 값을 쓰기 전에 정규화할지 여부.
     * (선택)<br>
     * 기본값: false
     *
     * @param normalized 설정할 normalized
     */
    public void setNormalized(Boolean normalized) {
        if (normalized == null) {
            this.normalized = normalized;
            return;
        }
        this.normalized = normalized;
    }

    /**
     * 정수 데이터 값을 쓰기 전에 정규화할지 여부.
     * (선택)<br>
     * 기본값: false
     *
     * @return normalized
     */
    public Boolean isNormalized() {
        return this.normalized;
    }

    /**
     * normalized의 기본값을 돌려준다<br>
     *
     * @return normalized 기본값
     * @see #isNormalized
     */
    public Boolean defaultNormalized() {
        return false;
    }

    /**
     * 이 accessor가 참조하는 요소 수. (필수)<br>
     * 최솟값: 1 (포함)
     *
     * @return count
     */
    public Integer getCount() {
        return this.count;
    }

    /**
     * 이 accessor가 참조하는 요소 수. (필수)<br>
     * 최솟값: 1 (포함)
     *
     * @param count 설정할 count
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setCount(Integer count) {
        if (count == null) {
            throw new NullPointerException((("Invalid value for count: " + count) + ", may not be null"));
        }
        if (count < 1) {
            throw new IllegalArgumentException("count < 1");
        }
        this.count = count;
    }

    /**
     * accessor 요소가 스칼라, 벡터, 행렬 중 무엇인지 지정한다.
     * (필수)<br>
     * 허용 값: [SCALAR, VEC2, VEC3, VEC4, MAT2, MAT3, MAT4]
     *
     * @return type
     */
    public String getType() {
        return this.type;
    }

    /**
     * accessor 요소가 스칼라, 벡터, 행렬 중 무엇인지 지정한다.
     * (필수)<br>
     * 허용 값: [SCALAR, VEC2, VEC3, VEC4, MAT2, MAT3, MAT4]
     *
     * @param type 설정할 type
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setType(String type) {
        if (type == null) {
            throw new NullPointerException((("Invalid value for type: " + type) + ", may not be null"));
        }
        if (((((((!"SCALAR".equals(type)) && (!"VEC2".equals(type))) && (!"VEC3".equals(type))) && (!"VEC4".equals(type))) && (!"MAT2".equals(type))) && (!"MAT3".equals(type))) && (!"MAT4".equals(type))) {
            throw new IllegalArgumentException((("Invalid value for type: " + type) + ", valid: [SCALAR, VEC2, VEC3, VEC4, MAT2, MAT3, MAT4]"));
        }
        this.type = type;
    }

    /**
     * 이 accessor의 구성 요소별 최댓값. (선택)<br>
     * 최소 항목 수: 1<br>
     * 최대 항목 수: 16<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @return max
     */
    public Number[] getMax() {
        return this.max;
    }

    /**
     * 이 accessor의 구성 요소별 최댓값. (선택)<br>
     * 최소 항목 수: 1<br>
     * 최대 항목 수: 16<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @param max 설정할 max
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setMax(Number[] max) {
        if (max == null) {
            this.max = max;
            return;
        }
        if (max.length < 1) {
            throw new IllegalArgumentException("Number of max elements is < 1");
        }
        if (max.length > 16) {
            throw new IllegalArgumentException("Number of max elements is > 16");
        }
        this.max = max;
    }

    /**
     * 이 accessor의 구성 요소별 최솟값. (선택)<br>
     * 최소 항목 수: 1<br>
     * 최대 항목 수: 16<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @return min
     */
    public Number[] getMin() {
        return this.min;
    }

    /**
     * 이 accessor의 구성 요소별 최솟값. (선택)<br>
     * 최소 항목 수: 1<br>
     * 최대 항목 수: 16<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @param min 설정할 min
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setMin(Number[] min) {
        if (min == null) {
            this.min = min;
            return;
        }
        if (min.length < 1) {
            throw new IllegalArgumentException("Number of min elements is < 1");
        }
        if (min.length > 16) {
            throw new IllegalArgumentException("Number of min elements is > 16");
        }
        this.min = min;
    }

    /**
     * 초깃값과 다른 요소를 담는 희소 저장소.
     * (선택)
     *
     * @return sparse
     */
    public AccessorSparse getSparse() {
        return this.sparse;
    }

    /**
     * 초깃값과 다른 요소를 담는 희소 저장소.
     * (선택)
     *
     * @param sparse 설정할 sparse
     */
    public void setSparse(AccessorSparse sparse) {
        if (sparse == null) {
            this.sparse = sparse;
            return;
        }
        this.sparse = sparse;
    }
}

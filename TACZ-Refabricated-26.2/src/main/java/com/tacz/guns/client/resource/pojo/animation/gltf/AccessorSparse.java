package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AccessorSparse {
    /**
     * 희소 배열에 저장된, 초깃값과 다른 accessor 값의 수.
     * (필수)<br>
     * 최솟값: 1 (포함)
     */
    private Integer count;
    /**
     * 초깃값과 다른 accessor 값의 인덱스를 담은 buffer view를 가리키는 객체.
     * 인덱스 수는 `count`와 같다.
     * 인덱스는 **반드시** 엄격하게 증가해야 한다. (필수)
     */
    private AccessorSparseIndices indices;
    /**
     * 초깃값과 다른 accessor 값을 담은 buffer view를 가리키는 객체.
     * (필수)
     */
    private AccessorSparseValues values;

    /**
     * 희소 배열에 저장된, 초깃값과 다른 accessor 값의 수.
     * (필수)<br>
     * 최솟값: 1 (포함)
     *
     * @return count
     */
    public Integer getCount() {
        return this.count;
    }

    /**
     * 희소 배열에 저장된, 초깃값과 다른 accessor 값의 수.
     * (필수)<br>
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
     * 초깃값과 다른 accessor 값의 인덱스를 담은 buffer view를 가리키는 객체.
     * 인덱스 수는 `count`와 같다.
     * 인덱스는 **반드시** 엄격하게 증가해야 한다. (필수)
     *
     * @return indices
     */
    public AccessorSparseIndices getIndices() {
        return this.indices;
    }

    /**
     * 초깃값과 다른 accessor 값의 인덱스를 담은 buffer view를 가리키는 객체.
     * 인덱스 수는 `count`와 같다.
     * 인덱스는 **반드시** 엄격하게 증가해야 한다. (필수)
     *
     * @param indices 설정할 indices
     * @throws NullPointerException 주어진 값이 <code>null</code>일 때
     */
    public void setIndices(AccessorSparseIndices indices) {
        if (indices == null) {
            throw new NullPointerException((("Invalid value for indices: " + indices) + ", may not be null"));
        }
        this.indices = indices;
    }

    /**
     * 초깃값과 다른 accessor 값을 담은 buffer view를 가리키는 객체.
     * (필수)
     *
     * @return values
     */
    public AccessorSparseValues getValues() {
        return this.values;
    }

    /**
     * 초깃값과 다른 accessor 값을 담은 buffer view를 가리키는 객체.
     * (필수)
     *
     * @param values 설정할 values
     * @throws NullPointerException 주어진 값이 <code>null</code>일 때
     */
    public void setValues(AccessorSparseValues values) {
        if (values == null) {
            throw new NullPointerException((("Invalid value for values: " + values) + ", may not be null"));
        }
        this.values = values;
    }
}

package com.tacz.guns.api.client.animation.gltf;

import com.tacz.guns.api.client.animation.gltf.accessor.AccessorData;
import com.tacz.guns.api.client.animation.gltf.accessor.AccessorDatas;
import com.tacz.guns.api.client.animation.gltf.accessor.Accessors;

public class AccessorModel {
    /**
     * GL 상수로 나타낸 성분 종류
     */
    private final int componentType;
    /**
     * 이 접근자의 {@link ElementType}
     */
    private final ElementType elementType;
    /**
     * 요소 수
     */
    private final int count;
    /**
     * 버퍼 뷰 기준 바이트 오프셋
     */
    private int byteOffset;
    /**
     * 이 모델의 {@link BufferViewModel}
     */
    private BufferViewModel bufferViewModel;
    /**
     * 한 요소의 시작과 다음 요소 시작 사이의 간격
     */
    private int byteStride;

    /**
     * {@link AccessorData}
     */
    private AccessorData accessorData;

    /**
     * 최소 성분 값
     */
    private Number[] max;

    /**
     * 최대 성분 값
     */
    private Number[] min;

    /**
     * 새 인스턴스를 만든다
     *
     * @param componentType 성분 종류 GL 상수
     * @param count         요소 수
     * @param elementType   요소 종류
     */
    public AccessorModel(
            int componentType,
            int count,
            ElementType elementType) {
        this.componentType = componentType;
        this.count = count;
        this.elementType = elementType;
    }

    public BufferViewModel getBufferViewModel() {
        return bufferViewModel;
    }

    /**
     * 이 모델의 {@link BufferViewModel}을 설정한다
     *
     * @param bufferViewModel {@link BufferViewModel}
     */
    public void setBufferViewModel(BufferViewModel bufferViewModel) {
        this.bufferViewModel = bufferViewModel;
    }

    public int getComponentType() {
        return componentType;
    }

    public Class<?> getComponentDataType() {
        return Accessors.getDataTypeForAccessorComponentType(
                getComponentType());
    }

    public int getComponentSizeInBytes() {
        return Accessors.getNumBytesForAccessorComponentType(componentType);
    }

    public int getElementSizeInBytes() {
        return elementType.getNumComponents() * getComponentSizeInBytes();
    }

    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * {@link BufferViewModel} 기준 바이트 오프셋을 설정한다
     *
     * @param byteOffset 바이트 오프셋
     */
    public void setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
    }

    public int getCount() {
        return count;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public int getByteStride() {
        return byteStride;
    }

    /**
     * 한 요소의 시작과 다음 요소 시작 사이의 바이트 수인
     * 바이트 간격을 설정한다
     *
     * @param byteStride 바이트 간격
     */
    public void setByteStride(int byteStride) {
        this.byteStride = byteStride;
    }

    public AccessorData getAccessorData() {
        if (accessorData == null) {
            accessorData = AccessorDatas.create(this);
        }
        return accessorData;
    }

    public Number[] getMin() {
        if (min == null) {
            min = AccessorDatas.computeMin(getAccessorData());
        }
        return min.clone();
    }

    public Number[] getMax() {
        if (max == null) {
            max = AccessorDatas.computeMax(getAccessorData());
        }
        return max.clone();
    }

}

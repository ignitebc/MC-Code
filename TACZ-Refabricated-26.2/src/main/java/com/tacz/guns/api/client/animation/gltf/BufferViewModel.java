package com.tacz.guns.api.client.animation.gltf;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class BufferViewModel {
    /**
     * 선택적 대상
     */
    private final Integer target;
    /**
     * 이 모델의 {@link BufferModel}
     */
    private BufferModel bufferModel;
    /**
     * 바이트 오프셋
     */
    private int byteOffset;
    /**
     * 바이트 길이
     */
    private int byteLength;
    /**
     * 바이트 간격
     */
    private Integer byteStride;
    /**
     * {@link #getBufferViewData() 버퍼 뷰 데이터}를 처음 얻을 때
     * 희소 접근자 데이터 치환을 수행하는
     * 선택적 콜백.
     */
    private Consumer<? super ByteBuffer> sparseSubstitutionCallback;

    /**
     * 희소 치환을 이미 적용했는지
     */
    private boolean sparseSubstitutionApplied;

    /**
     * 새 인스턴스를 만든다
     *
     * @param target 선택적 대상
     */
    public BufferViewModel(Integer target) {
        this.byteOffset = 0;
        this.byteLength = 0;
        this.target = target;
    }

    /**
     * {@link #getBufferViewData() 버퍼 뷰 데이터}를 처음 얻을 때
     * 희소 접근자 데이터 치환을 수행할 콜백을 설정한다.
     *
     * @param sparseSubstitutionCallback 콜백
     */
    public void setSparseSubstitutionCallback(
            Consumer<? super ByteBuffer> sparseSubstitutionCallback) {
        this.sparseSubstitutionCallback = sparseSubstitutionCallback;
    }

    public ByteBuffer getBufferViewData() {
        ByteBuffer bufferData = bufferModel.getBufferData();
        ByteBuffer bufferViewData =
                Buffers.createSlice(bufferData, getByteOffset(), getByteLength());
        if (sparseSubstitutionCallback != null && !sparseSubstitutionApplied) {
            sparseSubstitutionCallback.accept(bufferViewData);
            sparseSubstitutionApplied = true;
        }
        return bufferViewData;
    }

    public BufferModel getBufferModel() {
        return bufferModel;
    }

    /**
     * 이 모델의 {@link BufferModel}을 설정한다
     *
     * @param bufferModel {@link BufferModel}
     */
    public void setBufferModel(BufferModel bufferModel) {
        this.bufferModel = bufferModel;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * 이 뷰가 참조하는 {@link BufferModel} 기준 바이트 오프셋을 설정한다
     *
     * @param byteOffset 바이트 오프셋
     */
    public void setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
    }

    public int getByteLength() {
        return byteLength;
    }

    /**
     * 이 버퍼 뷰의 바이트 길이를 설정한다
     *
     * @param byteLength 바이트 길이
     */
    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public Integer getByteStride() {
        return byteStride;
    }

    /**
     * 선택적 바이트 간격을 설정한다. 이 버퍼 뷰를 참조하는 접근자가
     * 둘 이상이면 이 값은 <code>null</code>이면
     * 안 된다.
     *
     * @param byteStride 바이트 간격
     */
    public void setByteStride(Integer byteStride) {
        this.byteStride = byteStride;
    }

    public Integer getTarget() {
        return target;
    }
}

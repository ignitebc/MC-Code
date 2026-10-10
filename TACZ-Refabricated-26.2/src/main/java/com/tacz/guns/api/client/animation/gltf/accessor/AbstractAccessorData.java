/*
 *
 * Copyright 2015-2016 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.tacz.guns.api.client.animation.gltf.accessor;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * {@link AccessorData}의 패키지 내부용 추상 기반 구현
 */
abstract class AbstractAccessorData implements AccessorData {
    /**
     * 성분 종류
     */
    private final Class<?> componentType;

    /**
     * 접근자가 참조하는 버퍼 뷰의
     * 바이트 버퍼
     */
    private final ByteBuffer bufferViewByteBuffer;

    /**
     * 버퍼 뷰의 바이트 버퍼 안에서
     * 접근자가 시작하는 오프셋
     */
    private final int byteOffset;

    /**
     * 요소 수
     */
    private final int numElements;

    /**
     * 요소당 성분 수
     */
    private final int numComponentsPerElement;

    /**
     * 성분당 바이트 수
     */
    private final int numBytesPerComponent;

    /**
     * 연속한 두 요소 사이의 간격(바이트 수)
     */
    private final int byteStridePerElement;

    /**
     * 기본 생성자
     *
     * @param componentType           성분 종류
     * @param bufferViewByteBuffer    버퍼 뷰의 바이트 버퍼
     * @param byteOffset              버퍼 뷰 안의 바이트 오프셋
     * @param numElements             요소 수
     * @param numComponentsPerElement 요소당 성분 수
     * @param numBytesPerComponent    성분당 바이트 수
     * @param byteStride              두 요소 사이의 바이트 간격. 이 값이
     *                                <code>null</code>이나 <code>0</code>이면 간격은
     *                                요소 하나의 크기가 된다.
     * @throws NullPointerException bufferViewByteBuffer가
     *                              <code>null</code>일 때
     */
    AbstractAccessorData(Class<?> componentType,
                         ByteBuffer bufferViewByteBuffer, int byteOffset,
                         int numElements, int numComponentsPerElement,
                         int numBytesPerComponent, Integer byteStride) {
        Objects.requireNonNull(bufferViewByteBuffer,
                "The bufferViewByteBuffer is null");

        this.componentType = componentType;
        this.bufferViewByteBuffer = bufferViewByteBuffer;
        this.byteOffset = byteOffset;
        this.numElements = numElements;
        this.numComponentsPerElement = numComponentsPerElement;
        this.numBytesPerComponent = numBytesPerComponent;
        if (byteStride == null || byteStride == 0) {
            this.byteStridePerElement =
                    numComponentsPerElement * numBytesPerComponent;
        } else {
            this.byteStridePerElement = byteStride;
        }
    }

    @Override
    public final Class<?> getComponentType() {
        return componentType;
    }

    @Override
    public final int getNumElements() {
        return numElements;
    }

    @Override
    public final int getNumComponentsPerElement() {
        return numComponentsPerElement;
    }

    @Override
    public final int getTotalNumComponents() {
        return numElements * numComponentsPerElement;
    }

    /**
     * 지정한 성분이 시작하는 바이트 버퍼 안의
     * 바이트 인덱스를 돌려준다
     *
     * @param elementIndex   요소 인덱스
     * @param componentIndex 성분 인덱스
     * @return 바이트 인덱스
     */
    protected final int getByteIndex(int elementIndex, int componentIndex) {
        return byteOffset + elementIndex * byteStridePerElement + componentIndex * numBytesPerComponent;
    }


    /**
     * 바탕 바이트 버퍼를 돌려준다
     *
     * @return 바이트 버퍼
     */
    protected final ByteBuffer getBufferViewByteBuffer() {
        return bufferViewByteBuffer;
    }

    /**
     * 요소당 바이트 간격을 돌려준다
     *
     * @return 바이트 간격
     */
    protected final int getByteStridePerElement() {
        return byteStridePerElement;
    }

    /**
     * 성분당 바이트 수를 돌려준다
     *
     * @return 성분당 바이트 수
     */
    protected final int getNumBytesPerComponent() {
        return numBytesPerComponent;
    }

}
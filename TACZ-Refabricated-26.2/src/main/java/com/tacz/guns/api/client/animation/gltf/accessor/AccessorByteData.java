/*
 * www.javagl.de - JglTF
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
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

/**
 * 접근자가 설명하는 데이터에 접근하는 클래스.
 * 접근자 매개변수에 따라 접근자의 버퍼 뷰 바이트 버퍼에
 * 접근할 수 있게 한다.<br>
 * <br>
 * 이 데이터는 여러 요소(예: 3차원 byte 벡터)로 이루어지고,
 * 각 요소는 여러 성분(예: byte 값 3개)으로 이루어진다.
 */
public final class AccessorByteData
        extends AbstractAccessorData implements AccessorData {
    /**
     * 데이터를 부호 없는 값으로 해석할지
     */
    private final boolean unsigned;

    /**
     * 주어진 접근자 매개변수가 정하는 규칙에 따라
     * 주어진 바이트 버퍼의 데이터에 접근하는
     * 새 인스턴스를 만든다.
     *
     * @param componentType           성분 종류
     * @param bufferViewByteBuffer    버퍼 뷰의 바이트 버퍼
     * @param byteOffset              버퍼 뷰 안의 바이트 오프셋
     * @param numElements             요소 수
     * @param numComponentsPerElement 요소당 성분 수
     * @param byteStride              두 요소 사이의 바이트 간격. 이 값이
     *                                <code>null</code>이나 <code>0</code>이면 간격은
     *                                요소 하나의 크기가 된다.
     * @throws NullPointerException     bufferViewByteBuffer가
     *                                  <code>null</code>일 때
     * @throws IllegalArgumentException 성분 종류가
     *                                  <code>GL_BYTE</code>나 <code>GL_UNSIGEND_BYTE</code>가 아닐 때
     * @throws IllegalArgumentException 주어진 바이트 버퍼의 용량이
     *                                  접근자 데이터를 담기에 모자랄 때
     */
    public AccessorByteData(int componentType,
                            ByteBuffer bufferViewByteBuffer, int byteOffset, int numElements,
                            int numComponentsPerElement, Integer byteStride) {
        super(byte.class, bufferViewByteBuffer, byteOffset, numElements,
                numComponentsPerElement, Byte.BYTES, byteStride);
        AccessorDatas.validateByteType(componentType);
        this.unsigned = AccessorDatas.isUnsignedType(componentType);
        AccessorDatas.validateCapacity(byteOffset, getNumElements(),
                getByteStridePerElement(), bufferViewByteBuffer.capacity());
    }

    /**
     * 데이터를 부호 없는 값으로 해석할지 돌려준다
     *
     * @return 데이터를 부호 없는 값으로 해석할지
     */
    public boolean isUnsigned() {
        return unsigned;
    }

    /**
     * 지정한 요소의 지정한 성분 값을 돌려준다
     *
     * @param elementIndex   요소 인덱스
     * @param componentIndex 성분 인덱스
     * @return 값
     * @throws IndexOutOfBoundsException 주어진 인덱스 때문에
     *                                   바탕 버퍼의 범위 밖에 접근하게 될 때
     */
    public byte get(int elementIndex, int componentIndex) {
        int byteIndex = getByteIndex(elementIndex, componentIndex);
        return getBufferViewByteBuffer().get(byteIndex);
    }

    /**
     * 지정한 성분의 값을 돌려준다
     *
     * @param globalComponentIndex 전체 성분 인덱스
     * @return 값
     * @throws IndexOutOfBoundsException 주어진 인덱스 때문에
     *                                   바탕 버퍼의 범위 밖에 접근하게 될 때
     */
    public byte get(int globalComponentIndex) {
        int elementIndex =
                globalComponentIndex / getNumComponentsPerElement();
        int componentIndex =
                globalComponentIndex % getNumComponentsPerElement();
        return get(elementIndex, componentIndex);
    }

    /**
     * 지정한 요소의 지정한 성분 값을 설정한다
     *
     * @param elementIndex   요소 인덱스
     * @param componentIndex 성분 인덱스
     * @param value          값
     * @throws IndexOutOfBoundsException 주어진 인덱스 때문에
     *                                   바탕 버퍼의 범위 밖에 접근하게 될 때
     */
    public void set(int elementIndex, int componentIndex, byte value) {
        int byteIndex = getByteIndex(elementIndex, componentIndex);
        getBufferViewByteBuffer().put(byteIndex, value);
    }

    /**
     * 지정한 성분의 값을 설정한다
     *
     * @param globalComponentIndex 전체 성분 인덱스
     * @param value                값
     * @throws IndexOutOfBoundsException 주어진 인덱스 때문에
     *                                   바탕 버퍼의 범위 밖에 접근하게 될 때
     */
    public void set(int globalComponentIndex, byte value) {
        int elementIndex =
                globalComponentIndex / getNumComponentsPerElement();
        int componentIndex =
                globalComponentIndex % getNumComponentsPerElement();
        set(elementIndex, componentIndex, value);
    }


    /**
     * 지정한 요소의 지정한 성분 값을 돌려준다.
     * 데이터가 {@link #isUnsigned()}인지 고려한다: 부호 없는 데이터면
     * 돌려줄 byte 값을 부호 없는 정수 값으로
     * 바꾼다.
     *
     * @param elementIndex   요소 인덱스
     * @param componentIndex 성분 인덱스
     * @return 값
     * @throws IndexOutOfBoundsException 주어진 인덱스 때문에
     *                                   바탕 버퍼의 범위 밖에 접근하게 될 때
     */
    public int getInt(int elementIndex, int componentIndex) {
        byte value = get(elementIndex, componentIndex);
        return unsigned ? Byte.toUnsignedInt(value) : value;
    }

    /**
     * 지정한 성분의 값을 돌려준다.
     * 데이터가 {@link #isUnsigned()}인지 고려한다: 부호 없는 데이터면
     * 돌려줄 byte 값을 부호 없는 정수
     * 값으로 바꾼다.
     *
     * @param globalComponentIndex 전체 성분 인덱스
     * @return 값
     * @throws IndexOutOfBoundsException 주어진 인덱스 때문에
     *                                   바탕 버퍼의 범위 밖에 접근하게 될 때
     */
    public int getInt(int globalComponentIndex) {
        byte value = get(globalComponentIndex);
        return unsigned ? Byte.toUnsignedInt(value) : value;
    }

    /**
     * 이 접근자 데이터의 모든 요소에서 성분별 최솟값을 담은 배열을 돌려준다.
     * 배열 길이는
     * {@link #getNumComponentsPerElement() 요소당 성분 수}다.
     *
     * @return 최솟값
     */
    public byte[] computeMin() {
        byte[] result = new byte[getNumComponentsPerElement()];
        Arrays.fill(result, Byte.MAX_VALUE);
        for (int e = 0; e < getNumElements(); e++) {
            for (int c = 0; c < getNumComponentsPerElement(); c++) {
                result[c] = (byte) Math.min(result[c], get(e, c));
            }
        }
        return result;
    }

    /**
     * 이 접근자 데이터의 모든 요소에서 성분별 최댓값을 담은 배열을 돌려준다.
     * 배열 길이는
     * {@link #getNumComponentsPerElement() 요소당 성분 수}다.
     *
     * @return 최댓값
     */
    public byte[] computeMax() {
        byte[] result = new byte[getNumComponentsPerElement()];
        Arrays.fill(result, Byte.MIN_VALUE);
        for (int e = 0; e < getNumElements(); e++) {
            for (int c = 0; c < getNumComponentsPerElement(); c++) {
                result[c] = (byte) Math.max(result[c], get(e, c));
            }
        }
        return result;
    }

    /**
     * 이 접근자 데이터의 모든 요소에서 성분별 최솟값을 담은 배열을 돌려준다.
     * 배열 길이는
     * {@link #getNumComponentsPerElement() 요소당 성분 수}다.
     * 값은 {@link #getInt(int, int)}를 바탕으로 계산한다.
     *
     * @return 최솟값
     */
    public int[] computeMinInt() {
        int[] result = new int[getNumComponentsPerElement()];
        Arrays.fill(result, Integer.MAX_VALUE);
        for (int e = 0; e < getNumElements(); e++) {
            for (int c = 0; c < getNumComponentsPerElement(); c++) {
                result[c] = Math.min(result[c], getInt(e, c));
            }
        }
        return result;
    }

    /**
     * 이 접근자 데이터의 모든 요소에서 성분별 최댓값을 담은 배열을 돌려준다.
     * 배열 길이는
     * {@link #getNumComponentsPerElement() 요소당 성분 수}다.
     * 값은 {@link #getInt(int, int)}를 바탕으로 계산한다.
     *
     * @return 최댓값
     */
    public int[] computeMaxInt() {
        int result[] = new int[getNumComponentsPerElement()];
        Arrays.fill(result, Integer.MIN_VALUE);
        for (int e = 0; e < getNumElements(); e++) {
            for (int c = 0; c < getNumComponentsPerElement(); c++) {
                result[c] = Math.max(result[c], getInt(e, c));
            }
        }
        return result;
    }

    @Override
    public ByteBuffer createByteBuffer() {
        int totalNumComponents = getTotalNumComponents();
        int totalBytes = totalNumComponents * getNumBytesPerComponent();
        ByteBuffer result = ByteBuffer.allocateDirect(totalBytes)
                .order(ByteOrder.nativeOrder());
        for (int i = 0; i < totalNumComponents; i++) {
            byte component = get(i);
            result.put(component);
        }
        result.position(0);
        return result;
    }

    /**
     * 데이터의 문자열 표현을 만든다(아주 길 수 있다!)
     *
     * @param locale         숫자 형식에 쓸 로케일
     * @param format         숫자 형식 문자열
     * @param elementsPerRow 한 줄에 넣을 요소 수. 이 값이
     *                       0보다 크지 않으면 모든 요소를 한 줄에 넣는다.
     * @return 데이터 문자열
     */
    public String createString(
            Locale locale, String format, int elementsPerRow) {
        StringBuilder sb = new StringBuilder();
        int nc = getNumComponentsPerElement();
        sb.append("[");
        for (int e = 0; e < getNumElements(); e++) {
            if (e > 0) {
                sb.append(", ");
                if (elementsPerRow > 0 && (e % elementsPerRow) == 0) {
                    sb.append("\n ");
                }
            }
            if (nc > 1) {
                sb.append("(");
            }
            for (int c = 0; c < nc; c++) {
                if (c > 0) {
                    sb.append(", ");
                }
                int component = getInt(e, c);
                sb.append(String.format(locale, format, component));
            }
            if (nc > 1) {
                sb.append(")");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
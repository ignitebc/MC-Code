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

import com.tacz.guns.api.client.animation.gltf.AccessorModel;
import com.tacz.guns.api.client.animation.gltf.BufferViewModel;
import com.tacz.guns.api.client.animation.gltf.GltfConstants;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * 접근자가 참조하는 버퍼 뷰의 데이터에 <i>타입별로</i> 접근하게 해 주는
 * 접근자 데이터 도우미 클래스의 인스턴스를 만드는 메서드 모음.<br>
 * <br>
 * 따로 적지 않았다면 이 메서드들의 인자는 모두
 * <code>null</code>이면 안 된다.
 */
public class AccessorDatas {
    /**
     * 인스턴스를 만들지 못하게 막는 비공개 생성자
     */
    private AccessorDatas() {
        // 인스턴스를 만들지 못하게 막는 비공개 생성자
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorData}를 만든다
     *
     * @param accessorModel {@link AccessorModel}
     * @return {@link AccessorData}
     */
    public static AccessorData create(AccessorModel accessorModel) {
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
        ByteBuffer bufferViewData = bufferViewModel.getBufferViewData();
        return create(accessorModel, bufferViewData);
    }

    /**
     * 주어진 버퍼의 데이터를 참조하는 {@link AccessorModel}의
     * {@link AccessorData}를 만든다.
     *
     * @param accessorModel {@link AccessorModel}
     * @param byteBuffer    데이터가 든 바이트 버퍼
     * @return {@link AccessorData}
     */
    public static AccessorData create(
            AccessorModel accessorModel, ByteBuffer byteBuffer) {
        if (accessorModel.getComponentDataType() == byte.class) {
            return createByte(accessorModel, byteBuffer);
        }
        if (accessorModel.getComponentDataType() == short.class) {
            return createShort(accessorModel, byteBuffer);
        }
        if (accessorModel.getComponentDataType() == int.class) {
            return createInt(accessorModel, byteBuffer);
        }
        if (accessorModel.getComponentDataType() == float.class) {
            return createFloat(accessorModel, byteBuffer);
        }
        return null;
    }

    /**
     * 주어진 성분 종류에 맞는 {@link AccessorData}를 만든다.
     * {@link AccessorByteData}, {@link AccessorShortData},
     * {@link AccessorIntData}, {@link AccessorFloatData} 중 하나를 돌려준다
     *
     * @param componentType           GL 상수로 나타낸 성분 종류(예:
     *                                <code>GL_UNSIGNED_SHORT</code>나 <code>GL_FLOAT</code>)
     * @param bufferViewData          접근자가 참조하는 버퍼 뷰 데이터
     * @param byteOffset              접근자의 바이트 오프셋
     * @param count                   접근자의 개수(요소 수)
     * @param numComponentsPerElement 요소당 성분 수.
     *                                예를 들어 접근자 종류가 <code>"VEC3"</code>이면
     *                                3이다
     * @param byteStride              접근자 데이터의 선택적 바이트 간격
     * @return {@link AccessorData}
     * @throws IllegalArgumentException 주어진 성분 종류가
     *                                  올바른 GL 상수가 아닐 때
     */
    public static AccessorData create(
            int componentType, ByteBuffer bufferViewData, int byteOffset,
            int count, int numComponentsPerElement, Integer byteStride) {
        if (isByteType(componentType)) {
            return new AccessorByteData(
                    componentType, bufferViewData, byteOffset, count,
                    numComponentsPerElement, byteStride);
        }
        if (isShortType(componentType)) {
            return new AccessorShortData(
                    componentType, bufferViewData, byteOffset, count,
                    numComponentsPerElement, byteStride);
        }
        if (isIntType(componentType)) {
            return new AccessorIntData(
                    componentType, bufferViewData, byteOffset, count,
                    numComponentsPerElement, byteStride);
        }
        if (isFloatType(componentType)) {
            return new AccessorFloatData(
                    componentType, bufferViewData, byteOffset, count,
                    numComponentsPerElement, byteStride);
        }
        throw new IllegalArgumentException(
                "Not a valid component type: " + componentType);
    }

    /**
     * 주어진 상수가 <code>GL_BYTE</code>나
     * <code>GL_UNSIGNED_BYTE</code>인지 돌려준다.
     *
     * @param type 종류 상수
     * @return <code>byte</code> 종류인지
     */
    public static boolean isByteType(int type) {
        return
                type == GltfConstants.GL_BYTE ||
                        type == GltfConstants.GL_UNSIGNED_BYTE;
    }

    /**
     * 주어진 상수가 <code>GL_SHORT</code>나
     * <code>GL_UNSIGNED_SHORT</code>인지 돌려준다.
     *
     * @param type 종류 상수
     * @return <code>short</code> 종류인지
     */
    public static boolean isShortType(int type) {
        return
                type == GltfConstants.GL_SHORT ||
                        type == GltfConstants.GL_UNSIGNED_SHORT;
    }

    /**
     * 주어진 상수가 <code>GL_INT</code>나
     * <code>GL_UNSIGNED_INT</code>인지 돌려준다.
     *
     * @param type 종류 상수
     * @return <code>int</code> 종류인지
     */
    public static boolean isIntType(int type) {
        return
                type == GltfConstants.GL_INT ||
                        type == GltfConstants.GL_UNSIGNED_INT;
    }

    /**
     * 주어진 상수가 <code>GL_FLOAT</code>인지 돌려준다.
     *
     * @param type 종류 상수
     * @return <code>float</code> 종류인지
     */
    public static boolean isFloatType(int type) {
        return type == GltfConstants.GL_FLOAT;
    }

    /**
     * 주어진 상수가 <code>GL_UNSIGNED_BYTE</code>,
     * <code>GL_UNSIGNED_SHORT</code>, <code>GL_UNSIGNED_INT</code> 중 하나인지 돌려준다.
     *
     * @param type 종류 상수
     * @return 부호 없는 종류인지
     */
    static boolean isUnsignedType(int type) {
        return
                type == GltfConstants.GL_UNSIGNED_BYTE ||
                        type == GltfConstants.GL_UNSIGNED_SHORT ||
                        type == GltfConstants.GL_UNSIGNED_INT;
    }

    /**
     * 주어진 종류가 <code>GL_BYTE</code>나
     * <code>GL_UNSIGNED_BYTE</code>인지 확인하고, 아니면
     * <code>IllegalArgumentException</code>을 던진다.
     *
     * @param type 종류 상수
     * @throws IllegalArgumentException 주어진 종류가
     *                                  <code>GL_BYTE</code>나 <code>GL_UNSIGNED_BYTE</code>가 아닐 때
     */
    static void validateByteType(int type) {
        if (!isByteType(type)) {
            throw new IllegalArgumentException(
                    "The type is not GL_BYTE or GL_UNSIGNED_BYTE, but " +
                            GltfConstants.stringFor(type));
        }
    }

    /**
     * 주어진 종류가 <code>GL_SHORT</code>나
     * <code>GL_UNSIGNED_SHORT</code>인지 확인하고, 아니면
     * <code>IllegalArgumentException</code>을 던진다.
     *
     * @param type 종류 상수
     * @throws IllegalArgumentException 주어진 종류가
     *                                  <code>GL_SHORT</code>나 <code>GL_UNSIGNED_BYTE</code>가 아닐 때
     */
    static void validateShortType(int type) {
        if (!isShortType(type)) {
            throw new IllegalArgumentException(
                    "The type is not GL_SHORT or GL_UNSIGNED_SHORT, but " +
                            GltfConstants.stringFor(type));
        }
    }

    /**
     * 주어진 종류가 <code>GL_INT</code>나
     * <code>GL_UNSIGNED_INT</code>인지 확인하고, 아니면
     * <code>IllegalArgumentException</code>을 던진다.
     *
     * @param type 종류 상수
     * @throws IllegalArgumentException 주어진 종류가
     *                                  <code>GL_INT</code>나 <code>GL_UNSIGNED_INT</code>가 아닐 때
     */
    static void validateIntType(int type) {
        if (!isIntType(type)) {
            throw new IllegalArgumentException(
                    "The type is not GL_INT or GL_UNSIGNED_INT, but " +
                            GltfConstants.stringFor(type));
        }
    }

    /**
     * 주어진 종류가 <code>GL_FLOAT</code>인지 확인하고, 아니면
     * <code>IllegalArgumentException</code>을 던진다.
     *
     * @param type 종류 상수
     * @throws IllegalArgumentException 주어진 종류가
     *                                  <code>GL_FLOAT</code>가 아닐 때
     */
    static void validateFloatType(int type) {
        if (!isFloatType(type)) {
            throw new IllegalArgumentException(
                    "The type is not GL_FLOAT, but " +
                            GltfConstants.stringFor(type));
        }
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorByteData}를 만든다
     *
     * @param accessorModel {@link AccessorModel}
     * @return {@link AccessorByteData}
     * @throws IllegalArgumentException 주어진 접근자의
     *                                  {@link AccessorModel#getComponentType() 성분 종류}가
     *                                  <code>GL_BYTE</code>나 <code>GL_UNSIGNED_BYTE</code>가 아닐 때
     */
    static AccessorByteData createByte(AccessorModel accessorModel) {
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
        return createByte(accessorModel, bufferViewModel.getBufferViewData());
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorByteData}를 만든다
     *
     * @param accessorModel        {@link AccessorModel}
     * @param bufferViewByteBuffer {@link AccessorModel}이 참조하는
     *                             {@link BufferViewModel}의 바이트 버퍼
     * @return {@link AccessorByteData}
     * @throws NullPointerException     인자 중 하나라도 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 accessorModel의
     *                                  {@link AccessorModel#getComponentType() 성분 종류}가
     *                                  <code>GL_BYTE</code>나
     *                                  <code>GL_UNSIGNED_BYTE</code>가 아닐 때
     */
    private static AccessorByteData createByte(
            AccessorModel accessorModel, ByteBuffer bufferViewByteBuffer) {
        return new AccessorByteData(accessorModel.getComponentType(),
                bufferViewByteBuffer,
                accessorModel.getByteOffset(),
                accessorModel.getCount(),
                accessorModel.getElementType().getNumComponents(),
                accessorModel.getByteStride());
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorShortData}를 만든다
     *
     * @param accessorModel {@link AccessorModel}
     * @return {@link AccessorShortData}
     * @throws IllegalArgumentException 주어진 accessorModel의
     *                                  {@link AccessorModel#getComponentType() 성분 종류}가
     *                                  <code>GL_SHORT</code>나
     *                                  <code>GL_UNSIGNED_SHORT</code>가 아닐 때
     */
    static AccessorShortData createShort(AccessorModel accessorModel) {
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
        return createShort(accessorModel, bufferViewModel.getBufferViewData());
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorShortData}를 만든다
     *
     * @param accessorModel        {@link AccessorModel}
     * @param bufferViewByteBuffer {@link AccessorModel}이 참조하는
     *                             {@link BufferViewModel}의 바이트 버퍼
     * @return {@link AccessorShortData}
     * @throws NullPointerException     인자 중 하나라도 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 accessorModel의
     *                                  {@link AccessorModel#getComponentType() 성분 종류}가
     *                                  <code>GL_SHORT</code>나
     *                                  <code>GL_UNSIGNED_SHORT</code>가 아닐 때
     */
    private static AccessorShortData createShort(
            AccessorModel accessorModel, ByteBuffer bufferViewByteBuffer) {
        return new AccessorShortData(accessorModel.getComponentType(),
                bufferViewByteBuffer,
                accessorModel.getByteOffset(),
                accessorModel.getCount(),
                accessorModel.getElementType().getNumComponents(),
                accessorModel.getByteStride());
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorIntData}를 만든다
     *
     * @param accessorModel {@link AccessorModel}
     * @return {@link AccessorIntData}
     * @throws IllegalArgumentException 주어진 accessorModel의
     *                                  {@link AccessorModel#getComponentType() 성분 종류}가
     *                                  <code>GL_INT</code>나 <code>GL_UNSIGNED_INT</code>가 아닐 때
     */
    static AccessorIntData createInt(AccessorModel accessorModel) {
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
        return createInt(accessorModel, bufferViewModel.getBufferViewData());
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorIntData}를 만든다
     *
     * @param accessorModel        {@link AccessorModel}
     * @param bufferViewByteBuffer {@link AccessorModel}이 참조하는
     *                             {@link BufferViewModel}의 바이트 버퍼
     * @return {@link AccessorIntData}
     * @throws NullPointerException     인자 중 하나라도 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 accessorModel의
     *                                  {@link AccessorModel#getComponentType() 성분 종류}가
     *                                  <code>GL_INT</code>나 <code>GL_UNSIGNED_INT</code>가 아닐 때
     */
    private static AccessorIntData createInt(
            AccessorModel accessorModel, ByteBuffer bufferViewByteBuffer) {
        return new AccessorIntData(accessorModel.getComponentType(),
                bufferViewByteBuffer,
                accessorModel.getByteOffset(),
                accessorModel.getCount(),
                accessorModel.getElementType().getNumComponents(),
                accessorModel.getByteStride());
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorFloatData}를 만든다
     *
     * @param accessorModel {@link AccessorModel}
     * @return {@link AccessorFloatData}
     * @throws IllegalArgumentException 주어진 accessorModel의
     *                                  {@link AccessorModel#getComponentType() 성분 종류}가
     *                                  <code>GL_FLOAT</code>가 아닐 때
     */
    public static AccessorFloatData createFloat(AccessorModel accessorModel) {
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
        return createFloat(accessorModel, bufferViewModel.getBufferViewData());
    }

    /**
     * 주어진 {@link AccessorModel}의 {@link AccessorFloatData}를 만든다
     *
     * @param accessorModel        {@link AccessorModel}
     * @param bufferViewByteBuffer {@link AccessorModel}이 참조하는
     *                             {@link BufferViewModel}의 바이트 버퍼
     * @return {@link AccessorFloatData}
     * @throws NullPointerException     인자 중 하나라도 <code>null</code>일 때
     * @throws IllegalArgumentException 성분 종류가 맞지 않을 때
     */
    private static AccessorFloatData createFloat(
            AccessorModel accessorModel, ByteBuffer bufferViewByteBuffer) {
        return new AccessorFloatData(accessorModel.getComponentType(),
                bufferViewByteBuffer,
                accessorModel.getByteOffset(),
                accessorModel.getCount(),
                accessorModel.getElementType().getNumComponents(),
                accessorModel.getByteStride());
    }

    /**
     * 주어진 {@link AccessorModel} 매개변수가 주어진 용량의 버퍼에
     * 접근하기에 올바른지 검사한다
     *
     * @param byteOffset           바이트 오프셋
     * @param numElements          요소 수
     * @param byteStridePerElement 바이트 간격
     * @param bufferCapacity       버퍼 용량
     * @throws IllegalArgumentException 주어진 바이트 버퍼의
     *                                  용량이 모자랄 때
     */
    static void validateCapacity(int byteOffset, int numElements,
                                 int byteStridePerElement, int bufferCapacity) {
        int expectedCapacity = numElements * byteStridePerElement;
        if (expectedCapacity > bufferCapacity) {
            throw new IllegalArgumentException(
                    "The accessorModel has an offset of " + byteOffset + " and " +
                            numElements + " elements with a byte stride of " +
                            byteStridePerElement + ", requiring " + expectedCapacity +
                            " bytes, but the buffer view has only " +
                            bufferCapacity + " bytes");
        }
    }

    /**
     * 주어진 {@link AccessorData}의 성분별 최솟값을
     * 계산한다
     *
     * @param accessorData {@link AccessorData}
     * @return 최솟값
     * @throws IllegalArgumentException 주어진 모델의 종류를 알 수 없을 때
     */
    public static Number[] computeMin(AccessorData accessorData) {
        if (accessorData instanceof AccessorByteData) {
            AccessorByteData accessorByteData =
                    (AccessorByteData) accessorData;
            return NumberArrays.asNumbers(
                    accessorByteData.computeMinInt());
        }
        if (accessorData instanceof AccessorShortData) {
            AccessorShortData accessorShortData =
                    (AccessorShortData) accessorData;
            return NumberArrays.asNumbers(
                    accessorShortData.computeMinInt());
        }
        if (accessorData instanceof AccessorIntData) {
            AccessorIntData accessorIntData =
                    (AccessorIntData) accessorData;
            return NumberArrays.asNumbers(
                    accessorIntData.computeMinLong());
        }
        if (accessorData instanceof AccessorFloatData) {
            AccessorFloatData accessorFloatData =
                    (AccessorFloatData) accessorData;
            return NumberArrays.asNumbers(
                    accessorFloatData.computeMin());
        }
        throw new IllegalArgumentException(
                "Invalid data type: " + accessorData);
    }

    /**
     * 주어진 {@link AccessorData}의 성분별 최댓값을
     * 계산한다
     *
     * @param accessorData {@link AccessorData}
     * @return 최댓값
     * @throws IllegalArgumentException 주어진 모델의 종류를 알 수 없을 때
     */
    public static Number[] computeMax(AccessorData accessorData) {
        if (accessorData instanceof AccessorByteData) {
            AccessorByteData accessorByteData =
                    (AccessorByteData) accessorData;
            return NumberArrays.asNumbers(
                    accessorByteData.computeMaxInt());
        }
        if (accessorData instanceof AccessorShortData) {
            AccessorShortData accessorShortData =
                    (AccessorShortData) accessorData;
            return NumberArrays.asNumbers(
                    accessorShortData.computeMaxInt());
        }
        if (accessorData instanceof AccessorIntData) {
            AccessorIntData accessorIntData =
                    (AccessorIntData) accessorData;
            return NumberArrays.asNumbers(
                    accessorIntData.computeMaxLong());
        }
        if (accessorData instanceof AccessorFloatData) {
            AccessorFloatData accessorFloatData =
                    (AccessorFloatData) accessorData;
            return NumberArrays.asNumbers(
                    accessorFloatData.computeMax());
        }
        throw new IllegalArgumentException(
                "Invalid data type: " + accessorData);
    }

    /**
     * 주어진 데이터 종류에 따라
     * {@link AccessorByteData#createString(Locale, String, int)},
     * {@link AccessorShortData#createString(Locale, String, int)},
     * {@link AccessorIntData#createString(Locale, String, int)},
     * {@link AccessorFloatData#createString(Locale, String, int)} 중 하나를
     * 정해지지 않은 형식 문자열로 호출해,
     * 주어진 {@link AccessorData}의 문자열 표현을 만든다(아주 길 수 있다!).
     *
     * @param accessorData   {@link AccessorData}
     * @param elementsPerRow 한 줄에 넣을 요소 수
     * @return 문자열
     */
    public static String createString(
            AccessorData accessorData, int elementsPerRow) {
        if (accessorData instanceof AccessorByteData) {
            AccessorByteData accessorByteData =
                    (AccessorByteData) accessorData;
            String accessorDataString =
                    accessorByteData.createString(
                            Locale.ENGLISH, "%4d", elementsPerRow);
            return accessorDataString;
        }
        if (accessorData instanceof AccessorShortData) {
            AccessorShortData accessorShortData =
                    (AccessorShortData) accessorData;
            String accessorDataString =
                    accessorShortData.createString(
                            Locale.ENGLISH, "%6d", elementsPerRow);
            return accessorDataString;
        }
        if (accessorData instanceof AccessorIntData) {
            AccessorIntData accessorIntData =
                    (AccessorIntData) accessorData;
            String accessorDataString =
                    accessorIntData.createString(
                            Locale.ENGLISH, "%11d", elementsPerRow);
            return accessorDataString;
        }
        if (accessorData instanceof AccessorFloatData) {
            AccessorFloatData accessorFloatData =
                    (AccessorFloatData) accessorData;
            String accessorDataString =
                    accessorFloatData.createString(
                            Locale.ENGLISH, "%10.5f", elementsPerRow);
            return accessorDataString;
        }
        return "Unknown accessor data type: " + accessorData;
    }
}

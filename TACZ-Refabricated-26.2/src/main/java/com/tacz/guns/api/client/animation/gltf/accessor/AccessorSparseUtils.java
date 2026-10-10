package com.tacz.guns.api.client.animation.gltf.accessor;

import java.util.logging.Logger;

public class AccessorSparseUtils {
    /**
     * 이 클래스에서 쓰는 로거
     */
    private static final Logger logger =
            Logger.getLogger(AccessorSparseUtils.class.getName());

    /**
     * 인스턴스를 만들지 못하게 막는 비공개 생성자
     */
    private AccessorSparseUtils() {
        // 인스턴스를 만들지 못하게 막는 비공개 생성자
    }

    /**
     * 주어진 {@link AccessorData}에서 인덱스를 꺼낸다. 주어진
     * {@link AccessorData}는 정수 타입이어야 한다. 즉
     * {@link AccessorData#getComponentType() 성분 종류}가
     * <code>byte.class</code>, <code>short.class</code>,
     * <code>int.class</code> 중 하나여야 한다.
     *
     * @param accessorData {@link AccessorData}
     * @return 인덱스
     * @throws IllegalArgumentException 주어진 데이터가
     *                                  정수 타입이 아닐 때
     */
    private static int[] extractIndices(AccessorData accessorData) {
        if (accessorData.getComponentType() == byte.class) {
            AccessorByteData accessorByteData =
                    (AccessorByteData) accessorData;
            int numElements = accessorByteData.getNumElements();
            int indices[] = new int[numElements];
            for (int i = 0; i < numElements; i++) {
                indices[i] = accessorByteData.getInt(i, 0);
            }
            return indices;
        }
        if (accessorData.getComponentType() == short.class) {
            AccessorShortData accessorShortData =
                    (AccessorShortData) accessorData;
            int numElements = accessorShortData.getNumElements();
            int indices[] = new int[numElements];
            for (int i = 0; i < numElements; i++) {
                indices[i] = accessorShortData.getInt(i, 0);
            }
            return indices;
        }
        if (accessorData.getComponentType() == int.class) {
            AccessorIntData accessorIntData =
                    (AccessorIntData) accessorData;
            int numElements = accessorIntData.getNumElements();
            int indices[] = new int[numElements];
            for (int i = 0; i < numElements; i++) {
                indices[i] = accessorIntData.get(i, 0);
            }
            return indices;
        }
        throw new IllegalArgumentException(
                "Invalid type for indices: " + accessorData.getComponentType());
    }

    /**
     * 주어진 밀집 {@link AccessorData}의 데이터를, 주어진
     * {@link AccessorData} 객체들이 제공하는 (희소) 데이터로 치환한다. <br>
     * <br>
     * <code>baseAccessorData</code>는 주어진 희소 인덱스와 값으로 치환을
     * 적용하기 <b>전에</b> 밀집 데이터를 초기화할 데이터다.<br>
     * <br>
     * <code>sparseIndicesAccessorData</code>는
     * <code>accessor.sparse.indices</code> 구조로 만든
     * {@link AccessorData}다.<br>
     * <br>
     * <code>sparseValuesAccessorData</code>는
     * <code>accessor.sparse.values</code> 구조로 만든
     * {@link AccessorData}다.<br>
     * <br>
     * 이 메서드는 유효성 검사를 거의 하지 않는다. 호출하는 쪽이
     * (인덱스와 데이터 타입 면에서) 올바른 인자로만 호출해야
     * 한다.
     *
     * @param denseAccessorData         채울 밀집 {@link AccessorData}
     * @param baseAccessorData          선택적 "기반" {@link AccessorData}
     * @param sparseIndicesAccessorData 희소 인덱스 {@link AccessorData}
     * @param sparseValuesAccessorData  희소 값 {@link AccessorData}
     * @throws IllegalArgumentException sparseIndicesAccessorData가
     *                                  정수 타입(byte, short, int) 데이터가 아닐 때
     */
    public static void substituteAccessorData(
            AccessorData denseAccessorData,
            AccessorData baseAccessorData,
            AccessorData sparseIndicesAccessorData,
            AccessorData sparseValuesAccessorData) {
        Class<?> componentType = denseAccessorData.getComponentType();
        if (componentType == byte.class) {
            AccessorByteData sparseValuesAccessorByteData =
                    (AccessorByteData) sparseValuesAccessorData;
            AccessorByteData baseAccessorByteData =
                    (AccessorByteData) baseAccessorData;
            AccessorByteData denseAccessorByteData =
                    (AccessorByteData) denseAccessorData;
            substituteByteAccessorData(
                    denseAccessorByteData,
                    baseAccessorByteData,
                    sparseIndicesAccessorData,
                    sparseValuesAccessorByteData);
        } else if (componentType == short.class) {
            AccessorShortData sparseValuesAccessorShortData =
                    (AccessorShortData) sparseValuesAccessorData;
            AccessorShortData baseAccessorShortData =
                    (AccessorShortData) baseAccessorData;
            AccessorShortData denseAccessorShortData =
                    (AccessorShortData) denseAccessorData;
            substituteShortAccessorData(
                    denseAccessorShortData,
                    baseAccessorShortData,
                    sparseIndicesAccessorData,
                    sparseValuesAccessorShortData);
        } else if (componentType == int.class) {
            AccessorIntData sparseValuesAccessorIntData =
                    (AccessorIntData) sparseValuesAccessorData;
            AccessorIntData baseAccessorIntData =
                    (AccessorIntData) baseAccessorData;
            AccessorIntData denseAccessorIntData =
                    (AccessorIntData) denseAccessorData;
            substituteIntAccessorData(
                    denseAccessorIntData,
                    baseAccessorIntData,
                    sparseIndicesAccessorData,
                    sparseValuesAccessorIntData);
        } else if (componentType == float.class) {
            AccessorFloatData sparseValuesAccessorFloatData =
                    (AccessorFloatData) sparseValuesAccessorData;
            AccessorFloatData baseAccessorFloatData =
                    (AccessorFloatData) baseAccessorData;
            AccessorFloatData denseAccessorFloatData =
                    (AccessorFloatData) denseAccessorData;

            substituteFloatAccessorData(
                    denseAccessorFloatData,
                    baseAccessorFloatData,
                    sparseIndicesAccessorData,
                    sparseValuesAccessorFloatData);
        } else {
            logger.warning("Invalid component type for accessor: "
                    + componentType);
        }
    }

    /**
     * {@link #substituteAccessorData} 참고
     *
     * @param denseAccessorData         채울 밀집 {@link AccessorData}
     * @param baseAccessorData          선택적 "기반" {@link AccessorData}
     * @param sparseIndicesAccessorData 희소 인덱스 {@link AccessorData}
     * @param sparseValuesAccessorData  희소 값 {@link AccessorData}
     * @throws IllegalArgumentException sparseIndicesAccessorData가
     *                                  정수 타입(byte, short, int) 데이터가 아닐 때
     */
    private static void substituteByteAccessorData(
            AccessorByteData denseAccessorData,
            AccessorByteData baseAccessorData,
            AccessorData sparseIndicesAccessorData,
            AccessorByteData sparseValuesAccessorData) {
        int numElements = denseAccessorData.getNumElements();
        int numComponentsPerElement =
                denseAccessorData.getNumComponentsPerElement();

        if (baseAccessorData != null) {
            // 밀집 AccessorData를 기반 데이터로 채운다
            for (int e = 0; e < numElements; e++) {
                for (int c = 0; c < numComponentsPerElement; c++) {
                    byte value = baseAccessorData.get(e, c);
                    denseAccessorData.set(e, c, value);
                }
            }
        }

        // 희소 인덱스와 값으로 치환을 적용한다
        int indices[] = extractIndices(sparseIndicesAccessorData);
        for (int i = 0; i < indices.length; i++) {
            int targetElementIndex = indices[i];
            for (int c = 0; c < numComponentsPerElement; c++) {
                byte substitution = sparseValuesAccessorData.get(i, c);
                denseAccessorData.set(targetElementIndex, c, substitution);
            }
        }
    }

    /**
     * {@link #substituteAccessorData} 참고
     *
     * @param denseAccessorData         채울 밀집 {@link AccessorData}
     * @param baseAccessorData          선택적 "기반" {@link AccessorData}
     * @param sparseIndicesAccessorData 희소 인덱스 {@link AccessorData}
     * @param sparseValuesAccessorData  희소 값 {@link AccessorData}
     * @throws IllegalArgumentException sparseIndicesAccessorData가
     *                                  정수 타입(byte, short, int) 데이터가 아닐 때
     */
    private static void substituteShortAccessorData(
            AccessorShortData denseAccessorData,
            AccessorShortData baseAccessorData,
            AccessorData sparseIndicesAccessorData,
            AccessorShortData sparseValuesAccessorData) {
        int numElements = denseAccessorData.getNumElements();
        int numComponentsPerElement =
                denseAccessorData.getNumComponentsPerElement();

        if (baseAccessorData != null) {
            // 밀집 AccessorData를 기반 데이터로 채운다
            for (int e = 0; e < numElements; e++) {
                for (int c = 0; c < numComponentsPerElement; c++) {
                    short value = baseAccessorData.get(e, c);
                    denseAccessorData.set(e, c, value);
                }
            }
        }

        // 희소 인덱스와 값으로 치환을 적용한다
        int indices[] = extractIndices(sparseIndicesAccessorData);
        for (int i = 0; i < indices.length; i++) {
            int targetElementIndex = indices[i];
            for (int c = 0; c < numComponentsPerElement; c++) {
                short substitution = sparseValuesAccessorData.get(i, c);
                denseAccessorData.set(targetElementIndex, c, substitution);
            }
        }
    }

    /**
     * {@link #substituteAccessorData} 참고
     *
     * @param denseAccessorData         채울 밀집 {@link AccessorData}
     * @param baseAccessorData          선택적 "기반" {@link AccessorData}
     * @param sparseIndicesAccessorData 희소 인덱스 {@link AccessorData}
     * @param sparseValuesAccessorData  희소 값 {@link AccessorData}
     * @throws IllegalArgumentException sparseIndicesAccessorData가
     *                                  정수 타입(byte, short, int) 데이터가 아닐 때
     */
    private static void substituteIntAccessorData(
            AccessorIntData denseAccessorData,
            AccessorIntData baseAccessorData,
            AccessorData sparseIndicesAccessorData,
            AccessorIntData sparseValuesAccessorData) {
        int numElements = denseAccessorData.getNumElements();
        int numComponentsPerElement =
                denseAccessorData.getNumComponentsPerElement();

        if (baseAccessorData != null) {
            // 밀집 AccessorData를 기반 데이터로 채운다
            for (int e = 0; e < numElements; e++) {
                for (int c = 0; c < numComponentsPerElement; c++) {
                    int value = baseAccessorData.get(e, c);
                    denseAccessorData.set(e, c, value);
                }
            }
        }

        // 희소 인덱스와 값으로 치환을 적용한다
        int indices[] = extractIndices(sparseIndicesAccessorData);
        for (int i = 0; i < indices.length; i++) {
            int targetElementIndex = indices[i];
            for (int c = 0; c < numComponentsPerElement; c++) {
                int substitution = sparseValuesAccessorData.get(i, c);
                denseAccessorData.set(targetElementIndex, c, substitution);
            }
        }
    }

    /**
     * {@link #substituteAccessorData} 참고
     *
     * @param denseAccessorData         채울 밀집 {@link AccessorData}
     * @param baseAccessorData          선택적 "기반" {@link AccessorData}
     * @param sparseIndicesAccessorData 희소 인덱스 {@link AccessorData}
     * @param sparseValuesAccessorData  희소 값 {@link AccessorData}
     * @throws IllegalArgumentException sparseIndicesAccessorData가
     *                                  정수 타입(byte, short, int) 데이터가 아닐 때
     */
    private static void substituteFloatAccessorData(
            AccessorFloatData denseAccessorData,
            AccessorFloatData baseAccessorData,
            AccessorData sparseIndicesAccessorData,
            AccessorFloatData sparseValuesAccessorData) {
        int numElements = denseAccessorData.getNumElements();
        int numComponentsPerElement =
                denseAccessorData.getNumComponentsPerElement();

        if (baseAccessorData != null) {
            // 밀집 AccessorData를 기반 데이터로 채운다
            for (int e = 0; e < numElements; e++) {
                for (int c = 0; c < numComponentsPerElement; c++) {
                    float value = baseAccessorData.get(e, c);
                    denseAccessorData.set(e, c, value);
                }
            }
        }

        // 희소 인덱스와 값으로 치환을 적용한다
        int indices[] = extractIndices(sparseIndicesAccessorData);
        for (int i = 0; i < indices.length; i++) {
            int targetElementIndex = indices[i];
            for (int c = 0; c < numComponentsPerElement; c++) {
                float substitution = sparseValuesAccessorData.get(i, c);
                denseAccessorData.set(targetElementIndex, c, substitution);
            }
        }
    }
}

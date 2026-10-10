package com.tacz.guns.api.client.animation.gltf;

public enum ElementType {
    /**
     * 스칼라 종류
     */
    SCALAR(1),

    /**
     * 2차원 벡터 종류
     */
    VEC2(2),

    /**
     * 3차원 벡터 종류
     */
    VEC3(3),

    /**
     * 4차원 벡터 종류
     */
    VEC4(4),

    /**
     * 2x2 행렬 종류
     */
    MAT2(4),

    /**
     * 3x3 행렬 종류
     */
    MAT3(9),

    /**
     * 4x4 행렬 종류
     */
    MAT4(16);

    /**
     * 요소 하나를 이루는 성분 수
     */
    private final int numComponents;

    /**
     * 주어진 성분 수로 새 인스턴스를 만든다
     *
     * @param numComponents 성분 수
     */
    ElementType(int numComponents) {
        this.numComponents = numComponents;
    }

    /**
     * 주어진 문자열이 올바른 요소 종류 이름이어서 예외 없이
     * <code>ElementType.valueOf</code>에 넘길 수 있는지 돌려준다.
     *
     * @param s 문자열
     * @return 올바른 요소 종류인지
     */
    public static boolean contains(String s) {
        for (ElementType elementType : values()) {
            if (elementType.name().equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 주어진 문자열에 해당하는 요소 종류를 돌려준다. 문자열이
     * <code>null</code>이거나 올바른 요소 종류가 아니면
     * <code>null</code>을 돌려준다
     *
     * @param string 문자열
     * @return 요소 종류
     */
    public static ElementType forString(String string) {
        if (string == null) {
            return null;
        }
        if (!contains(string)) {
            return null;
        }
        return ElementType.valueOf(string);
    }

    /**
     * 요소 하나를 이루는 성분 수를 돌려준다
     *
     * @return 성분 수
     */
    public int getNumComponents() {
        return numComponents;
    }
}

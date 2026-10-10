package com.tacz.guns.api.client.animation.gltf.accessor;

import com.tacz.guns.api.client.animation.gltf.GltfConstants;

public class Accessors {
    /**
     * 인스턴스를 만들지 못하게 막는 비공개 생성자
     */
    private Accessors() {
        // 인스턴스를 만들지 못하게 막는 비공개 생성자
    }

    /**
     * 주어진 접근자 종류에서 요소 하나가 가진 성분 수를 돌려준다.
     * 올바른 매개변수는 다음과 같다
     * <pre><code>
     * "SCALAR" :  1
     * "VEC2"   :  2
     * "VEC3"   :  3
     * "VEC4"   :  4
     * "MAT2"   :  4
     * "MAT3"   :  9
     * "MAT4"   : 16
     * </code></pre>
     *
     * @param accessorType 접근자 종류
     * @return 성분 수
     * @throws IllegalArgumentException 주어진 종류가 올바른
     *                                  매개변수가 아닐 때
     */
    public static int getNumComponentsForAccessorType(String accessorType) {
        switch (accessorType) {
            case "SCALAR":
                return 1;
            case "VEC2":
                return 2;
            case "VEC3":
                return 3;
            case "VEC4":
                return 4;
            case "MAT2":
                return 4;
            case "MAT3":
                return 9;
            case "MAT4":
                return 16;
            default:
                break;
        }
        throw new IllegalArgumentException(
                "Invalid accessor type: " + accessorType);
    }

    /**
     * 주어진 접근자 성분 종류의 성분 하나가 차지하는
     * 바이트 수를 돌려준다.
     * 올바른 매개변수는 다음과 같다
     * <pre><code>
     * GL_BYTE           : 1
     * GL_UNSIGNED_BYTE  : 1
     * GL_SHORT          : 2
     * GL_UNSIGNED_SHORT : 2
     * GL_INT            : 4
     * GL_UNSIGNED_INT   : 4
     * GL_FLOAT          : 4
     * </code></pre>
     *
     * @param componentType 성분 종류
     * @return 바이트 수
     * @throws IllegalArgumentException 주어진 종류가 올바른
     *                                  매개변수가 아닐 때
     */
    public static int getNumBytesForAccessorComponentType(int componentType) {
        switch (componentType) {
            case GltfConstants.GL_BYTE:
                return 1;
            case GltfConstants.GL_UNSIGNED_BYTE:
                return 1;
            case GltfConstants.GL_SHORT:
                return 2;
            case GltfConstants.GL_UNSIGNED_SHORT:
                return 2;
            case GltfConstants.GL_INT:
                return 4;
            case GltfConstants.GL_UNSIGNED_INT:
                return 4;
            case GltfConstants.GL_FLOAT:
                return 4;
            default:
                break;
        }
        throw new IllegalArgumentException(
                "Invalid accessor component type: " + componentType);
    }

    /**
     * 주어진 접근자 성분 종류의 데이터 타입을 돌려준다.
     * 올바른 매개변수와 반환값은 다음과 같다
     * <pre><code>
     * GL_BYTE           : byte.class
     * GL_UNSIGNED_BYTE  : byte.class
     * GL_SHORT          : short.class
     * GL_UNSIGNED_SHORT : short.class
     * GL_INT            : int.class
     * GL_UNSIGNED_INT   : int.class
     * GL_FLOAT          : float.class
     * </code></pre>
     *
     * @param componentType 성분 종류
     * @return 데이터 타입
     * @throws IllegalArgumentException 주어진 종류가 올바른
     *                                  매개변수가 아닐 때
     */
    public static Class<?> getDataTypeForAccessorComponentType(
            int componentType) {
        switch (componentType) {
            case GltfConstants.GL_BYTE:
                return byte.class;
            case GltfConstants.GL_UNSIGNED_BYTE:
                return byte.class;
            case GltfConstants.GL_SHORT:
                return short.class;
            case GltfConstants.GL_UNSIGNED_SHORT:
                return short.class;
            case GltfConstants.GL_INT:
                return int.class;
            case GltfConstants.GL_UNSIGNED_INT:
                return int.class;
            case GltfConstants.GL_FLOAT:
                return float.class;
            default:
                break;
        }
        throw new IllegalArgumentException(
                "Invalid accessor component type: " + componentType);
    }
}

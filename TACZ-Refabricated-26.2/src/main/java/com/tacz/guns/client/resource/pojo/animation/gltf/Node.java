package com.tacz.guns.client.resource.pojo.animation.gltf;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;
    private List<Integer> children;
    /**
     * 열 우선 순서로 저장한 부동소수점 4x4 변환
     * 행렬. (선택)<br>
     * 기본값:
     * [1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0]<br>
     * 항목 수: 16<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     */
    private float[] matrix;
    /**
     * (x, y, z, w) 순서의 노드 단위 사원수 회전. w는
     * 스칼라다. (선택)<br>
     * 기본값: [0.0,0.0,0.0,1.0]<br>
     * 항목 수: 4<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)<br>
     * &nbsp;&nbsp;최솟값: -1.0 (포함)<br>
     * &nbsp;&nbsp;최댓값: 1.0 (포함)
     */
    private float[] rotation;
    /**
     * x, y, z 축을 따른 크기 배율로 나타낸 노드의
     * 비균일 크기. (선택)<br>
     * 기본값: [1.0,1.0,1.0]<br>
     * 항목 수: 3<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     */
    private float[] scale;
    /**
     * x, y, z 축을 따른 노드의 이동량. (선택)<br>
     * 기본값: [0.0,0.0,0.0]<br>
     * 항목 수: 3<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     */
    private float[] translation;

    /**
     * 이 노드 자식들의 인덱스. (선택)<br>
     * 최소 항목 수: 1<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)<br>
     * &nbsp;&nbsp;최솟값: 0 (포함)
     *
     * @return children
     */
    public List<Integer> getChildren() {
        return this.children;
    }

    /**
     * 이 노드 자식들의 인덱스. (선택)<br>
     * 최소 항목 수: 1<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)<br>
     * &nbsp;&nbsp;최솟값: 0 (포함)
     *
     * @param children 설정할 children
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setChildren(List<Integer> children) {
        if (children == null) {
            this.children = children;
            return;
        }
        if (children.size() < 1) {
            throw new IllegalArgumentException("Number of children elements is < 1");
        }
        for (Integer childrenElement : children) {
            if (childrenElement < 0) {
                throw new IllegalArgumentException("childrenElement < 0");
            }
        }
        this.children = children;
    }

    /**
     * 주어진 children을 추가한다. 이 인스턴스의 children은 이전 요소 전체에
     * 새 요소를 더한 목록으로
     * 바뀐다.
     *
     * @param element 요소
     * @throws NullPointerException 주어진 요소가 <code>null</code>일 때
     */
    public void addChildren(Integer element) {
        if (element == null) {
            throw new NullPointerException("The element may not be null");
        }
        List<Integer> oldList = this.children;
        List<Integer> newList = new ArrayList<Integer>();
        if (oldList != null) {
            newList.addAll(oldList);
        }
        newList.add(element);
        this.children = newList;
    }

    /**
     * 주어진 children을 제거한다. 이 인스턴스의 children은 제거한 요소를 뺀
     * 이전 요소 전체를 담은 목록으로
     * 바뀐다.<br>
     * 새 목록이 비게 되면
     * <code>null</code>로 설정된다.
     *
     * @param element 요소
     * @throws NullPointerException 주어진 요소가 <code>null</code>일 때
     */
    public void removeChildren(Integer element) {
        if (element == null) {
            throw new NullPointerException("The element may not be null");
        }
        List<Integer> oldList = this.children;
        List<Integer> newList = new ArrayList<Integer>();
        if (oldList != null) {
            newList.addAll(oldList);
        }
        newList.remove(element);
        if (newList.isEmpty()) {
            this.children = null;
        } else {
            this.children = newList;
        }
    }

    /**
     * 열 우선 순서로 저장한 부동소수점 4x4 변환
     * 행렬. (선택)<br>
     * 기본값:
     * [1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0]<br>
     * 항목 수: 16<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @return matrix
     */
    public float[] getMatrix() {
        return this.matrix;
    }

    public void setMatrix(float[] matrix) {
        if (matrix == null) {
            this.matrix = matrix;
            return;
        }
        if (matrix.length < 16) {
            throw new IllegalArgumentException("Number of matrix elements is < 16");
        }
        if (matrix.length > 16) {
            throw new IllegalArgumentException("Number of matrix elements is > 16");
        }
        this.matrix = matrix;
    }

    /**
     * (x, y, z, w) 순서의 노드 단위 사원수 회전. w는
     * 스칼라다. (선택)<br>
     * 기본값: [0.0,0.0,0.0,1.0]<br>
     * 항목 수: 4<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)<br>
     * &nbsp;&nbsp;최솟값: -1.0 (포함)<br>
     * &nbsp;&nbsp;최댓값: 1.0 (포함)
     *
     * @return rotation
     */
    public float[] getRotation() {
        return this.rotation;
    }

    /**
     * (x, y, z, w) 순서의 노드 단위 사원수 회전. w는
     * 스칼라다. (선택)<br>
     * 기본값: [0.0,0.0,0.0,1.0]<br>
     * 항목 수: 4<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)<br>
     * &nbsp;&nbsp;최솟값: -1.0 (포함)<br>
     * &nbsp;&nbsp;최댓값: 1.0 (포함)
     *
     * @param rotation 설정할 rotation
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setRotation(float[] rotation) {
        if (rotation == null) {
            this.rotation = rotation;
            return;
        }
        if (rotation.length < 4) {
            throw new IllegalArgumentException("Number of rotation elements is < 4");
        }
        if (rotation.length > 4) {
            throw new IllegalArgumentException("Number of rotation elements is > 4");
        }
        for (float rotationElement : rotation) {
            if (rotationElement > 1.0D) {
                throw new IllegalArgumentException("rotationElement > 1.0");
            }
            if (rotationElement < -1.0D) {
                throw new IllegalArgumentException("rotationElement < -1.0");
            }
        }
        this.rotation = rotation;
    }

    /**
     * x, y, z 축을 따른 크기 배율로 나타낸 노드의
     * 비균일 크기. (선택)<br>
     * 기본값: [1.0,1.0,1.0]<br>
     * 항목 수: 3<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @return scale
     */
    public float[] getScale() {
        return this.scale;
    }

    /**
     * x, y, z 축을 따른 크기 배율로 나타낸 노드의
     * 비균일 크기. (선택)<br>
     * 기본값: [1.0,1.0,1.0]<br>
     * 항목 수: 3<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @param scale 설정할 scale
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setScale(float[] scale) {
        if (scale == null) {
            this.scale = scale;
            return;
        }
        if (scale.length < 3) {
            throw new IllegalArgumentException("Number of scale elements is < 3");
        }
        if (scale.length > 3) {
            throw new IllegalArgumentException("Number of scale elements is > 3");
        }
        this.scale = scale;
    }

    /**
     * x, y, z 축을 따른 노드의 이동량. (선택)<br>
     * 기본값: [0.0,0.0,0.0]<br>
     * 항목 수: 3<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @return translation
     */
    public float[] getTranslation() {
        return this.translation;
    }

    /**
     * x, y, z 축을 따른 노드의 이동량. (선택)<br>
     * 기본값: [0.0,0.0,0.0]<br>
     * 항목 수: 3<br>
     * 배열 요소:<br>
     * &nbsp;&nbsp;이 배열의 요소 (선택)
     *
     * @param translation 설정할 translation
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setTranslation(float[] translation) {
        if (translation == null) {
            this.translation = translation;
            return;
        }
        if (translation.length < 3) {
            throw new IllegalArgumentException("Number of translation elements is < 3");
        }
        if (translation.length > 3) {
            throw new IllegalArgumentException("Number of translation elements is > 3");
        }
        this.translation = translation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

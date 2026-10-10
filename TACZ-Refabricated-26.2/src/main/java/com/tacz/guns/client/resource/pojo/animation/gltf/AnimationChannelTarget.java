package com.tacz.guns.client.resource.pojo.animation.gltf;

public class AnimationChannelTarget {
    /**
     * 애니메이션할 노드의 인덱스. 정의하지 않으면 애니메이션 대상 객체를
     * 확장에서 정의할 **수 있다**. (선택)
     */
    private Integer node;

    /**
     * 애니메이션할 노드의 TRS 속성 이름, 또는 노드가 만드는 모프 타깃의
     * `"weights"`. `"translation"` 속성이면
     * 샘플러가 주는 값은 X, Y, Z 축을 따른
     * 이동량이다. `"rotation"` 속성이면 값은
     * (x, y, z, w) 순서의 사원수이며 w가 스칼라다.
     * `"scale"` 속성이면 값은 X, Y, Z 축을 따른
     * 크기 배율이다. (필수)<br>
     * 허용 값: [translation, rotation, scale, weights]
     */
    private String path;

    /**
     * 애니메이션할 노드의 인덱스. 정의하지 않으면 애니메이션 대상 객체를
     * 확장에서 정의할 **수 있다**. (선택)
     *
     * @return node
     */
    public Integer getNode() {
        return this.node;
    }

    /**
     * 애니메이션할 노드의 인덱스. 정의하지 않으면 애니메이션 대상 객체를
     * 확장에서 정의할 **수 있다**. (선택)
     *
     * @param node 설정할 node
     */
    public void setNode(Integer node) {
        if (node == null) {
            this.node = node;
            return;
        }
        this.node = node;
    }

    /**
     * 애니메이션할 노드의 TRS 속성 이름, 또는 노드가 만드는 모프 타깃의
     * `"weights"`. `"translation"` 속성이면
     * 샘플러가 주는 값은 X, Y, Z 축을 따른
     * 이동량이다. `"rotation"` 속성이면 값은
     * (x, y, z, w) 순서의 사원수이며 w가 스칼라다.
     * `"scale"` 속성이면 값은 X, Y, Z 축을 따른
     * 크기 배율이다. (필수)<br>
     * 허용 값: [translation, rotation, scale, weights]
     *
     * @return path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * 애니메이션할 노드의 TRS 속성 이름, 또는 노드가 만드는 모프 타깃의
     * `"weights"`. `"translation"` 속성이면
     * 샘플러가 주는 값은 X, Y, Z 축을 따른
     * 이동량이다. `"rotation"` 속성이면 값은
     * (x, y, z, w) 순서의 사원수이며 w가 스칼라다.
     * `"scale"` 속성이면 값은 X, Y, Z 축을 따른
     * 크기 배율이다. (필수)<br>
     * 허용 값: [translation, rotation, scale, weights]
     *
     * @param path 설정할 path
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setPath(String path) {
        if (path == null) {
            throw new NullPointerException((("Invalid value for path: " + path) + ", may not be null"));
        }
        if ((((!"translation".equals(path)) && (!"rotation".equals(path))) && (!"scale".equals(path))) && (!"weights".equals(path))) {
            throw new IllegalArgumentException((("Invalid value for path: " + path) + ", valid: [translation, rotation, scale, weights]"));
        }
        this.path = path;
    }
}

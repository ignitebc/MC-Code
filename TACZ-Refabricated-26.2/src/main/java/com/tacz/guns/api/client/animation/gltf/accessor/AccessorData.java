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

/**
 * 원시 접근자 데이터에 타입별로 접근하게 해 주는 클래스의 인터페이스.
 * 데이터의 정확한 종류(곧 구현 클래스)는
 * {@link #getComponentType() 성분 종류}가 정한다:<br>
 * <ul>
 *   <li><code>byte.class</code>면 구현은
 *   {@link AccessorByteData}</li>
 *   <li><code>short.class</code>면 구현은
 *   {@link AccessorShortData}</li>
 *   <li><code>int.class</code>면 구현은
 *   {@link AccessorIntData}</li>
 *   <li><code>float.class</code>면 구현은
 *   {@link AccessorFloatData}</li>
 * </ul>
 */
public interface AccessorData {
    /**
     * 이 클래스가 접근하게 해 주는 성분의 종류를 돌려준다.
     * 보통 <code>float.class</code>나 <code>short.class</code> 같은
     * 기본 타입이다.
     *
     * @return 성분 종류
     */
    Class<?> getComponentType();

    /**
     * 이 데이터의 요소 수(예: 3차원 벡터의 개수)를
     * 돌려준다
     *
     * @return 요소 수
     */
    int getNumElements();

    /**
     * 요소당 성분 수(예: 요소가 3차원 벡터면 3)를
     * 돌려준다
     *
     * @return 요소당 성분 수
     */
    int getNumComponentsPerElement();

    /**
     * 전체 성분 수(요소 수에 요소당 성분 수를
     * 곱한 값)를 돌려준다
     *
     * @return 전체 성분 수
     */
    int getTotalNumComponents();

    /**
     * 접근자의 데이터를 오프셋과 추가 간격 없이
     * 빽빽하게(모든 요소를 붙여서) 담은
     * 새 다이렉트 바이트 버퍼(네이티브 바이트 순서)를
     * 만든다.
     *
     * @return 바이트 버퍼
     */
    ByteBuffer createByteBuffer();

}

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

/**
 * 기본 타입 배열을 Number 객체 배열로 바꾸는 메서드 모음
 */
class NumberArrays {
    /**
     * 인스턴스를 만들지 못하게 막는 비공개 생성자
     */
    private NumberArrays() {
        // 인스턴스를 만들지 못하게 막는 비공개 생성자
    }

    /**
     * 주어진 배열을 Number 배열로 바꾼다
     *
     * @param array 배열
     * @return 결과
     */
    static Number[] asNumbers(int array[]) {
        Number result[] = new Number[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * 주어진 배열을 Number 배열로 바꾼다
     *
     * @param array 배열
     * @return 결과
     */
    static Number[] asNumbers(long array[]) {
        Number result[] = new Number[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * 주어진 배열을 Number 배열로 바꾼다
     *
     * @param array 배열
     * @return 결과
     */
    static Number[] asNumbers(float array[]) {
        Number result[] = new Number[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }
}

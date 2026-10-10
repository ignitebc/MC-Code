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
package com.tacz.guns.api.client.animation.gltf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 버퍼 관련 도우미 메서드
 */
public class Buffers {
    /**
     * 인스턴스를 만들지 못하게 막는 비공개 생성자
     */
    private Buffers() {
        // 인스턴스를 만들지 못하게 막는 비공개 생성자
    }

    /**
     * 주어진 바이트 버퍼의 현재 위치와 한계로 조각을 만든다.
     * 돌려주는 조각은 주어진 버퍼와 바이트 순서가 같다.
     * 주어진 버퍼가 <code>null</code>이면 <code>null</code>을 돌려준다.
     *
     * @param byteBuffer 바이트 버퍼
     * @return 조각
     */
    public static ByteBuffer createSlice(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        return byteBuffer.slice().order(byteBuffer.order());
    }

    /**
     * 주어진 바이트 버퍼에서 지정한 범위의 조각을 만든다.
     * 돌려주는 버퍼는 주어진 버퍼와 바이트 순서가 같다.
     * 주어진 버퍼가 <code>null</code>이면 <code>null</code>을 돌려준다.
     *
     * @param byteBuffer 바이트 버퍼
     * @param position   조각이 시작할 위치
     * @param length     조각 길이
     * @return 조각
     * @throws IllegalArgumentException 위치와 길이로 지정한 범위가
     *                                  주어진 버퍼에 맞지 않을 때
     */
    public static ByteBuffer createSlice(
            ByteBuffer byteBuffer, int position, int length) {
        if (byteBuffer == null) {
            return null;
        }
        int oldPosition = byteBuffer.position();
        int oldLimit = byteBuffer.limit();
        try {
            int newLimit = position + length;
            if (newLimit > byteBuffer.capacity()) {
                throw new IllegalArgumentException(
                        "The new limit is " + newLimit + ", but the capacity is "
                                + byteBuffer.capacity());
            }
            byteBuffer.limit(newLimit);
            byteBuffer.position(position);
            ByteBuffer slice = byteBuffer.slice();
            slice.order(byteBuffer.order());
            return slice;
        } finally {
            byteBuffer.limit(oldLimit);
            byteBuffer.position(oldPosition);
        }
    }

    /**
     * 주어진 데이터를 담은 새 다이렉트 바이트 버퍼를
     * 리틀 엔디언 순서로 만든다
     *
     * @param data 데이터
     * @return 바이트 버퍼
     */
    public static ByteBuffer create(byte data[]) {
        return create(data, 0, data.length);
    }

    /**
     * 주어진 데이터의 지정한 범위를 담은 새 다이렉트 바이트 버퍼를
     * 리틀 엔디언 순서로 만든다
     *
     * @param data   데이터
     * @param offset 데이터 배열 안의 오프셋
     * @param length 범위 길이
     * @return 바이트 버퍼
     */
    public static ByteBuffer create(byte data[], int offset, int length) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(data, offset, length);
        byteBuffer.position(0);
        return byteBuffer;
    }

    /**
     * 주어진 크기와 리틀 엔디언 순서로
     * 새 다이렉트 바이트 버퍼를 만든다.
     *
     * @param size 버퍼 크기
     * @return 바이트 버퍼
     * @throws IllegalArgumentException 크기가 음수일 때
     */
    public static ByteBuffer create(int size) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer;
    }
}

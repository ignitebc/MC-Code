package com.tacz.guns.api.client.animation.gltf;

import java.nio.ByteBuffer;

public class BufferModel {
    /**
     * 버퍼 데이터의 URI
     */
    private String uri;

    /**
     * 버퍼의 실제 데이터
     */
    private ByteBuffer bufferData;

    public String getUri() {
        return uri;
    }

    /**
     * 버퍼 데이터의 URI를 설정한다
     *
     * @param uri 버퍼 데이터의 URI
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getByteLength() {
        return bufferData.capacity();
    }

    public ByteBuffer getBufferData() {
        return Buffers.createSlice(bufferData);
    }

    /**
     * 이 버퍼의 데이터를 설정한다
     *
     * @param bufferData 버퍼 데이터
     */
    public void setBufferData(ByteBuffer bufferData) {
        this.bufferData = bufferData;
    }
}

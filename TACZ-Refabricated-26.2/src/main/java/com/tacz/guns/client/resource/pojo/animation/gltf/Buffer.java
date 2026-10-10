package com.tacz.guns.client.resource.pojo.animation.gltf;

public class Buffer {
    /**
     * 버퍼의 URI(또는 IRI). (선택)
     */
    private String uri;
    /**
     * 버퍼의 바이트 길이. (필수)<br>
     * 최솟값: 1 (포함)
     */
    private Integer byteLength;

    /**
     * 버퍼의 URI(또는 IRI). (선택)
     *
     * @return uri
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * 버퍼의 URI(또는 IRI). (선택)
     *
     * @param uri 설정할 uri
     */
    public void setUri(String uri) {
        if (uri == null) {
            this.uri = uri;
            return;
        }
        this.uri = uri;
    }

    /**
     * 버퍼의 바이트 길이. (필수)<br>
     * 최솟값: 1 (포함)
     *
     * @return byteLength
     */
    public Integer getByteLength() {
        return this.byteLength;
    }

    /**
     * 버퍼의 바이트 길이. (필수)<br>
     * 최솟값: 1 (포함)
     *
     * @param byteLength 설정할 byteLength
     * @throws NullPointerException     주어진 값이 <code>null</code>일 때
     * @throws IllegalArgumentException 주어진 값이 제약 조건을 만족하지 않을 때
     */
    public void setByteLength(Integer byteLength) {
        if (byteLength == null) {
            throw new NullPointerException((("Invalid value for byteLength: " + byteLength) + ", may not be null"));
        }
        if (byteLength < 1) {
            throw new IllegalArgumentException("byteLength < 1");
        }
        this.byteLength = byteLength;
    }
}

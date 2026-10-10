package com.tacz.guns.client.resource.pojo.display;

import net.minecraft.resources.FileToIdConverter;

/**
 * 사실 타협용 인터페이스로, 예전 texture 경로를 새 경로로 바꾸는 데 쓴다<br/>
 * 역직렬화가 끝나면 init 메서드를 호출해 모든 경로를 새 경로로 바꾼다
 */
public interface IDisplay {
    FileToIdConverter converter = new FileToIdConverter("textures", ".png");

    void init();
}

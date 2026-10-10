package com.tacz.guns.client.input;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * 모든 TACZ 키 설정이 함께 쓰는 KeyMapping 분류.
 *
 * <h2>{@code tacz} 네임스페이스를 명시해야 하는 이유</h2>
 * 26.2의 분류 제목은 직접 고정한 문자열이 아니라 <b>Identifier에서 만들어진다</b>:
 * <pre>
 * KeyMapping.Category#label():
 *     return Component.translatable(this.id.toLanguageKey("key.category"));
 * Identifier#toLanguageKey(String prefix):
 *     return prefix + "." + namespace + "." + path;
 * </pre>
 * (두 곳 모두 바이트코드 확인.)
 *
 * <p>원래는 {@code Identifier.parse("tacz")}였는데 — 콜론이 없으면 <b>기본 네임스페이스
 * {@code minecraft}</b>가 붙어 {@code key.category.minecraft.tacz}라는 키가 만들어졌다.
 * 언어 파일에는 이 키가 없어 화면에 원래 키 이름이 그대로 나왔다
 * (사용자 실측: 키 설정 제목이 {@code key.category.mincraft.tacz}로 표시됨).
 *
 * <p>{@code tacz:tacz}로 바꾸면 {@code key.category.tacz.tacz}가 만들어지며,
 * 언어 파일에도 이 키를 넣었다.
 * 바닐라와 대조: {@code Category.register("movement")}는 안에서
 * {@code Identifier.withDefaultNamespace}를 거쳐
 * {@code key.category.minecraft.movement}가 된다 — 같은 규칙이다.
 *
 */
public final class TaCZKeyCategory {
    public static final KeyMapping.Category TACZ =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tacz", "tacz"));

    private TaCZKeyCategory() {
    }
}

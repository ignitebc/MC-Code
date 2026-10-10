package com.tacz.guns.client.render.scope;

/**
 * 조준선(눈금)의 형태 분류.
 *
 * <p>조준경 모델에 실제로 있는 노드로 자동 판정하며, 총기 팩에 필드를 새로 넣을 필요가 없다:</p>
 * <table border="1">
 *   <tr><th>형태</th><th>노드 구성</th><th>실제 원형</th><th>기본 총기 팩 수</th></tr>
 *   <tr><td>{@link #HOLOGRAPHIC}</td><td>{@code *_illuminated}만 있음</td>
 *       <td>EOTech / Aimpoint 도트</td><td>—</td></tr>
 *   <tr><td>{@link #ETCHED}</td><td>{@code division}만 있음(발광 자식 노드 없음)</td>
 *       <td>98k / PU 조준경 새김 선</td><td>2({@code scope_98k}, {@code scope_retro_2x})</td></tr>
 *   <tr><td>{@link #HYBRID}</td><td>둘 다 있음</td>
 *       <td>ACOG TA31(검은 새김 선 + 야간 조명 구간)</td><td>31</td></tr>
 *   <tr><td>{@link #NONE}</td><td>둘 다 없음</td><td>조준경이 아님(레이저/손전등)</td><td>—</td></tr>
 * </table>
 */
public enum ReticleKind {
    /** 순수 발광 조준선: 홀로그램 / 도트. 조준선 전체가 {@code *_illuminated}다. */
    HOLOGRAPHIC,
    /** 순수 새긴 눈금: 빛나지 않고 주변 조명으로 밝아진다. */
    ETCHED,
    /** 혼합: 검은 새김 선 + 발광 구간(현대 고배율 조준경의 주류 형태). */
    HYBRID,
    /** 조준선 없음(조준경이 아닌 부착물이거나 모델에 눈금이 없음). */
    NONE
}

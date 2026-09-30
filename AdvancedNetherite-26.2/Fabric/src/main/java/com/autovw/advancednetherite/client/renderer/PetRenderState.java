package com.autovw.advancednetherite.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * 펫 렌더 상태. 머리 위 체력 막대를 그리려고 현재 체력과 최대 체력을 함께 담는다.
 * 두 값 모두 바닐라가 주변 클라이언트에 동기화해 주는 값이다.
 */
public class PetRenderState extends LivingEntityRenderState
{
    public float health;
    public float maxHealth;
}

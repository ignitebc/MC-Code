package com.autovw.advancednetherite.api.impl;

import net.minecraft.world.item.ItemStack;

/**
 * 다른 아이템 클래스에 Advanced Netherite 훅을 붙여 같은 기능을 재현할 때 쓰는 인터페이스.
 * 이 인터페이스를 구현하면 {@link com.autovw.advancednetherite.common.item} 패키지의 아이템 클래스를 상속하지 않아도 된다.
 * 다만 호환성을 위해 가능하면 Advanced Netherite가 제공하는 아이템 클래스를 쓰는 것이 좋다.
 * 이 인터페이스를 쓰면 직접 구현해야 하며, 아이템 툴팁 같은 기능은 자동으로 들어가지 않는다.
 * @since 1.12.0
 * @author Autovw
 */
public interface IAdvancedHooks
{
    /**
     * 이 아이템을 착용한 플레이어에게 엔더맨이 먼저 공격받기 전까지 우호적으로 행동할지 확인한다.
     * @param stack 플레이어가 착용한 아이템
     * @return true면 이 아이템을 착용한 플레이어에게 엔더맨이 우호적으로 행동한다
     */
    default boolean pacifyEndermen(ItemStack stack)
    {
        return false;
    }

    /**
     * 이 아이템을 착용한 플레이어에게 팬텀이 먼저 공격받기 전까지 우호적으로 행동할지 확인한다.
     * @param stack 플레이어가 착용한 아이템
     * @return true면 이 아이템을 착용한 플레이어에게 팬텀이 우호적으로 행동한다
     */
    default boolean pacifyPhantoms(ItemStack stack)
    {
        return false;
    }
}

package com.autovw.advancednetherite.api.impl;

import net.minecraft.world.item.ToolMaterial;

/**
 * @author Autovw
 */
public interface IToolMaterial
{
    ToolMaterial getMaterial();

    default boolean isMaterial(ToolMaterial material)
    {
        return getMaterial() == material;
    }

    /**
     * @return 도구 종류 {@link IToolMaterial.Type}
     * @since MC 1.21.5
     */
    Type getToolType();

    /**
     * @return 채굴 도구인지 여부. <code>instanceof DiggerItem</code> 검사를 대신한다.
     * @since MC 1.21.5
     */
    default boolean isDiggerItem()
    {
        return getToolType() == Type.AXE || getToolType() == Type.SHOVEL || getToolType() == Type.PICKAXE;
    }

    /**
     * 아이템 태그에 기대지 않고 자바 코드에서 도구 종류를 구분하는 Advanced Netherite의 방식.
     * @since Minecraft 1.21.5
     */
    enum Type
    {
        AXE,
        HOE,
        PICKAXE,
        SHOVEL,
        SWORD,
        /**
         * @since Minecraft 26.2 - Advanced Netherite 2.4.2
         */
        SPEAR;
    }
}

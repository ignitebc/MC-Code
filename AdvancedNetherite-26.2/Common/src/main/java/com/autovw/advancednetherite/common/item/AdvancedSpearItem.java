package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.api.impl.IToolMaterial;
import net.minecraft.world.item.ToolMaterial;

/**
 * This class was added for easy compatibility with Advanced Netherite features.
 * @since Minecraft 26.2 - Advanced Netherite 2.4.2
 * @author Autovw
 */
public class AdvancedSpearItem extends AdvancedItem implements IToolMaterial
{
    private final ToolMaterial material;

    public AdvancedSpearItem(ToolMaterial material, float attackDuration, float damageMultiplier, float delay, float dismountTime, float dismountThreshold, float knockbackTime, float knockbackThreshold, float damageTime, float damageThreshold, Properties properties)
    {
        super(properties.spear(material, attackDuration, damageMultiplier, delay, dismountTime, dismountThreshold, knockbackTime, knockbackThreshold, damageTime, damageThreshold).fireResistant());
        this.material = material;
    }

    @Override
    public ToolMaterial getMaterial()
    {
        return this.material;
    }

    @Override
    public Type getToolType()
    {
        return Type.SPEAR;
    }
}

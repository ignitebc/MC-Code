package com.daqem.jobsplus.achievement;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

import java.util.List;

/** Mixin 클래스의 중첩 타입을 실행 중 참조하지 않도록 스냅샷을 별도 타입으로 둔다. */
public final class ProductionSnapshots
{
    private ProductionSnapshots()
    {
    }

    public record Menu(Container container, List<ItemStack> before, List<ItemStack> references)
    {
    }

    public record Furnace(int inputCount, ItemStack previousOutput)
    {
    }

    public record Brew(BrewingStandBlockEntity stand, List<ItemStack> bottles, int ingredientCount)
    {
    }
}

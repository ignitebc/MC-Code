package com.autovw.advancednetherite.core;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.common.item.BackpackItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModBackpackItems
{
    public static final BackpackItem LEVEL_1 = create(1);
    public static final BackpackItem LEVEL_2 = create(2);
    public static final BackpackItem LEVEL_3 = create(3);

    private ModBackpackItems()
    {
    }

    private static BackpackItem create(int level)
    {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "backpack_level_" + level));
        return new BackpackItem(new Item.Properties().setId(key), level);
    }
}

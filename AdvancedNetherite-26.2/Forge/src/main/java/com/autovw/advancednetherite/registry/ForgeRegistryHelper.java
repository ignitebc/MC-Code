package com.autovw.advancednetherite.registry;

import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.helper.IRegistryHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Autovw
 */
@Internal
public final class ForgeRegistryHelper implements IRegistryHelper
{
    @Override
    public Identifier getBlockById(Block block)
    {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    @Override
    public Identifier getItemById(Item item)
    {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    @Override
    public Identifier getMobEffectById(MobEffect mobEffect)
    {
        return ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
    }

    @Override
    public Identifier getSoundEventById(SoundEvent soundEvent)
    {
        return ForgeRegistries.SOUND_EVENTS.getKey(soundEvent);
    }

    @Override
    public Identifier getEntityTypeById(EntityType<?> entityType)
    {
        return ForgeRegistries.ENTITY_TYPES.getKey(entityType);
    }
}

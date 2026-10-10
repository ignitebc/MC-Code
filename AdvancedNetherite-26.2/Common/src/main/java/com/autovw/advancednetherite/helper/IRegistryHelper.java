package com.autovw.advancednetherite.helper;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @author Autovw
 */
public interface IRegistryHelper
{
    /**
     * @param block ID를 얻을 블록
     * @return 해당 블록의 ID
     * @since 2.0.0
     */
    Identifier getBlockById(Block block);

    /**
     * @param item ID를 얻을 아이템
     * @return 해당 아이템의 ID
     * @since 2.0.0
     */
    Identifier getItemById(Item item);

    /**
     * @param mobEffect ID를 얻을 상태 효과
     * @return 해당 상태 효과의 ID
     * @since 2.0.0
     */
    Identifier getMobEffectById(MobEffect mobEffect);

    /**
     * @param soundEvent ID를 얻을 사운드 이벤트
     * @return 해당 사운드 이벤트의 ID
     * @since 2.0.0
     */
    Identifier getSoundEventById(SoundEvent soundEvent);

    /**
     * @param entityType ID를 얻을 엔티티 종류
     * @return 해당 엔티티 종류의 ID
     * @since 2.0.0
     */
    Identifier getEntityTypeById(EntityType<?> entityType);
}

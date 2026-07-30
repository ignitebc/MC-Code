package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.core.ModEntityTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PetBoxItem extends AdvancedItem
{
    private final double petAttackDamage;

    public PetBoxItem(double petAttackDamage, Properties properties)
    {
        super(properties);
        this.petAttackDamage = petAttackDamage;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel))
        {
            return InteractionResult.FAIL;
        }

        DialgaPetEntity pet = ModEntityTypes.DIALGA_PET.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (pet == null)
        {
            return InteractionResult.FAIL;
        }

        pet.snapTo(player.getX() + 1.0, player.getY(), player.getZ() + 1.0, player.getYRot(), 0.0F);
        pet.tame(player);
        pet.setPersistenceRequired();

        // 상자 등급별 공격력. 속성 기본값은 바닐라가 엔티티 NBT로 저장하므로 재접속해도 유지된다.
        AttributeInstance attackDamage = pet.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null)
        {
            attackDamage.setBaseValue(this.petAttackDamage);
        }

        if (!serverLevel.addFreshEntity(pet))
        {
            return InteractionResult.FAIL;
        }

        ItemStack petBox = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild)
        {
            petBox.shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}

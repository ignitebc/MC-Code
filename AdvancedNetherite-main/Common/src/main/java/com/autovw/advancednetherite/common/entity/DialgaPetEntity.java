package com.autovw.advancednetherite.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public class DialgaPetEntity extends TamableAnimal
{
    public DialgaPetEntity(EntityType<? extends DialgaPetEntity> entityType, Level level)
    {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.25, 3.0F, 1.5F));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel)
    {
        LivingEntity owner = this.getOwner();
        if (owner instanceof ServerPlayer serverPlayer && serverPlayer.level() != serverLevel)
        {
            this.teleport(new TeleportTransition(
                    serverPlayer.level(),
                    serverPlayer.position().add(1.0, 0.0, 1.0),
                    Vec3.ZERO,
                    serverPlayer.getYRot(),
                    0.0F,
                    TeleportTransition.DO_NOTHING));
            return;
        }

        super.customServerAiStep(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack)
    {
        return false;
    }

    @Override
    public DialgaPetEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob partner)
    {
        return null;
    }
}

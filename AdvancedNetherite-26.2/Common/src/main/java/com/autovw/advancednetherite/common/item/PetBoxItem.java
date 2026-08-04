package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.common.pet.PetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class PetBoxItem extends AdvancedItem
{
    private final List<Supplier<EntityType<DialgaPetEntity>>> petTypes;
    private final double petAttackDamage;

    public PetBoxItem(List<Supplier<EntityType<DialgaPetEntity>>> petTypes, double petAttackDamage, Properties properties)
    {
        super(properties);
        this.petTypes = List.copyOf(petTypes);
        this.petAttackDamage = petAttackDamage;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResult.FAIL;
        }

        // 상자에 후보가 여러 종류면 균등 확률로 하나를 뽑는다.
        int pickedIndex = serverLevel.getRandom().nextInt(this.petTypes.size());
        EntityType<DialgaPetEntity> petType = this.petTypes.get(pickedIndex).get();

        // 소유의 원본은 펫 저장소 기록이다. 기록 생성에 실패하면 상자를 소모하지 않는다.
        if (!PetManager.createPet(serverPlayer, petType, this.petAttackDamage))
        {
            serverPlayer.sendSystemMessage(Component.translatable("item.advancednetherite.pet_box.failed"));
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

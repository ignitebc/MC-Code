package com.daqem.arc.data.reward.block;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BlockDropMultiplierReward extends AbstractReward {

    private final int multiplier;

    public BlockDropMultiplierReward(double chance, int priority, int multiplier) {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public Component getDescription() {
        return getDescription(multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        BlockState blockState = actionData.getData(ActionDataType.BLOCK_STATE);
        if (blockState != null) {
            BlockPos blockPos = actionData.getData(ActionDataType.BLOCK_POSITION);
            if (blockPos != null) {
                Level level = actionData.getData(ActionDataType.WORLD);
                if (level == null) {
                    level = actionData.getPlayer().arc$getLevel();
                }
                if (level != null) {
                    if (level instanceof ServerLevel serverLevel) {
                        Vec3 vec3 = Vec3.atCenterOf(blockPos);
                        List<ItemStack> drops = blockState.getDrops(new LootParams.Builder(serverLevel)
                                        .withParameter(LootContextParams.ORIGIN, vec3)
                                        .withParameter(LootContextParams.TOOL, actionData.getPlayer().arc$getPlayer().getMainHandItem())
                                        .withParameter(LootContextParams.BLOCK_STATE, blockState)
                                        .withParameter(LootContextParams.THIS_ENTITY, actionData.getPlayer().arc$getPlayer())
                        );
                        for (ItemStack drop : drops) {
                            for (int i = 1; i < multiplier; i++) {
                                level.addFreshEntity(
                                        new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), drop));
                            }
                        }
                    }
                }
            }
        }
        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.BLOCK_DROP_MULTIPLIER;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<BlockDropMultiplierReward> {

        @Override
        public BlockDropMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new BlockDropMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsInt(jsonObject, "multiplier"));
        }

        @Override
        public BlockDropMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new BlockDropMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, BlockDropMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.multiplier);
        }
    }
}

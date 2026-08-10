package com.daqem.arc.data.reward.block;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.arc.event.triggers.BlockDropTracker;
import com.daqem.arc.player.SkillActivationNotifier;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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
        BlockPos blockPos = actionData.getData(ActionDataType.BLOCK_POSITION);
        if (blockPos != null) {
            Level level = actionData.getData(ActionDataType.WORLD);
            if (level == null) {
                level = actionData.getPlayer().arc$getLevel();
            }
            if (level instanceof ServerLevel serverLevel) {
                // 루트 테이블을 다시 굴리면 실제 획득량과 다른 수량이 될 수 있으므로,
                // 파괴 시점에 실제로 떨어진 아이템을 그대로 복제해 정확히 배수만큼 지급한다.
                List<ItemStack> actualDrops = BlockDropTracker.consume(blockPos, serverLevel.getGameTime());
                Vec3 vec3 = Vec3.atCenterOf(blockPos);
                List<ItemStack> addedDrops = new ArrayList<>();
                for (ItemStack drop : actualDrops) {
                    for (int i = 1; i < multiplier; i++) {
                        ItemStack extraDrop = drop.copy();
                        if (level.addFreshEntity(
                                new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), extraDrop))) {
                            addedDrops.add(extraDrop.copy());
                        }
                    }
                }
                if (!addedDrops.isEmpty()) {
                    SkillActivationNotifier.notifyExtraDrop(actionData, addedDrops);
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

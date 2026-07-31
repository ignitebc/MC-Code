package com.daqem.arc.data.reward.world;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.arc.player.SkillActivationNotifier;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DropItemReward extends AbstractReward {

    private final ItemStackTemplate itemTemplate;

    public DropItemReward(double chance, int priority, ItemStackTemplate itemTemplate) {
        super(chance, priority);
        this.itemTemplate = itemTemplate;
    }

    @Override
    public Component getDescription() {
        ItemStack itemStack = getItemStack();
        return getDescription(itemStack.getCount(), itemStack.getHoverName());
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        BlockPos pos = actionData.getData(ActionDataType.BLOCK_POSITION);
        if (pos != null) {
            Level level = actionData.getData(ActionDataType.WORLD);
            if (level == null) level = actionData.getPlayer().arc$getLevel();
            if (level instanceof ServerLevel serverLevel) {
                ItemStack itemStack = getItemStack();
                if (!itemStack.isEmpty()) {
                    // 설정 수량은 스택의 count에 이미 담겨 있으므로 스택 하나만 드롭한다.
                    // 수량만큼 반복하며 전체 스택을 복사하면 수량의 제곱만큼 지급된다.
                    ItemEntity entity = new ItemEntity(
                            serverLevel,
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),
                            itemStack.copy());
                    entity.setDefaultPickUpDelay();
                    if (serverLevel.addFreshEntity(entity)) {
                        SkillActivationNotifier.notifyExtraDrop(actionData.getPlayer(), itemStack);
                    }
                } else {
                    BlockState state = actionData.getData(ActionDataType.BLOCK_STATE);
                    if (state != null) {
                        List<ItemStack> drops = state.getDrops(
                                new LootParams.Builder(serverLevel)
                                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                                        .withParameter(LootContextParams.TOOL, actionData.getPlayer().arc$getPlayer().getMainHandItem())
                                        .withParameter(LootContextParams.BLOCK_STATE, state)
                                        .withParameter(LootContextParams.THIS_ENTITY, actionData.getPlayer().arc$getPlayer())
                        );
                        List<ItemStack> addedDrops = new java.util.ArrayList<>();
                        for (int i = 0; i < itemStack.getCount() && !drops.isEmpty(); i++) {
                            ItemStack randomDrop = drops.get(serverLevel.getRandom().nextInt(drops.size()));
                            ItemEntity entity = new ItemEntity(
                                    serverLevel,
                                    pos.getX(),
                                    pos.getY(),
                                    pos.getZ(),
                                    randomDrop);
                            entity.setDefaultPickUpDelay();
                            if (serverLevel.addFreshEntity(entity)) {
                                addedDrops.add(randomDrop.copy());
                            }
                        }
                        SkillActivationNotifier.notifyExtraDrop(actionData.getPlayer(), addedDrops);
                    }
                }
            }
        }
        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.DROP_ITEM;
    }

    public ItemStack getItemStack() {
        return itemTemplate.create();
    }

    public static class Serializer implements IRewardSerializer<DropItemReward> {

        @Override
        public DropItemReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new DropItemReward(chance, priority, getItemStackTemplate(jsonObject.get("item")));
        }

        @Override
        public DropItemReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new DropItemReward(
                    chance,
                    priority,
                    ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, DropItemReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, type.itemTemplate);
        }
    }
}

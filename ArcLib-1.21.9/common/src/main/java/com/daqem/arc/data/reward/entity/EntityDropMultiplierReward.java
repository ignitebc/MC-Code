package com.daqem.arc.data.reward.entity;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class EntityDropMultiplierReward extends AbstractReward {
    private final int multiplier;

    public EntityDropMultiplierReward(double chance, int priority, int multiplier) {
        super(chance, priority);
        this.multiplier = Math.max(1, multiplier);
    }

    @Override
    public Component getDescription() {
        return getDescription(multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        Entity entity = actionData.getData(ActionDataType.ENTITY);

        if (!(entity instanceof LivingEntity livingEntity)) {
            return new ActionResult();
        }

        if (multiplier <= 1) {
            return new ActionResult();
        }

        if (!(livingEntity.level() instanceof ServerLevel serverLevel)) {
            return new ActionResult();
        }

        Optional<ResourceKey<LootTable>> lootKeyOpt = livingEntity.getLootTable();

        if (lootKeyOpt.isEmpty()) {
            return new ActionResult();
        }

        ResourceKey<LootTable> lootKey = lootKeyOpt.get();

        LootTable lootTable = serverLevel.getServer()
                .reloadableRegistries()
                .getLootTable(lootKey);

        Vec3 origin = livingEntity.position();

        LootParams.Builder paramsBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.THIS_ENTITY, livingEntity)
                .withOptionalParameter(LootContextParams.DAMAGE_SOURCE, livingEntity.getLastDamageSource())
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, actionData.getPlayer().arc$getPlayer())
                .withParameter(LootContextParams.TOOL, actionData.getPlayer().arc$getPlayer().getMainHandItem());

        LootParams lootParams = paramsBuilder.create(LootContextParamSets.ENTITY);

        List<ItemStack> drops = lootTable.getRandomItems(lootParams);

        for (ItemStack drop : drops) {
            for (int i = 1; i < multiplier; i++) {
                serverLevel.addFreshEntity(
                        new ItemEntity(serverLevel, origin.x(), origin.y(), origin.z(), drop.copy()));
            }
        }

        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.ENTITY_DROP_MULTIPLIER;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<EntityDropMultiplierReward> {
        @Override
        public EntityDropMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new EntityDropMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsInt(jsonObject, "multiplier"));
        }

        @Override
        public EntityDropMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance,
                int priority) {
            return new EntityDropMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EntityDropMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.multiplier);
        }
    }
}

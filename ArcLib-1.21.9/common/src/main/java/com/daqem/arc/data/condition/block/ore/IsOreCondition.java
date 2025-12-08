package com.daqem.arc.data.condition.block.ore;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;

public class IsOreCondition extends AbstractCondition {

    public IsOreCondition(boolean inverted) {
        super(inverted);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        BlockState blockState = actionData.getData(ActionDataType.BLOCK_STATE);
        return blockState != null && isOre(blockState.getBlock());
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.IS_ORE;
    }

    @SuppressWarnings("deprecation")
    public static boolean isOre(Block block) {
        return ((block instanceof DropExperienceBlock && block != Blocks.SCULK)
                || block instanceof RedStoneOreBlock
                || block.defaultBlockState().is(new TagKey<>(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "ores")))
                || block.defaultBlockState().is(new TagKey<>(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores")))
                || block == Blocks.ANCIENT_DEBRIS);
    }

    public static class Serializer implements IConditionSerializer<IsOreCondition> {

        @Override
        public IsOreCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new IsOreCondition(inverted);
        }

        @Override
        public IsOreCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new IsOreCondition(inverted);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, IsOreCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}

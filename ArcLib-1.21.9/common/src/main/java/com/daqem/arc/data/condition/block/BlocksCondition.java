package com.daqem.arc.data.condition.block;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class BlocksCondition extends AbstractCondition {

    private final List<Block> blocks;
    private final List<TagKey<Block>> blockTags;

    public BlocksCondition(boolean inverted, List<Block> blocks, List<TagKey<Block>> blockTags) {
        super(inverted);
        this.blocks = blocks;
        this.blockTags = blockTags;
    }

    @Override
    public Component getDescription() {
        return getDescription(blocks.stream().map(Block::getName).toArray(Component[]::new), blockTags.stream().map(TagKey::location).toArray(ResourceLocation[]::new));
    }

    @Override
    public boolean isMet(ActionData actionData) {
        BlockState blockState = actionData.getData(ActionDataType.BLOCK_STATE);
        return blockState != null
                && (this.blocks.contains(blockState.getBlock())
                || this.blockTags.stream().anyMatch(blockState::is));
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.BLOCKS;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<TagKey<Block>> getBlockTags() {
        return blockTags;
    }

    public List<Block> getAllBlocks(RegistryAccess registryAccess) {
        List<Block> allBlocks = new ArrayList<>(blocks);
        for (TagKey<Block> tag : blockTags) {
            registryAccess.lookupOrThrow(Registries.BLOCK)
                    .get(tag)
                    .ifPresent(x -> allBlocks.addAll(x.stream().map(Holder::value).toList()));
        }
        return allBlocks;
    }

    public static class Serializer implements IConditionSerializer<BlocksCondition> {

        @Override
        public BlocksCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new BlocksCondition(
                    inverted,
                    getBlocks(jsonObject, "blocks"),
                    getBlockTags(jsonObject, "blocks"));
        }

        @Override
        public BlocksCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            int blockCount = friendlyByteBuf.readVarInt();
            int tagCount = friendlyByteBuf.readVarInt();

            List<Block> blocks = new ArrayList<>();
            List<TagKey<Block>> blockTags = new ArrayList<>();

            for (int i = 0; i < blockCount; i++) {
                blocks.add(BuiltInRegistries.BLOCK.byId(friendlyByteBuf.readVarInt()));
            }

            for (int i = 0; i < tagCount; i++) {
                blockTags.add(TagKey.create(BuiltInRegistries.BLOCK.key(), friendlyByteBuf.readResourceLocation()));
            }


            return new BlocksCondition(
                    inverted,
                    blocks,
                    blockTags);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, BlocksCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeVarInt(type.blocks.size());
            friendlyByteBuf.writeVarInt(type.blockTags.size());
            type.blocks.forEach(block -> friendlyByteBuf.writeVarInt(BuiltInRegistries.BLOCK.getId(block)));
            type.blockTags.forEach(tag -> friendlyByteBuf.writeResourceLocation(tag.location()));
        }
    }
}

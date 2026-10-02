package com.mcserver.serverutilities.level;

import com.mcserver.serverutilities.tier.EquipmentTier;
import com.mcserver.serverutilities.tier.EquipmentTierRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 도구 한 개의 누적 EXP를 저장한다. 등급, 강화, 직업 EXP와는 별개다. */
public final class ToolLevelRules {
    public static final int MAX_LEVEL = 100;
    public static final int EXPERIENCE_PER_LEVEL = 100;
    public static final int MAX_EXPERIENCE = (MAX_LEVEL - 1) * EXPERIENCE_PER_LEVEL;

    private static final String EXPERIENCE_KEY = "ToolLevelExperience";
    private static final String APPLIED_LEVEL_KEY = "ToolLevelApplied";
    private static final String APPLIED_ITEM_KEY = "ToolLevelItem";
    private static final int SCAN_INTERVAL_TICKS = 20;

    private ToolLevelRules() { }

    public static boolean isDiggingTool(ItemStack stack) {
        if (stack.is(ItemTags.PICKAXES)) return true;
        if (stack.is(ItemTags.SHOVELS)) return true;
        if (stack.is(ItemTags.AXES)) return true;
        if (stack.is(ItemTags.HOES)) return true;
        return false;
    }

    public static boolean isFishingRod(ItemStack stack) {
        if (stack.getItem() instanceof FishingRodItem) return true;
        return false;
    }

    public static boolean isLevelable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Integer baseMaxDamage = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
        if (baseMaxDamage == null || baseMaxDamage <= 0) return false;
        if (isDiggingTool(stack)) return true;
        if (isFishingRod(stack)) return true;
        return false;
    }

    public static int totalExperience(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        int experience = data.copyTag().getIntOr(EXPERIENCE_KEY, 0);
        return Math.clamp(experience, 0, MAX_EXPERIENCE);
    }

    public static int level(ItemStack stack) {
        return 1 + totalExperience(stack) / EXPERIENCE_PER_LEVEL;
    }

    public static int experienceInLevel(ItemStack stack) {
        if (level(stack) == MAX_LEVEL) return 0;
        return totalExperience(stack) % EXPERIENCE_PER_LEVEL;
    }

    public static double miningSpeedMultiplier(ItemStack stack) {
        if (!isLevelable(stack) || !isDiggingTool(stack)) return 1.0D;
        return 1.0D + (level(stack) - 1) * 0.001D;
    }

    public static double durabilityMultiplier(ItemStack stack) {
        if (!isLevelable(stack)) return 1.0D;
        if (isFishingRod(stack)) {
            return 1.0D + 0.20D * (level(stack) - 1) / (MAX_LEVEL - 1);
        }
        return 1.0D + (level(stack) - 1) * 0.001D;
    }

    /** 서버만 데이터를 생성한다. 새 도구나 재질이 바뀐 도구만 수치를 다시 적용한다. */
    public static void tick(ServerPlayer player) {
        if (player.tickCount % SCAN_INTERVAL_TICKS != 0) return;
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ensureLevel(inventory.getItem(slot));
        }
        ensureLevel(player.getMainHandItem());
        ensureLevel(player.getOffhandItem());
    }

    public static void ensureLevel(ItemStack stack) {
        if (!isLevelable(stack)) return;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int currentLevel = level(stack);
        int experience = totalExperience(stack);
        if (tag.getIntOr(EXPERIENCE_KEY, -1) == experience
                && tag.getIntOr(APPLIED_LEVEL_KEY, 0) == currentLevel
                && itemId.equals(tag.getStringOr(APPLIED_ITEM_KEY, ""))) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            data.putInt(EXPERIENCE_KEY, experience);
            data.putInt(APPLIED_LEVEL_KEY, currentLevel);
            data.putString(APPLIED_ITEM_KEY, itemId);
        });
        EquipmentTierRules.refreshToolStats(stack);
    }

    /** 실제 성공 행동 한 번에만 호출한다. 파손된 도구는 EXP를 남기지 않는다. */
    public static void addExperience(ServerPlayer player, ItemStack stack) {
        if (player.isCreative() || player.isSpectator()) return;
        if (!isLevelable(stack)) return;
        ensureLevel(stack);
        int experience = totalExperience(stack);
        if (experience == MAX_EXPERIENCE) return;
        int previousLevel = level(stack);
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                data -> data.putInt(EXPERIENCE_KEY, experience + 1));
        if (level(stack) != previousLevel) {
            ensureLevel(stack);
            Component message = Component.literal(stack.getHoverName().getString()
                    + " LV " + level(stack)).withStyle(ChatFormatting.AQUA);
            player.sendSystemMessage(message, true);
        }
        player.getInventory().setChanged();
    }

    /** 직접 채굴한 블록의 원래 상태로 판정한다. 범위 채굴과 추가 수확은 호출하지 않는다. */
    public static boolean canGainMiningExperience(ServerPlayer player, ItemStack stack,
                                                  ServerLevel level, BlockPos pos, BlockState state) {
        if (player.isCreative() || player.isSpectator()) return false;
        if (!isLevelable(stack) || !isDiggingTool(stack)) return false;
        if (stack.is(ItemTags.HOES)) {
            return isMatureCrop(state);
        }
        if (state.getDestroySpeed(level, pos) <= 0.0F) return false;
        if (state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)) return false;
        if (ToolPlacedBlockData.get(level).contains(pos)) return false;
        if (stack.is(ItemTags.PICKAXES) && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return true;
        if (stack.is(ItemTags.SHOVELS) && state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return true;
        if (stack.is(ItemTags.AXES) && state.is(BlockTags.MINEABLE_WITH_AXE)) return true;
        return false;
    }

    private static boolean isMatureCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            if (crop.isMaxAge(state)) return true;
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            if (state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE) return true;
        }
        if (state.getBlock() instanceof CocoaBlock) {
            if (state.getValue(CocoaBlock.AGE) == CocoaBlock.MAX_AGE) return true;
        }
        return false;
    }

    /** 수리/업그레이드 결과는 주 재료의 기록만 계승한다. 보조 도구 EXP는 더하지 않는다. */
    public static void inheritPrimaryTool(ItemStack primary, ItemStack result) {
        if (!isLevelable(primary) || !isLevelable(result)) return;
        CompoundTag data = primary.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        // 결과에 추가된 데이터도 보존하되 성장 기록은 반드시 주 재료의 값을 사용한다.
        data.merge(result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        data.putInt(EXPERIENCE_KEY, totalExperience(primary));
        data.remove(APPLIED_LEVEL_KEY);
        data.remove(APPLIED_ITEM_KEY);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        if (primary.getItem() == result.getItem() && primary.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
            result.set(DataComponents.ATTRIBUTE_MODIFIERS, primary.get(DataComponents.ATTRIBUTE_MODIFIERS));
        }
        EquipmentTier tier = EquipmentTierRules.readTier(primary);
        if (tier != null) {
            EquipmentTierRules.setTier(result, tier);
        }
        ensureLevel(result);
    }
}

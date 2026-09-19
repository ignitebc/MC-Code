package com.mcserver.serverutilities.death;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** 유품 상자 블록 등록과 사망 시 설치·회수·만료 처리. */
public final class DeathChests {
    public static final int CAPACITY = 27;
    private static final Identifier ID = Identifier.fromNamespaceAndPath("serverutilities", "death_chest");
    // 기반암과 같은 파괴 시간 -1·폭발 저항이라 채굴·폭발·피스톤이 통하지 않는다. 최종 방어선은 Level.setBlock 차단이다.
    public static final DeathChestBlock BLOCK = new DeathChestBlock(BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, ID))
            .mapColor(MapColor.WOOD).sound(SoundType.WOOD)
            .strength(-1.0F, 3_600_000.0F).noLootTable().pushReaction(PushReaction.BLOCK));
    public static final BlockEntityType<DeathChestBlockEntity> BLOCK_ENTITY =
            new BlockEntityType<>(DeathChestBlockEntity::new, Set.of(BLOCK));

    private static boolean removing;

    private DeathChests() { }

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, ID, BLOCK);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ID, BLOCK_ENTITY);
    }

    /** 이 모듈이 스스로 상자를 거두는 동안에만 블록 교체를 허용한다. */
    public static boolean isRemoving() {
        return removing;
    }

    /**
     * 손실 처리 뒤 남은 소지품을 무작위 순서로 상자에 담는다. 상자에 들어가지 못한 스택은
     * 인벤토리에 남겨 바닐라 사망 드롭이 그대로 바닥에 떨어뜨리게 한다.
     */
    public static void store(ServerPlayer player) {
        var config = ServerUtilities.config();
        if (!config.deathChest()) return;
        ServerLevel level = player.level();
        if (level.getServer().getGameRules().get(GameRules.KEEP_INVENTORY)) return;

        Inventory inventory = player.getInventory();
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            // 소실 저주는 바닐라 드롭 경로에 남겨 그대로 소멸시킨다.
            if (stack.isEmpty() || EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) continue;
            slots.add(slot);
        }
        if (slots.isEmpty()) return;
        Collections.shuffle(slots, new Random(player.getRandom().nextLong()));

        BlockPos pos = findPosition(level, player.blockPosition());
        DeathChestBlockEntity chest = place(level, pos, player.getDirection().getOpposite(), player.getName().getString());
        if (chest == null) {
            ServerUtilities.LOGGER.warn("유품 상자를 {}에 설치하지 못해 소지품을 바닥에 떨어뜨립니다.", pos.toShortString());
            return;
        }
        int stored = 0;
        for (int slot : slots) {
            if (stored >= CAPACITY) break;
            ItemStack stack = inventory.removeItemNoUpdate(slot);
            if (stack.isEmpty()) continue;
            chest.setItem(stored++, stack);
        }
        inventory.setChanged();
        chest.setChanged();

        int overflow = slots.size() - stored;
        player.sendSystemMessage(Component.literal("유품 상자가 ")
                .append(Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.YELLOW))
                .append("에 설치되었습니다. " + describeSeconds(config.deathChestExpireSeconds()) + " 뒤 상자와 내용물이 사라집니다."));
        if (overflow > 0) {
            player.sendSystemMessage(Component.literal("상자에 담지 못한 " + overflow + "개 스택은 바닥에 떨어졌습니다.")
                    .withStyle(ChatFormatting.RED));
        }
    }

    /**
     * 사망 좌표가 기본이다. 막혀 있으면 수평 4방향·위·아래 순으로 교체 가능한 칸을 찾고,
     * 전부 막혀 있으면 최후 수단으로 사망 좌표의 블록을 그대로 덮어쓴다.
     */
    private static BlockPos findPosition(ServerLevel level, BlockPos death) {
        BlockPos base = death.atY(Mth.clamp(death.getY(), level.getMinY(), level.getMaxY()));
        List<BlockPos> candidates = new ArrayList<>();
        candidates.add(base);
        for (Direction direction : Direction.Plane.HORIZONTAL) candidates.add(base.relative(direction));
        candidates.add(base.above());
        candidates.add(base.below());
        for (BlockPos candidate : candidates) {
            if (level.isInsideBuildHeight(candidate) && level.getBlockState(candidate).canBeReplaced()) return candidate;
        }
        // 이미 다른 유품 상자가 있는 칸은 덮어쓸 수 없으므로 위·아래로 물러나며 다른 블록을 고른다.
        for (BlockPos candidate = base; level.isInsideBuildHeight(candidate); candidate = candidate.above()) {
            if (!level.getBlockState(candidate).is(BLOCK)) return candidate;
        }
        for (BlockPos candidate = base.below(); level.isInsideBuildHeight(candidate); candidate = candidate.below()) {
            if (!level.getBlockState(candidate).is(BLOCK)) return candidate;
        }
        return base;
    }

    private static DeathChestBlockEntity place(ServerLevel level, BlockPos pos, Direction facing, String owner) {
        BlockState state = BLOCK.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.TYPE, ChestType.SINGLE)
                .setValue(ChestBlock.WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        if (!level.setBlock(pos, state, Block.UPDATE_ALL)) return null;
        if (!(level.getBlockEntity(pos) instanceof DeathChestBlockEntity chest)) return null;
        chest.initialize(owner);
        return chest;
    }

    /** 내용물을 지우고 보고 있는 플레이어의 창을 닫은 뒤 블록을 거둔다. 물속이면 물을 되돌린다. */
    static void remove(ServerLevel level, BlockPos pos, DeathChestBlockEntity chest) {
        for (int slot = 0; slot < chest.getContainerSize(); slot++) chest.setItem(slot, ItemStack.EMPTY);
        for (ServerPlayer viewer : level.players()) {
            if (viewer.containerMenu instanceof ChestMenu menu && menu.getContainer() == chest) viewer.closeContainer();
        }
        BlockState replacement = level.getBlockState(pos).getFluidState().createLegacyBlock();
        removing = true;
        try {
            level.setBlock(pos, replacement, Block.UPDATE_ALL);
        } finally {
            removing = false;
        }
    }

    private static String describeSeconds(int seconds) {
        return seconds % 60 == 0 ? (seconds / 60) + "분" : seconds + "초";
    }
}

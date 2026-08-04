package com.daqem.itemrestrictions.event;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.chunk.ChunkOwnership;
import com.daqem.itemrestrictions.chunk.ChunkProtection;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.ExplosionEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 청크 소유권 구매와 보호를 담당하는 이벤트 묶음.
 */
public class ChunkProtectionEvents {

    /** 상점에서 파는 땅 구입 문서. 다른 모드의 아이템이라 레지스트리 ID로 찾는다. */
    private static final Identifier LAND_PURCHASE_DOCUMENT_ID = Identifier.fromNamespaceAndPath(
            "advancednetherite",
            "chunk_claim_map");

    public static void registerEvents() {
        LifecycleEvent.SERVER_STARTING.register(ChunkOwnership::load);
        LifecycleEvent.SERVER_STOPPING.register((MinecraftServer server) -> ChunkOwnership.save());
        PlayerEvent.PLAYER_QUIT.register(ChunkProtection::clearNotificationCooldown);
        PlayerEvent.PICKUP_ITEM_PRE.register((Player player, ItemEntity itemEntity, ItemStack itemStack) -> {
            if (ChunkProtection.denyAndNotify(player.level(), itemEntity.blockPosition(), player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        // 땅 구입 문서를 들고 우클릭하면 서 있는 청크를 구매한다.
        InteractionEvent.RIGHT_CLICK_ITEM.register((Player player, InteractionHand hand) -> {
            if (player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
                return EventResult.pass();
            }
            ItemStack itemStack = player.getItemInHand(hand);
            if (!isLandPurchaseDocument(itemStack)) {
                return EventResult.pass();
            }
            return EventResult.fromMinecraft(claimChunk(player, itemStack));
        });

        BlockEvent.BREAK.register((Level level, BlockPos pos, BlockState state,
                                   ServerPlayer player) -> {
            if (ChunkProtection.denyAndNotify(level, pos, player)) {
                return EventResult.interruptFalse();
            }
            BlockPos connectedBlockPos = getConnectedBlockPos(pos, state);
            if (connectedBlockPos != null
                    && ChunkProtection.denyAndNotify(level, connectedBlockPos, player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        BlockEvent.PLACE.register((Level level, BlockPos pos, BlockState state,
                                   Entity placer) -> {
            Player player = null;
            if (placer instanceof Player placingPlayer) {
                player = placingPlayer;
            }
            if (ChunkProtection.denyAndNotify(level, pos, player)) {
                ChunkProtection.resyncInventory(player);
                return EventResult.interruptFalse();
            }
            BlockPos connectedBlockPos = getConnectedBlockPos(pos, state);
            if (connectedBlockPos != null
                    && ChunkProtection.denyAndNotify(level, connectedBlockPos, player)) {
                ChunkProtection.resyncInventory(player);
                return EventResult.interruptFalse();
            }
            // 폭발물은 터진 뒤에 막을 수 없으므로 피해 반경이 남의 땅에 닿는지 미리 재서 설치를 막는다.
            int blastRadius = getBlastRadius(level, pos, state);
            if (blastRadius > 0
                    && ChunkProtection.denyReachAndNotify(level, pos, blastRadius, player, "chunk.blast_too_close")) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        // 상자, 화로, 문, 버튼 등 블록 상호작용 전반을 막는다.
        InteractionEvent.RIGHT_CLICK_BLOCK.register((Player player, InteractionHand hand, BlockPos pos,
                                                    Direction direction) -> {
            Level level = player.level();
            if (ChunkProtection.denyAndNotify(level, pos, player)) {
                ChunkProtection.resyncInventory(player);
                return EventResult.interruptFalse();
            }

            BlockState blockState = level.getBlockState(pos);
            BlockPos connectedBlockPos = getConnectedBlockPos(pos, blockState);
            if (connectedBlockPos != null
                    && ChunkProtection.denyAndNotify(level, connectedBlockPos, player)) {
                ChunkProtection.resyncInventory(player);
                return EventResult.interruptFalse();
            }

            int activationBlastRadius = getBlastRadius(level, pos, blockState);
            if (activationBlastRadius > 0
                    && ChunkProtection.denyReachAndNotify(level,
                    pos,
                    activationBlastRadius,
                    player,
                    "chunk.blast_too_close")) {
                return EventResult.interruptFalse();
            }

            ItemStack itemStack = player.getItemInHand(hand);
            if (isExplosiveEntityItem(itemStack)
                    && ChunkProtection.denyReachAndNotify(level,
                    pos,
                    10,
                    player,
                    "chunk.blast_too_close")) {
                return EventResult.interruptFalse();
            }
            if (isFilledBucket(itemStack)
                    && ChunkProtection.denyReachAndNotify(level,
                    pos,
                    1,
                    player,
                    "chunk.fluid_too_close")) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        // 아이템 액자, 방어구 거치대, 동물 상호작용을 막는다. 전투는 별개라 공격은 막지 않는다.
        InteractionEvent.INTERACT_ENTITY.register((Player player, Entity entity,
                                                   InteractionHand hand) -> {
            if (ChunkProtection.denyAndNotify(player.level(), entity.blockPosition(), player)) {
                ChunkProtection.resyncInventory(player);
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        InteractionEvent.FARMLAND_TRAMPLE.register((Level level, BlockPos pos,
                                                    BlockState state,
                                                    double distance, Entity entity) -> {
            Player player = null;
            if (entity instanceof Player tramplingPlayer) {
                player = tramplingPlayer;
            }
            if (!ChunkProtection.canModify(level, pos, player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        // 액자, 갑옷 거치대, 보트, 광산 수레처럼 공격으로 파괴되는 비전투 엔티티를 보호한다.
        PlayerEvent.ATTACK_ENTITY.register((Player player, Level level, Entity target, InteractionHand hand,
                                            EntityHitResult hitResult) -> {
            if (target instanceof EndCrystal
                    && ChunkProtection.denyReachAndNotify(level,
                    target.blockPosition(),
                    10,
                    player,
                    "chunk.blast_too_close")) {
                return EventResult.interruptFalse();
            }
            if (target instanceof LivingEntity && !(target instanceof ArmorStand)) {
                return EventResult.pass();
            }
            if (ChunkProtection.denyAndNotify(level, target.blockPosition(), player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        // 버킷 채우기는 플랫폼에 따라 블록 우클릭 이벤트와 별도로 호출될 수 있어 함께 검사한다.
        PlayerEvent.FILL_BUCKET.register((Player player, Level level, ItemStack itemStack, HitResult target) -> {
            if (!(target instanceof BlockHitResult blockHitResult)) {
                return EventResult.pass();
            }
            if (ChunkProtection.denyAndNotify(level, blockHitResult.getBlockPos(), player)) {
                ChunkProtection.resyncInventory(player);
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        // 폭발 반경이 소유 청크에 닿으면 폭발 자체를 취소한다.
        // 크리퍼·위더처럼 책임 플레이어를 추적할 수 없는 몹 폭발도 예외 없이 검사해야
        // 몹을 남의 청크로 유인하는 그리핑을 막을 수 있다.
        ExplosionEvent.PRE.register((Level level, Explosion explosion) -> {
            Player responsiblePlayer = ChunkProtection.findResponsiblePlayer(explosion);
            BlockPos origin = BlockPos.containing(explosion.center().x, explosion.center().y, explosion.center().z);
            int radius = Math.max(1, Mth.ceil(explosion.radius() * 1.3F));
            if (ChunkProtection.denyReachAndNotify(level,
                    origin,
                    radius,
                    responsiblePlayer,
                    "chunk.blast_too_close")) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
    }

    /**
     * 플레이어가 직접 놓아 터뜨릴 수 있는 블록의 파괴 반경이다. 몹이 일으키는 폭발은 대상이 아니다.
     */
    private static int getBlastRadius(Level level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.TNT)) {
            return 8;
        }
        if (state.is(Blocks.RESPAWN_ANCHOR)) {
            boolean respawnAnchorWorks = level.environmentAttributes()
                    .getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, pos);
            if (!respawnAnchorWorks) {
                return 10;
            }
        }
        if (state.is(BlockTags.BEDS)) {
            boolean bedExplodes = level.environmentAttributes()
                    .getValue(EnvironmentAttributes.BED_RULE, pos)
                    .explodes();
            if (bedExplodes) {
                return 10;
            }
        }
        return 0;
    }

    /**
     * 청크 안에 아이템 보관 블록이 있는지 확인한다. 특정 블록을 나열하는 대신
     * 인벤토리를 가진 모든 블록 엔티티(상자, 통, 셜커 상자, 호퍼, 화로 등)를 검사한다.
     */
    private static boolean hasContainerBlock(Level level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunk(chunkPos.x(), chunkPos.z());
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof Container) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLandPurchaseDocument(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        return LAND_PURCHASE_DOCUMENT_ID.equals(
                BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
    }

    private static boolean isFilledBucket(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BucketItem)) {
            return false;
        }
        return !itemStack.is(Items.BUCKET);
    }

    private static boolean isExplosiveEntityItem(ItemStack itemStack) {
        return itemStack.is(Items.END_CRYSTAL) || itemStack.is(Items.TNT_MINECART);
    }

    @Nullable
    private static BlockPos getConnectedBlockPos(BlockPos pos, BlockState blockState) {
        if (blockState.getBlock() instanceof ChestBlock && blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            return ChestBlock.getConnectedBlockPos(pos, blockState);
        }
        if (blockState.getBlock() instanceof BedBlock) {
            return pos.relative(BedBlock.getConnectedDirection(blockState));
        }
        return null;
    }

    /**
     * 서 있는 청크를 구매한다. 이미 주인이 있으면 문서를 소모하지 않는다.
     */
    private static InteractionResult claimChunk(Player player, ItemStack itemStack) {
        Level level = player.level();
        ChunkPos chunkPos = ChunkPos.containing(player.blockPosition());
        ChunkOwnership.Owner existing = ChunkOwnership.getOwner(level, chunkPos);

        if (existing != null) {
            boolean mine = existing.uuid().equals(player.getUUID());
            if (mine) {
                player.sendSystemMessage(ItemRestrictions.translatable("chunk.claim.already_own"));
            } else {
                player.sendSystemMessage(ItemRestrictions.translatable("chunk.claim.already_claimed"));
            }
            return InteractionResult.FAIL;
        }

        // 공용 상자가 놓인 청크를 사서 사유화하는 것을 막는다.
        if (hasContainerBlock(level, chunkPos)) {
            player.sendSystemMessage(ItemRestrictions.translatable("chunk.claim.container_present"));
            return InteractionResult.FAIL;
        }

        ChunkOwnership.Owner owner = new ChunkOwnership.Owner(player.getUUID(), player.getName().getString());
        if (!ChunkOwnership.claim(level, chunkPos, owner)) {
            player.sendSystemMessage(ItemRestrictions.translatable("chunk.claim.failed"));
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        player.sendSystemMessage(ItemRestrictions.translatable(
                "chunk.claim.success",
                chunkPos.x(),
                chunkPos.z()));
        return InteractionResult.SUCCESS;
    }
}

package com.tacz.guns.block;

import com.mojang.serialization.MapCodec;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class StatueBlock extends BaseEntityBlock {
    public static final MapCodec<StatueBlock> CODEC = simpleCodec(StatueBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public StatueBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return state.getValue(HALF).equals(DoubleBlockHalf.LOWER) && level.isClientSide() ? createTickerHelper(blockEntityType, ModBlocks.STATUE_BE, StatueBlockEntity::clientTick) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return pState.getValue(HALF) == DoubleBlockHalf.LOWER ? new StatueBlockEntity(pPos, pState) : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState pState, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.below();
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StatueBlockEntity statueBlockEntity) {
                ItemStack handStack = player.getItemInHand(pHand);
                if (handStack.getItem() instanceof IGun) {
                    statueBlockEntity.setGun(handStack);
                    handStack.shrink(1);
                    return InteractionResult.SUCCESS;
                }

                if (handStack.isEmpty()) {
                    statueBlockEntity.dropItem();
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        BlockPos clickedPos = context.getClickedPos();
        BlockPos above = clickedPos.above();
        Level level = context.getLevel();
        if (level.getBlockState(above).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(above)) {
            return this.defaultBlockState().setValue(FACING, direction);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide()) {
            BlockPos above = pos.above();
            world.setBlock(above, state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (facing.getAxis() == Direction.Axis.Y) {
            if (half.equals(DoubleBlockHalf.LOWER) && facing == Direction.UP || half.equals(DoubleBlockHalf.UPPER) && facing == Direction.DOWN) {
                // 절반을 부수면 나머지 절반도 함께 사라진다
                if (!facingState.is(this)) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        return state;
    }

    // 26.2: onRemove는 제거되었고, 블록 엔티티를 제거하면 자동으로 정리된다
    // 아이템을 떨어뜨려야 하면 StatueBlockEntity.setRemoved()에서 처리한다

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /**
     * [39차] 직접 만든 {@code IBlockExtension#tacz$onBlockExploded} + {@code ExplosionMixin}에서
     * <b>26.2 바닐라 공식 확장 지점</b>으로 옮겼다.
     *
     * <h2>예전 mixin을 버려야 했던 이유</h2>
     * 예전 구현은 {@code @Mixin(Explosion.class)}로 {@code finalizeExplosion}에 주입했지만,
     * 26.2에서 {@code net.minecraft.world.level.Explosion}은 <b>인터페이스가 되었다</b>
     * ({@code extends Object}, 필드 0개, 메서드는 모두 {@code level()}/{@code radius()} 같은 접근자):
     * <ul>
     *   <li>{@code finalizeExplosion} — 없다.</li>
     *   <li>{@code @Shadow @Final public Level level} — 인터페이스에는 필드가 없어 shadow할 수 없다.</li>
     * </ul>
     * 실제로 일을 하는 구현 클래스는 새로 생긴 {@code ServerExplosion}이다.
     *
     * <h2>{@code ServerExplosion}에 주입하지 않고 재정의한 이유</h2>
     * {@code ServerExplosion#interactWithBlocks}를 역어셈블하면 블록마다 다음을 호출한다:
     * <pre>
     *   BlockState.onExplosionHit(ServerLevel, BlockPos, Explosion, BiConsumer&lt;ItemStack,BlockPos&gt;)
     * </pre>
     * 그리고 {@code onExplosionHit}는 {@code BlockBehaviour}의 <b>public 재정의 가능 메서드</b>이며,
     * 바닐라 블록 9개({@code DoorBlock}, {@code BellBlock},
     * {@code BeehiveBlock}, {@code AbstractCandleBlock} 등)가 이미 같은 용도로 쓰고 있다.
     * 즉 26.2가 이 확장 지점을 <b>공식으로 제공</b>하므로 mixin을 또 쓰는 것은 군더더기다
     * — mixin이 하나 줄면 버전을 올릴 때 깨질 곳도 하나 준다.
     *
     * <p>동작 동등성: 예전 {@code tacz$onBlockExploded} 기본 구현은
     * "공기로 바꾸기 + {@code wasExploded}"였고, 이것이 바로 부모 기본 구현의 핵심이다
     * ({@code BlockBehaviour#onExplosionHit}는 {@code dropFromExplosion} /
     * {@code hasBlockEntity}에 따라 드롭을 처리한 뒤 블록을 비우고 {@code wasExploded}를 호출한다).
     * 그래서 여기서는 {@code super}만 호출하면 되고 의미는 바뀌지 않는다.</p>
     *
     * <p>조각상의 아이템 드롭은 계속 {@code StatueBlockEntity#setRemoved()}가 맡으며 이 메서드와는 관계없다.</p>
     */
    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
                                  BiConsumer<ItemStack, BlockPos> dropConsumer) {
        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }
}

package com.jship.basicfluidhopper.block;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.fluid.FluidHopper;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class BasicFluidHopperBlock extends BaseEntityBlock {
	public static final MapCodec<BasicFluidHopperBlock> CODEC = BasicFluidHopperBlock
			.simpleCodec(BasicFluidHopperBlock::new);
	public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
	private static final VoxelShape TOP_SHAPE = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape MIDDLE_SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 10.0, 12.0);
	private static final VoxelShape OUTSIDE_SHAPE = Shapes.or(MIDDLE_SHAPE, TOP_SHAPE);
	private static final VoxelShape INSIDE_SHAPE = box(2.0, 11.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape DEFAULT_SHAPE = Shapes.join(OUTSIDE_SHAPE, INSIDE_SHAPE,
			BooleanOp.ONLY_FIRST);
	private static final VoxelShape DOWN_SHAPE = Shapes.or(DEFAULT_SHAPE,
			Block.box(6.0, 0.0, 6.0, 10.0, 4.0, 10.0));
	private static final VoxelShape EAST_SHAPE = Shapes.or(DEFAULT_SHAPE,
			Block.box(12.0, 4.0, 6.0, 16.0, 8.0, 10.0));
	private static final VoxelShape NORTH_SHAPE = Shapes.or(DEFAULT_SHAPE,
			Block.box(6.0, 4.0, 0.0, 10.0, 8.0, 4.0));
	private static final VoxelShape SOUTH_SHAPE = Shapes.or(DEFAULT_SHAPE,
			Block.box(6.0, 4.0, 12.0, 10.0, 8.0, 16.0));
	private static final VoxelShape WEST_SHAPE = Shapes.or(DEFAULT_SHAPE,
			Block.box(0.0, 4.0, 6.0, 4.0, 8.0, 10.0));
	private static final VoxelShape DOWN_RAYCAST_SHAPE = INSIDE_SHAPE;
	private static final VoxelShape EAST_RAYCAST_SHAPE = Shapes.or(INSIDE_SHAPE,
			Block.box(12.0, 8.0, 6.0, 16.0, 10.0, 10.0));
	private static final VoxelShape NORTH_RAYCAST_SHAPE = Shapes.or(INSIDE_SHAPE,
			Block.box(6.0, 8.0, 0.0, 10.0, 10.0, 4.0));
	private static final VoxelShape SOUTH_RAYCAST_SHAPE = Shapes.or(INSIDE_SHAPE,
			Block.box(6.0, 8.0, 12.0, 10.0, 10.0, 16.0));
	private static final VoxelShape WEST_RAYCAST_SHAPE = Shapes.or(INSIDE_SHAPE,
			Block.box(0.0, 8.0, 6.0, 4.0, 10.0, 10.0));

	@Override
	public MapCodec<BasicFluidHopperBlock> codec() {
		return CODEC;
	}

	public BasicFluidHopperBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
				this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.valueOf(true)));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		switch ((Direction) state.getValue(FACING)) {
			case DOWN:
				return DOWN_SHAPE;
			case NORTH:
				return NORTH_SHAPE;
			case SOUTH:
				return SOUTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			case EAST:
				return EAST_SHAPE;
			default:
				return DEFAULT_SHAPE;
		}
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		switch ((Direction) state.getValue(FACING)) {
			case DOWN:
				return DOWN_RAYCAST_SHAPE;
			case NORTH:
				return NORTH_RAYCAST_SHAPE;
			case SOUTH:
				return SOUTH_RAYCAST_SHAPE;
			case WEST:
				return WEST_RAYCAST_SHAPE;
			case EAST:
				return EAST_RAYCAST_SHAPE;
			default:
				return INSIDE_SHAPE;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction direction = ctx.getClickedFace().getOpposite();
		return this.defaultBlockState()
				.setValue(FACING, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction)
				.setValue(ENABLED, Boolean.valueOf(true));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BasicFluidHopperBlockEntity(pos, state);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide ? null
				: createTickerHelper(type, BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(),
						BasicFluidHopperBlockEntity::pushFluidTick);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean notify) {
		if (!oldState.is(state.getBlock())) {
			this.checkPoweredState(level, pos, state);
		}
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) {
			return ItemInteractionResult.SUCCESS;
		} else {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof BasicFluidHopperBlockEntity) {
				boolean success = false;
				if (item.is(Items.BUCKET)) {
					success |= FluidHopper.tryFillBucket(item, level, pos, player, hand,
							((BasicFluidHopperBlockEntity) blockEntity).fluidStorage);
				} else if (item.getItem() instanceof BucketItem) {
					success |= FluidHopper.tryDrainBucket(item, level, pos, player,
							hand, ((BasicFluidHopperBlockEntity) blockEntity).fluidStorage);
				}
				if (success) {
					return ItemInteractionResult.CONSUME;
				}
			}
			return ItemInteractionResult.SUCCESS;
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, BlockPos sourcePos,
			boolean notify) {
		this.checkPoweredState(level, pos, state);
	}

	private void checkPoweredState(Level level, BlockPos pos, BlockState state) {
		boolean bl = !level.hasNeighborSignal(pos);
		if (bl != (Boolean) state.getValue(ENABLED)) {
			level.setBlock(pos, state.setValue(ENABLED, Boolean.valueOf(bl)), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
		Containers.dropContentsOnDestroy(state, newState, level, pos);
		super.onRemove(state, level, pos, newState, moved);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return ((BasicFluidHopperBlockEntity) level.getBlockEntity(pos)).getAnalogOutputSignal();
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ENABLED);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}
}

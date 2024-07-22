package com.jship.basicfluidhopper.fluid;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import org.jetbrains.annotations.Nullable;

import com.jship.basicfluidhopper.BasicFluidHopper;

public abstract class HoneyFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return BasicFluidHopper.FLOWING_HONEY.get();
	}

	@Override
	public Fluid getSource() {
		return BasicFluidHopper.HONEY.get();
	}

	@Override
	public Item getBucket() {
		return BasicFluidHopper.HONEY_BUCKET.get();
	}

	@Override
	public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
		if (!state.isSource() && !(Boolean)state.getValue(FALLING)) {
			if (random.nextInt(64) == 0) {
				level.playLocalSound(
					(double)pos.getX() + 0.5,
					(double)pos.getY() + 0.5,
					(double)pos.getZ() + 0.5,
					SoundEvents.BEEHIVE_DRIP,
					SoundSource.BLOCKS,
					random.nextFloat() * 0.25F + 0.75F,
					random.nextFloat() + 0.5F,
					false
				);
			}
		} else if (random.nextInt(10) == 0) {
			level.addParticle(
				ParticleTypes.DRIPPING_HONEY,
				(double)pos.getX() + random.nextDouble(),
				(double)pos.getY() + random.nextDouble(),
				(double)pos.getZ() + random.nextDouble(),
				0.0,
				0.0,
				0.0
			);
		}
	}

	@Nullable
	@Override
	public ParticleOptions getDripParticle() {
		return ParticleTypes.DRIPPING_HONEY;
	}

	@Override
	protected boolean canConvertToSource(Level level) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState) {
		// from lava
		if (direction == Direction.DOWN) {
			FluidState fluidState2 = level.getFluidState(pos);
			if (this.is(FluidTags.LAVA) && fluidState2.is(FluidTags.WATER)) {
				if (blockState.getBlock() instanceof LiquidBlock) {
					level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
				}

				return;
			}
		}

		super.spreadTo(level, pos, blockState, direction, fluidState);
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
		Block.dropResources(state, level, pos, blockEntity);
	}

	@Override
	public int getSlopeFindDistance(LevelReader level) {
		return 2;
	}

	@Override
	public BlockState createLegacyBlock(FluidState state) {
		return BasicFluidHopper.HONEY_SOURCE_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(state)));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isSame(Fluid fluid) {
		// TODO Use TagKey to say it's the same fluid if it's c/fluid/honey or c/fluid/visual/honey
		return fluid.is(BasicFluidHopper.C_HONEY);// == BasicFluidHopper.HONEY || fluid == BasicFluidHopper.FLOWING_HONEY;
	}

	@Override
	public int getDropOff(LevelReader level) {
		return 2;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 60;
	}

	@Override
	public int getSpreadDelay(Level level, BlockPos pos, FluidState currentState, FluidState newState) {
		int i = this.getTickDelay(level);
		if (!currentState.isEmpty()
			&& !newState.isEmpty()
			&& !(Boolean)currentState.getValue(FALLING)
			&& !(Boolean)newState.getValue(FALLING)
			&& newState.getHeight(level, pos) > currentState.getHeight(level, pos)
			&& level.getRandom().nextInt(4) != 0) {
			i *= 4;
		}

		return i;
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
		//return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
		// lava version: return state.getHeight(level, pos) >= 0.44444445F && fluid.is(FluidTags.WATER);
		// TODO figure out what this method is actually for
		return false;
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BEEHIVE_ENTER);
	}

	public static class Flowing extends HoneyFluid {
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState state) {
			return (Integer)state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}

	public static class Source extends HoneyFluid {
		@Override
		public int getAmount(FluidState state) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}
}

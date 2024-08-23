package com.jship.basicfluidhopper.vehicle;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.fluid.FluidHopper;
import com.jship.basicfluidhopper.fluid.HopperFluidStorage;

import dev.architectury.fluid.FluidStack;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

public class BasicFluidHopperMinecartEntity extends AbstractMinecart implements FluidHopper {
	private boolean enabled = true;
	private int transferCooldown = -1;
	public final HopperFluidStorage fluidStorage;

	public BasicFluidHopperMinecartEntity(EntityType<?> entityType, Level level) {
		super(entityType, level);
		
		this.fluidStorage = HopperFluidStorage.createFluidStorage(BUCKET_CAPACITY * FluidStack.bucketAmount(), FluidStack.bucketAmount(), () -> this.setChanged());
	}

	public BasicFluidHopperMinecartEntity(EntityType<?> type, Level level, double x, double y, double z) {
		super(type, level, x, y, z);

		this.fluidStorage = HopperFluidStorage.createFluidStorage(BUCKET_CAPACITY * FluidStack.bucketAmount(), FluidStack.bucketAmount(), () -> this.setChanged());
	}

	public static BasicFluidHopperMinecartEntity create(
			ServerLevel level, double x, double y, double z) {
		BasicFluidHopperMinecartEntity basicFluidHopperMinecartEntity = new BasicFluidHopperMinecartEntity(
				BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY.get(), level, x, y, z);
		return basicFluidHopperMinecartEntity;
	}

	@Override
	public AbstractMinecart.Type getMinecartType() {
		return AbstractMinecart.Type.HOPPER;
	}

	public HopperFluidStorage getFluidStorage() {
		return this.fluidStorage;
	}

	@Override
	public BlockState getDefaultDisplayBlockState() {
		return BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get().defaultBlockState();
	}

	@Override
	public int getDefaultDisplayOffset() {
		return 1;
	}

	@Override
	public void activateMinecart(int x, int y, int z, boolean powered) {
		boolean bl = !powered;
		if (bl != this.isEnabled()) {
			this.setEnabled(bl);
		}
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@SuppressWarnings("resource")
	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide && this.isAlive() && this.isEnabled() && this.canOperate()) {
			this.setChanged();
		}
	}

	public boolean canOperate() {
		return FluidHopper.drain(this.level(), this.getOnPos().above(2), this);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		ItemStack item = player.getItemInHand(hand);
		if (item.is(Items.BUCKET)) {
			return FluidHopper.tryFillBucket(item, getCommandSenderWorld(), blockPosition(), player, hand,
					fluidStorage) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
		} else if (item.getItem() instanceof BucketItem) {
			return FluidHopper.tryDrainBucket(item, getCommandSenderWorld(), blockPosition(), player, hand,
					fluidStorage) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void setChanged() {
	}

	@Override
	protected Item getDropItem() {
		return BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get();
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get());
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putBoolean("enabled", this.enabled);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		this.enabled = nbt.contains("enabled") ? nbt.getBoolean("enabled") : true;
	}

	@Override
	public void setTransferCooldown(int transferCooldown) {
		this.transferCooldown = transferCooldown;
	}

	@Override
	public boolean needsCooldown() {
		return transferCooldown > 0;
	}

	@Override
	public Direction getFacing() {
		// Only relevant if the fill methods are called, but they shouldn't be for a hopper minecart.
		return Direction.DOWN;
	}
}

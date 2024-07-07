package com.jship.BasicFluidHopper.vehicle;

import com.jship.BasicFluidHopper.BasicFluidHopper;
import com.jship.BasicFluidHopper.block.BasicFluidHopperBlockEntity;
import com.mojang.serialization.DataResult;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BasicFluidHopperMinecartEntity extends AbstractMinecartEntity {
	private boolean enabled = true;
	public static final int TRANSFER_COOLDOWN = 8;
	public static final int BUCKET_CAPACITY = 1;
	public final SingleFluidStorage fluidStorage = SingleFluidStorage
			.withFixedCapacity(BUCKET_CAPACITY * FluidConstants.BUCKET, () -> markDirty());

	public BasicFluidHopperMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	public BasicFluidHopperMinecartEntity(EntityType<?> type, World world, double x, double y, double z) {
		super(type, world, x, y, z);
	}

	public static BasicFluidHopperMinecartEntity create(
			ServerWorld world, double x, double y, double z) {
		BasicFluidHopperMinecartEntity basicFluidHopperMinecartEntity = new BasicFluidHopperMinecartEntity(
				BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY, world, x, y, z);
		return basicFluidHopperMinecartEntity;
	}

	@Override
	public AbstractMinecartEntity.Type getMinecartType() {
		return AbstractMinecartEntity.Type.HOPPER;
	}

	@Override
	public BlockState getDefaultContainedBlock() {
		return BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.getDefaultState();
	}

	@Override
	public int getDefaultBlockOffset() {
		return 1;
	}

	@Override
	public void onActivatorRail(int x, int y, int z, boolean powered) {
		boolean bl = !powered;
		if (bl != this.isEnabled()) {
			this.setEnabled(bl);
		}
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.getWorld().isClient && this.isAlive() && this.isEnabled() && this.canOperate()) {
			this.markDirty();
		}
	}

	public boolean canOperate() {
		return BasicFluidHopperBlockEntity.extract(this.getWorld(), this);
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack item = player.getStackInHand(hand);
		if (item.isOf(Items.BUCKET)) {
			return BasicFluidHopperBlockEntity.tryFillBucket(item, getEntityWorld(), getBlockPos(), player, hand,
					fluidStorage) ? ActionResult.CONSUME : ActionResult.SUCCESS;
		} else if (item.isOf(Items.WATER_BUCKET) || item.isOf(Items.LAVA_BUCKET)) {
			return BasicFluidHopperBlockEntity.tryDrainBucket(item, getEntityWorld(), getBlockPos(), player, hand,
					fluidStorage) ? ActionResult.CONSUME : ActionResult.SUCCESS;
		}
		return ActionResult.SUCCESS;
	}

	public void markDirty() {
	}

	@Override
	protected Item asItem() {
		return BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM;
	}

	@Override
	public ItemStack getPickBlockStack() {
		return new ItemStack(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM);
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("enabled", this.enabled);
		nbt.put("variant", FluidVariant.CODEC.encode(fluidStorage.variant, NbtOps.INSTANCE, nbt)
				.getOrThrow(RuntimeException::new));
		nbt.putLong("amount", fluidStorage.amount);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.enabled = nbt.contains("enabled") ? nbt.getBoolean("enabled") : true;
		DataResult<FluidVariant> result = FluidVariant.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("variant"));
		fluidStorage.variant = result.error().isPresent() ? FluidVariant.blank() : result.result().get();
		fluidStorage.amount = nbt.getLong("amount");
	}
}

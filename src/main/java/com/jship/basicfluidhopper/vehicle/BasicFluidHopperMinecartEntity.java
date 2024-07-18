package com.jship.basicfluidhopper.vehicle;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;
import com.mojang.serialization.DataResult;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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

public class BasicFluidHopperMinecartEntity extends AbstractMinecart {
	private boolean enabled = true;
	public static final int TRANSFER_COOLDOWN = 8;
	public static final int BUCKET_CAPACITY = 1;
	public final SingleFluidStorage fluidStorage = SingleFluidStorage
			.withFixedCapacity(BUCKET_CAPACITY * FluidConstants.BUCKET, () -> setChanged());

	public BasicFluidHopperMinecartEntity(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	public BasicFluidHopperMinecartEntity(EntityType<?> type, Level level, double x, double y, double z) {
		super(type, level, x, y, z);
	}

	public static BasicFluidHopperMinecartEntity create(
			ServerLevel level, double x, double y, double z) {
		BasicFluidHopperMinecartEntity basicFluidHopperMinecartEntity = new BasicFluidHopperMinecartEntity(
				BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY, level, x, y, z);
		return basicFluidHopperMinecartEntity;
	}

	@Override
	public AbstractMinecart.Type getMinecartType() {
		return AbstractMinecart.Type.HOPPER;
	}

	@Override
	public BlockState getDefaultDisplayBlockState() {
		return BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.defaultBlockState();
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
		return BasicFluidHopperBlockEntity.extract(this.level(), this);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		ItemStack item = player.getItemInHand(hand);
		if (item.is(Items.BUCKET)) {
			return BasicFluidHopperBlockEntity.tryFillBucket(item, getCommandSenderWorld(), blockPosition(), player, hand,
					fluidStorage) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
		} else if (item.getItem() instanceof BucketItem) {
			return BasicFluidHopperBlockEntity.tryDrainBucket(item, getCommandSenderWorld(), blockPosition(), player, hand,
					fluidStorage) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	public void setChanged() {
	}

	@Override
	protected Item getDropItem() {
		return BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM;
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putBoolean("enabled", this.enabled);
		nbt.put("variant", FluidVariant.CODEC.encode(fluidStorage.variant, NbtOps.INSTANCE, nbt)
				.getOrThrow(RuntimeException::new));
		nbt.putLong("amount", fluidStorage.amount);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		this.enabled = nbt.contains("enabled") ? nbt.getBoolean("enabled") : true;
		DataResult<FluidVariant> result = FluidVariant.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("variant"));
		fluidStorage.variant = result.error().isPresent() ? FluidVariant.blank() : result.result().get();
		fluidStorage.amount = nbt.getLong("amount");
	}
}

package com.jship.basicfluidhopper.vehicle;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;
import com.jship.basicfluidhopper.fluid.FluidHopper;
import com.jship.spiritapi.api.fluid.SpiritFluidStorage;
import com.jship.spiritapi.api.fluid.SpiritFluidStorageProvider;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BasicFluidHopperMinecartEntity extends AbstractMinecartContainer implements FluidHopper, SpiritFluidStorageProvider {
	private boolean enabled = true;
	private int transferCooldown = -1;
	public final SpiritFluidStorage fluidStorage;

	public static final EntityDataAccessor<CompoundTag> DATA_ID_FLUID = SynchedEntityData.defineId(BasicFluidHopperMinecartEntity.class, EntityDataSerializers.COMPOUND_TAG);

	public BasicFluidHopperMinecartEntity(EntityType<?> entityType, Level level) {
		super(entityType, level);

		this.fluidStorage = SpiritFluidStorage.create(
				BasicFluidHopperConfig.hopperCapacity() * FluidStack.bucketAmount(),
				(long) (FluidStack.bucketAmount() * BasicFluidHopperConfig.transferRate()), () -> this.markDirty());
	}

	public static BasicFluidHopperMinecartEntity create(
			ServerLevel level, double x, double y, double z) {
		BasicFluidHopperMinecartEntity basicFluidHopperMinecartEntity = new BasicFluidHopperMinecartEntity(
				BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY.get(), level);
		return basicFluidHopperMinecartEntity;
	}

	public SpiritFluidStorage getFluidStorage(Direction face) {
		return face == Direction.DOWN ? this.fluidStorage : null;
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
			this.markDirty();
		}
	}

	public boolean canOperate() {
		return FluidHopper.drain(this.level(), this.getOnPos(), Direction.UP, this);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		return FluidHopper.useFluidItem(this.level(), player, hand, (FluidHopper) this);
	}

	public FluidStack getFluidStack() {
		CompoundTag fluidTag = this.getEntityData().get(BasicFluidHopperMinecartEntity.DATA_ID_FLUID);
        if (fluidTag.isEmpty()) 
            return FluidStack.empty();
        return FluidStackHooks.read(this.registryAccess(), fluidTag).orElse(FluidStack.empty());
	}

	@Override
	public void markDirty() {
		CompoundTag nbt = new CompoundTag();
		if (!fluidStorage.getFluidInTank(0).isEmpty())
			nbt.merge((CompoundTag)(FluidStackHooks.write(registryAccess(), fluidStorage.getFluidInTank(0), nbt)));
		this.entityData.set(DATA_ID_FLUID, nbt);
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
		CompoundTag fluidNbt = fluidStorage.serializeNbt(registryAccess());
		nbt.merge(fluidNbt);
		nbt.put("JadeFluidStorage", fluidNbt);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		this.enabled = nbt.contains("enabled") ? nbt.getBoolean("enabled").orElse(true) : true;
		fluidStorage.deserializeNbt(registryAccess(), nbt);
		this.entityData.set(DATA_ID_FLUID, nbt.getCompound("Fluid").orElse(new CompoundTag()));
	}

	@Override
	public void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ID_FLUID, new CompoundTag());
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
		// Only relevant if the fill methods are called, but they shouldn't be for a
		// hopper minecart.
		return Direction.DOWN;
	}

	@Override
	public SpiritFluidStorage getFluidStorage() {
		return this.fluidStorage;
	}

	@Override
	public int getContainerSize() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getContainerSize'");
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'createMenu'");
	}
}

package com.jship.basicfluidhopper.block.entity;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;
import com.jship.basicfluidhopper.fluid.FluidHopper;
import com.jship.basicfluidhopper.fluid.HopperFluidStorage;

import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class BasicFluidHopperBlockEntity extends BlockEntity implements FluidHopper {
    public final HopperFluidStorage fluidStorage;
    private int transferCooldown = -1;

    public BasicFluidHopperBlockEntity(BlockPos pos, BlockState state) {
        super(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(), pos, state);
        fluidStorage = HopperFluidStorage.createFluidStorage(FluidStack.bucketAmount() * BUCKET_CAPACITY, FluidStack.bucketAmount(), () -> this.markDirty());
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        if (nbt.contains("fluid_stack")) {
            CompoundTag fluidStack = nbt.getCompound("fluid_stack");
            Optional<FluidStack> fluid = FluidStack.read(registryLookup, fluidStack);
            if (fluid.isPresent())
                fluidStorage.setFluidStack(fluid.get());
        }
        this.transferCooldown = nbt.getInt("transfer_cooldown");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        Optional<FluidStack> fluid = fluidStorage.getFluidStack();
        if (fluid.isPresent()) {
            nbt.put("fluid_stack", fluid.get().write(registryLookup, new CompoundTag()));
        }
        nbt.putInt("transfer_cooldown", this.transferCooldown);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = super.getUpdateTag(registryLookup);
        Optional<FluidStack> fluid = fluidStorage.getFluidStack();
        if (fluid.isPresent()) {
            nbt.put("fluid_stack", fluid.get().write(registryLookup, new CompoundTag()));
        }
        return nbt;
    }

    // @Override
    // public void load(HolderLookup.Provider registryLookup, CompoundTag nbt) {

    // }

    // @Override
    // public void handleUpdateTag(HolderLookup.Provider registryLookup, CompoundTag nbt) {

    // }

    @Override
    public HopperFluidStorage getFluidStorage() {
        return fluidStorage;
    }

    @Override
    public boolean isEnabled() {
        return this.getBlockState().getValue(BasicFluidHopperBlock.ENABLED).booleanValue();
    }

    @Override
    public Direction getFacing() {
        return this.getBlockState().getValue(BasicFluidHopperBlock.FACING);
    }

    /**
     * Try to drain fluids from above and push fluids into facing storages when not on cooldown.
     * @param level the game world
     * @param pos the hopper's position
     * @param state the hopper's BlockState
     * @param blockEntity the hopper's BlockEntity
     */
    public static void pushFluidTick(Level level, BlockPos pos, BlockState state, BasicFluidHopperBlockEntity blockEntity) {
        if (blockEntity.transferCooldown > 0) {
            --blockEntity.transferCooldown;
        }
        if (!blockEntity.needsCooldown()) {
            FluidHopper.fillAndDrain(level, pos, state, (FluidHopper)blockEntity);
        }
    }

    @Override
    public void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    @Override
    public boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    @Override
    public void markDirty() {
        this.setChanged();
    }

    /**
     * @return 0-15 depending on how full the hopper is
     */
    public int getAnalogOutputSignal() {
        long amount = fluidStorage.getAmount();
        long limit = fluidStorage.getMaxAmount();
        return (int) (15 * amount / limit);
    }
}

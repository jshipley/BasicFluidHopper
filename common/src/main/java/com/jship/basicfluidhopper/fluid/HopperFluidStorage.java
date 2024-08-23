package com.jship.basicfluidhopper.fluid;

import java.util.Optional;

import dev.architectury.fluid.FluidStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;

/**
 * A common fluid storage class for fluid hoppers (and minecarts)
 */
public abstract class HopperFluidStorage {
    /**
     * Create a new fluid storage for @blockEntity
     * @param maxAmount the maximum amount of fluid the storage can hold
     * @param transferRate the maximum amount of fluid that can be drained or filled in one tick
     * @param setChanged called when the contents of the fluid storage have changed
     * @return a new HopperFluidStorageImpl for the appropriate platform
     */
    @ExpectPlatform
    public static HopperFluidStorage createFluidStorage(long maxAmount, long transferRate, Runnable setChanged) {
        throw new AssertionError();
    }

    /**
     * Try to drain fluid from the fluid storage of a block above the hopper
     * @param level the game world
     * @param pos the position of the block to drain
     * @param simulate if true, only simulate the changes without updating anything
     * @return the amount of fluid that was drained (or would be drained)
     */
    public abstract long drainBlockPos(Level level, BlockPos pos, boolean simulate);

    /**
     * Try to drain fluid from the fluid storage of a vehicle above the hopper
     * @param level the game world
     * @param vehicle the vehicle
     * @param simulate if true, only simulate the changes without updating anything
     * @return the amount of fluid that was drained (or would be drained)
     */
    public abstract long drainVehicle(Level level, VehicleEntity vehicle, boolean simulate);
    
    /**
     * Try to fill the fluid storage of a block in the world
     * @param level the game world
     * @param pos the position of the block to fill
     * @param facing the direction the block is in
     * @param simulate if true, only simulate the changes without updating anything
     * @return the amount of fluid that was filled (or would be filled)
     */
    public abstract long fillBlockPos(Level level, BlockPos pos, Direction facing, boolean simulate);

    /**
     * Try to fill the fluid storage of a vehicle
     * @param level the game world
     * @param vehicle the vehicle to fill
     * @param simulate if true, only simulate the changes without updating anything
     * @return the amount of fluid that was filled (or would be filled)
     */
    public abstract long fillVehicle(Level level, VehicleEntity vehicle, boolean simulate);

    /**
     * Get the FluidStack from the fluid storage. Useful for saving the fluid NBT
     * @return a FluidStack, if it's not empty
     */
    public abstract Optional<FluidStack> getFluidStack();

    /**
     * Overwrite the current fluid storage with a Fluid Stack. Useful for loading the fluid NBT
     * @param fluid
     */
    public abstract void setFluidStack(FluidStack fluid);

    /**
     * @return the current fluid amount in the fluid storage
     */
    public abstract long getAmount();
    
    /**
     * Set the fluid amount in the fluid storage
     * @param amount
     */
    public abstract void setAmount(long amount);

    /**
     * @return whether the fluid storage is empty
     */
    public abstract boolean isEmpty();

    /**
     * @return whether the fluid storage is full
     */
    public abstract boolean isFull();

    /**
     * @return the max amount of fluid that can be stored
     */
    public abstract long getMaxAmount();
}

package com.jship.basicfluidhopper.fluid.neoforge;

import com.jship.basicfluidhopper.fluid.HopperFluidStorage;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class HopperFluidStorageImpl extends HopperFluidStorage {

    private final int transferRate;
    private final Runnable markDirty;
    private final FluidTank fluidTank;

    HopperFluidStorageImpl(long maxAmount, long transferRate, Runnable markDirty) {
        this.transferRate = (int) transferRate;
        this.markDirty = markDirty;
        this.fluidTank = new FluidTank((int) maxAmount) {
            @Override
            protected void onContentsChanged() {
                HopperFluidStorageImpl.this.markDirty.run();
            }
        };
    }

    public static HopperFluidStorage createFluidStorage(long capacity, long transferRate, Runnable markDirty) {
        return new HopperFluidStorageImpl(capacity, transferRate, markDirty);
    }

    @Override
    public long drainBlockPos(Level level, BlockPos pos, boolean simulate) {
        if (isFull()) return 0;
        IFluidHandler fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.DOWN);
        if (fluidHandler == null) return 0;
        return drainFluidStorage(fluidHandler, simulate);
    }

    @Override
    public long drainVehicle(Level level, VehicleEntity vehicle, boolean simulate) {
        // given the lack of an fluid api for entities, only care about vehicles from this mod for now.
        if (vehicle instanceof BasicFluidHopperMinecartEntity hopperEntity) {
            return drainFluidStorage(((HopperFluidStorageImpl) hopperEntity.getFluidStorage()).fluidTank, simulate);
        }
        return 0;
    }

    private long drainFluidStorage(IFluidHandler sourceStorage, boolean simulate) {
        return FluidUtil.tryFluidTransfer(fluidTank, sourceStorage, transferRate, simulate).getAmount();
    }

    public boolean drainItem(Player player, InteractionHand hand, boolean simulate) {
        return FluidUtil.tryEmptyContainerAndStow(
            player.getItemInHand(hand),
            fluidTank,
            player.getCapability(Capabilities.ItemHandler.ENTITY, null),
            transferRate,
            player,
            simulate
        ).isSuccess();
    }

    @Override
    public long fillBlockPos(Level level, BlockPos pos, Direction facing, boolean simulate) {
        if (isEmpty()) return 0;
        var sourceStorage = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, facing.getOpposite());
        if (sourceStorage == null) return 0;
        return fillFluidStorage(sourceStorage, simulate);
    }

    @Override
    public long fillVehicle(Level level, VehicleEntity vehicle, boolean simulate) {
        // given the lack of an fluid api for entities, only care about vehicles from this mod for now.
        if (vehicle instanceof BasicFluidHopperMinecartEntity hopperEntity) {
            return fillFluidStorage(((HopperFluidStorageImpl) hopperEntity.getFluidStorage()).fluidTank, simulate);
        }
        return 0;
    }

    public long fillFluidStorage(IFluidHandler destStorage, boolean simulate) {
        return FluidUtil.tryFluidTransfer(fluidTank, destStorage, transferRate, !simulate).getAmount();
    }

    public boolean fillItem(Player player, InteractionHand hand, boolean simulate) {
        return FluidUtil.tryFillContainerAndStow(player.getItemInHand(hand), fluidTank, player.getCapability(Capabilities.ItemHandler.ENTITY), transferRate, player, !simulate).isSuccess();
    }

    public long add(FluidStack fluid, long amount, boolean simulate) {
        var fluidStack = FluidStackHooksForge.toForge(fluid);
        fluidStack.setAmount(Math.min((int)amount, Math.min(transferRate, fluidStack.getAmount())));
        return fluidTank.fill(fluidStack, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
    }

    public long remove(long amount, boolean simulate) {
        return fluidTank.drain(Math.min((int)amount, transferRate), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE).getAmount();
    }

    @Override
    public boolean isEmpty() {
        return fluidTank.isEmpty();
    }

    @Override
    public boolean isFull() {
        return fluidTank.getSpace() <= 0;
    }

    // @Override
    public boolean canExtract(FluidStack fluid) {
        if (isEmpty()) return false;

        FluidStack storageStack = FluidStackHooksForge.fromForge(fluidTank.getFluid());
        return storageStack.isFluidEqual(fluid) && storageStack.isComponentEqual(fluid);
    }

    // @Override
    public long extractFluid(FluidStack fluid, long maxExtract, boolean simulate) {
        var storageStack = FluidStackHooksForge.toForge(fluid);
        storageStack.setAmount((int)Math.min(Math.min(transferRate, maxExtract), fluidTank.getFluidAmount()));

        return fluidTank.drain(storageStack, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE).getAmount();
    }

    // @Override
    public long extractFluid(HopperFluidStorage destStorage, long maxExtract, boolean simulate) {
        return this.extractFluid(FluidStackHooksForge.fromForge(((HopperFluidStorageImpl) destStorage).fluidTank.getFluid()), maxExtract, simulate);
    }

    // @Override
    public boolean canInsert(FluidStack fluid) {
        if (isEmpty()) return true;
        FluidStack storageStack = FluidStackHooksForge.fromForge(fluidTank.getFluid());
        return storageStack.isFluidEqual(fluid) && storageStack.isComponentEqual(fluid);
    }

    // @Override
    public long insertFluid(FluidStack fluid, long maxInsert, boolean simulate) {
        var storageStack = FluidStackHooksForge.toForge(fluid);
        storageStack.setAmount((int)Math.min(Math.min(transferRate, maxInsert), storageStack.getAmount()));

        return fluidTank.fill(storageStack, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
    }

    // @Override
    public long insertFluid(HopperFluidStorage sourceStorage, long maxInsert, boolean simulate) {
        return this.insertFluid(FluidStackHooksForge.fromForge(((HopperFluidStorageImpl) sourceStorage).fluidTank.getFluid()), maxInsert, simulate);
    }

    @Override
    public Optional<FluidStack> getFluidStack() {
        return isEmpty() ? Optional.empty() : Optional.of(FluidStackHooksForge.fromForge(fluidTank.getFluid()));
    }

    @Override
    public void setFluidStack(FluidStack fluid) {
        fluidTank.setFluid(FluidStackHooksForge.toForge(fluid));
    }

    @Override
    public Fluid getFluid() {
        return fluidTank.getFluid().getFluid();
    }

    @Override
    public long getAmount() {
        return fluidTank.getFluidAmount();
    }

    @Override
    public void setAmount(long amount) {
        if (isEmpty()) return;
        var fluid = fluidTank.getFluid();
        fluid.setAmount((int)amount);
        fluidTank.setFluid(fluid);
    }

    @Override
    public long getMaxAmount() {
        return fluidTank.getCapacity();
    }
}

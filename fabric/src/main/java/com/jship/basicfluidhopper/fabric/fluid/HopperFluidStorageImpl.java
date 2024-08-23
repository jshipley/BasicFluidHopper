package com.jship.basicfluidhopper.fabric.fluid;

import java.util.Optional;

import com.jship.basicfluidhopper.fluid.HopperFluidStorage;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HopperFluidStorageImpl extends HopperFluidStorage {
    private final long maxAmount;
    private final long transferRate;
    private final Runnable setChanged;
    private final SingleVariantStorage<FluidVariant> fluidStorage;

    HopperFluidStorageImpl(long maxAmount, long transferRate, Runnable setChanged) {
        this.maxAmount = maxAmount;
        this.transferRate = transferRate;
        this.setChanged = setChanged;
        fluidStorage = new SingleVariantStorage<>() {
            @Override
            protected FluidVariant getBlankVariant() {
                return FluidVariant.blank();
            }

            @Override
            protected long getCapacity(FluidVariant variant) {
                return HopperFluidStorageImpl.this.maxAmount;
            }

            @Override
            protected void onFinalCommit() {
                HopperFluidStorageImpl.this.setChanged.run();
            }
        };
    }

    public static HopperFluidStorage createFluidStorage(long capacity, long transferRate, Runnable setChanged) {
        return new HopperFluidStorageImpl(capacity, transferRate, setChanged);
    }

    @Override
    public long drainBlockPos(Level level, BlockPos pos, boolean simulate) {
        if (isFull()) return 0;
        Storage<FluidVariant> sourceStorage = FluidStorage.SIDED.find(level, pos, Direction.DOWN);
        if (sourceStorage == null) return 0;
        return drainFluidStorage(sourceStorage, simulate);
    }

    @Override
    public long drainVehicle(Level level, VehicleEntity vehicle, boolean simulate) {
        // given the lack of an fluid api for entities, only care about vehicles from this mod for now.
        if (vehicle instanceof BasicFluidHopperMinecartEntity hopperEntity)
            return drainFluidStorage(((HopperFluidStorageImpl)hopperEntity.getFluidStorage()).fluidStorage, simulate);
        return 0;
    }
    
    private long drainFluidStorage(Storage<FluidVariant> sourceStorage, boolean simulate) {        
        long drained = 0;
        try (Transaction tx = Transaction.openOuter()) {
            for (var view : sourceStorage.nonEmptyViews()) {
                try (Transaction nestedTx = tx.openNested()) {
                    FluidVariant resource = fluidStorage.isResourceBlank() ? view.getResource() : fluidStorage.getResource();
                    long maxExtract = Math.min(this.transferRate, fluidStorage.getCapacity() - fluidStorage.getAmount());
                    long extracted = view.extract(resource, maxExtract, nestedTx);
                    long inserted = fluidStorage.insert(resource, extracted, nestedTx);
                    if (extracted == inserted) {
                        drained = extracted;
                        nestedTx.commit();
                        // Only extract from one storage per tick
                        break;
                    }
                }
            }
            if (drained > 0 && !simulate) {
                tx.commit();
            }
        }
        return drained;
    }

    @Override
    public long fillBlockPos(Level level, BlockPos pos, Direction facing, boolean simulate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fillBlockPos'");
    }

    @Override
    public long fillVehicle(Level level, VehicleEntity vehicle, boolean simulate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fillVehicle'");
    }

    public long fillFluidStorage(Storage<FluidVariant> dest, boolean simulate) {
        throw new UnsupportedOperationException("Unimplemented method 'fillFluidStorage'");
    }

    @Override
    public boolean isEmpty() {
        return fluidStorage.isResourceBlank() || fluidStorage.getAmount() == 0;
    }

    @Override
    public boolean isFull() {
        return !fluidStorage.isResourceBlank() && fluidStorage.getAmount() >= this.maxAmount;
    }

    // @Override
    public boolean canExtract(FluidStack fluid) {
        if (fluidStorage.isResourceBlank() || fluidStorage.getAmount() <= 0)
            return false;
        FluidStack storageStack = FluidStackHooksFabric.fromFabric(fluidStorage);
        return storageStack.isFluidEqual(fluid) && storageStack.isComponentEqual(fluid);
    }

    // @Override
    public long extractFluid(FluidStack fluid, long maxExtract, boolean simulate) {
        long extracted = 0;
        try (Transaction tx = Transaction.openOuter()) {
            extracted = fluidStorage.extract(FluidStackHooksFabric.toFabric(fluid), Math.min(maxExtract, this.transferRate), tx);
            if (extracted > 0 && !simulate)
                tx.commit();
        }
        return extracted;
    }

    // @Override
    public long extractFluid(HopperFluidStorage destStorage, long maxExtract, boolean simulate) {
        long extracted = 0;
        try (Transaction tx = Transaction.openOuter()) {
            extracted = this.fluidStorage.extract(((HopperFluidStorageImpl)destStorage).fluidStorage.getResource(), maxExtract, tx);
            if (extracted > 0 && !simulate)
                tx.commit();
        }
        return extracted;
    }

    // @Override
    public boolean canInsert(FluidStack fluid) {
        if (fluidStorage.isResourceBlank())
            return true;
        FluidStack storageStack = FluidStackHooksFabric.fromFabric(fluidStorage);
        return storageStack.isFluidEqual(fluid) && storageStack.isComponentEqual(fluid);
    }
    
    // @Override
    public long insertFluid(FluidStack fluid, long maxInsert, boolean simulate) {
        long inserted = 0;
        try (Transaction tx = Transaction.openOuter()) {
            inserted = fluidStorage.insert(FluidStackHooksFabric.toFabric(fluid), Math.min(maxInsert, this.transferRate), tx);
            if (inserted > 0 && !simulate)
                tx.commit();
        }
        return inserted;
    }

    // @Override
    public long insertFluid(HopperFluidStorage sourceStorage, long maxInsert, boolean simulate) {
        long inserted = 0;
        try (Transaction tx = Transaction.openOuter()) {
            inserted = fluidStorage.insert(((HopperFluidStorageImpl)sourceStorage).fluidStorage.getResource(), Math.min(maxInsert, this.transferRate), tx);
            if (inserted > 0 && !simulate)
                tx.commit();
        }
        return inserted;
    }

    @Override
    public Optional<FluidStack> getFluidStack() {
        return fluidStorage.isResourceBlank() ? Optional.empty() : Optional.of(FluidStackHooksFabric.fromFabric(fluidStorage));
    }

    @Override
    public void setFluidStack(FluidStack fluid) {
        fluidStorage.variant = FluidStackHooksFabric.toFabric(fluid);
        fluidStorage.amount = fluid.getAmount();
        this.setChanged.run();
    }

    @Override
    public long getAmount() {
        return fluidStorage.getAmount();
    }
    
    @Override
    public void setAmount(long amount) {
        fluidStorage.amount = amount;
        this.setChanged.run();
    }

    @Override
    public long getMaxAmount() {
        return fluidStorage.getCapacity();
    }
}    

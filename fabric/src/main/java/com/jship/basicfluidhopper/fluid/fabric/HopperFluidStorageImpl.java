package com.jship.basicfluidhopper.fluid.fabric;

import java.util.Optional;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.fluid.HopperFluidStorage;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public class HopperFluidStorageImpl extends HopperFluidStorage {
    private final long maxAmount;
    private final long transferRate;
    private final Runnable markDirty;
    private final SingleVariantStorage<FluidVariant> fluidStorage;

    HopperFluidStorageImpl(long maxAmount, long transferRate, Runnable markDirty) {
        this.maxAmount = maxAmount;
        this.transferRate = transferRate;
        this.markDirty = markDirty;
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
        try (var tx = Transaction.openOuter()) {
            for (var view : sourceStorage.nonEmptyViews()) {
                try (var nestedTx = tx.openNested()) {
                    FluidVariant resource = fluidStorage.isResourceBlank() ? view.getResource() : fluidStorage.getResource();
                    long maxExtract = Math.min(this.transferRate, fluidStorage.getCapacity() - fluidStorage.getAmount());
                    long extracted = view.extract(resource, maxExtract, nestedTx);
                    long inserted = fluidStorage.insert(resource, extracted, nestedTx);
                    if (extracted == inserted) {
                        drained = extracted;
                        if (!simulate)
                            nestedTx.commit();
                        // Only extract from one storage per tick
                        break;
                    }
                }
            }
            if (drained > 0 && !simulate) tx.commit();
        }
        return drained;
    }

    public long drainItem(Player player, InteractionHand hand, boolean simulate) {
        long drained = 0;
        Storage<FluidVariant> itemStorage = FluidStorage.ITEM.find(player.getItemInHand(hand), ContainerItemContext.forPlayerInteraction(player, hand));
        if (isFull() || itemStorage == null) return drained;

        try (var tx = Transaction.openOuter()) {
            for (var view : itemStorage.nonEmptyViews()) {
                try (var nestedTx = tx.openNested()) {
                    FluidVariant resource = fluidStorage.isResourceBlank() ? view.getResource() : fluidStorage.getResource();
                    long containerAmount = view.getAmount();
                    long maxExtract = Math.min(this.transferRate, view.getCapacity());
                    long hopperSpace = fluidStorage.getCapacity() - fluidStorage.getAmount();
                    maxExtract = Math.min(maxExtract, hopperSpace);
                    long extracted = view.extract(resource, maxExtract, nestedTx);
                    long inserted = fluidStorage.insert(resource, extracted, nestedTx);
                    if (extracted == inserted && extracted > 0 && (extracted == containerAmount || view.getCapacity() > FluidStack.bucketAmount())) {
                        drained = extracted;
                        if (!simulate) {
                            nestedTx.commit();
                            tx.commit();
                        }
                        break;
                    } 
                }
            }
        }
        return drained;
    }

    @Override
    public long fillBlockPos(Level level, BlockPos pos, Direction facing, boolean simulate) {
        if (isEmpty()) return 0;
        Storage<FluidVariant> sourceStorage = FluidStorage.SIDED.find(level, pos, facing.getOpposite());
        if (sourceStorage == null) return 0;
        return fillFluidStorage(sourceStorage, simulate);
    }

    @Override
    public long fillVehicle(Level level, VehicleEntity vehicle, boolean simulate) {
        // given the lack of an fluid api for entities, only care about vehicles from this mod for now.
        if (vehicle instanceof BasicFluidHopperMinecartEntity hopperEntity)
            return fillFluidStorage(((HopperFluidStorageImpl)hopperEntity.getFluidStorage()).fluidStorage, simulate);
        return 0;
    }

    public long fillFluidStorage(Storage<FluidVariant> destStorage, boolean simulate) {
        long filled = 0;
        try (var tx = Transaction.openOuter()) {
            FluidVariant resource = fluidStorage.getResource();
            long extracted = fluidStorage.extract(resource, transferRate, tx);
            long inserted = destStorage.insert(resource, extracted, tx);
            if (inserted == extracted) filled = inserted;
            if (filled > 0 && !simulate) tx.commit();
        }
        return filled;
    }

    public long fillItem(Player player, InteractionHand hand, boolean simulate) {
        long filled = 0;
        Storage<FluidVariant> itemStorage = FluidStorage.ITEM.find(
            player.getItemInHand(hand), player.isCreative() ? ContainerItemContext.forCreativeInteraction(player, player.getItemInHand(hand))
                                                            : ContainerItemContext.forPlayerInteraction(player, hand));
        if (isEmpty() || itemStorage == null) return filled;

        try (var tx = Transaction.openOuter()) {
            FluidVariant resource = fluidStorage.getResource();
            long maxExtract = Math.min(this.transferRate, fluidStorage.getAmount());
            long inserted = itemStorage.insert(resource, maxExtract, tx);
            long extracted = fluidStorage.extract(resource, inserted, tx);
            // may not be good enough if an item has multiple fluid storages with different sizes...
            long itemStorageCapacity = itemStorage.iterator().next().getCapacity();
            if (inserted == extracted && (inserted == itemStorageCapacity || itemStorageCapacity > FluidStack.bucketAmount())) {
                filled = inserted;
                if (!simulate)
                    tx.commit();
            }
        }
        return filled;
    }

    public long add(FluidStack fluid, long amount, boolean simulate) {
        if (isFull()) return 0;
        long added = 0;
        try (var tx = Transaction.openOuter()) {
            long inserted = fluidStorage.insert(FluidStackHooksFabric.toFabric(fluid), amount, tx);
            if (inserted == amount) added = inserted;
            if (added > 0 && !simulate) tx.commit();
        }
        return added;
    }

    public long remove(long amount, boolean simulate) {
        if (isEmpty()) return 0;
        long removed = 0;
        try (var tx = Transaction.openOuter()) {
            long extracted = fluidStorage.extract(fluidStorage.getResource(), amount, tx);
            if (extracted == amount) removed = extracted;
            if (removed > 0 && !simulate) tx.commit();
        }
        return removed;
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
        try (var tx = Transaction.openOuter()) {
            extracted = fluidStorage.extract(FluidStackHooksFabric.toFabric(fluid), Math.min(maxExtract, this.transferRate), tx);
            if (extracted > 0 && !simulate)
                tx.commit();
        }
        return extracted;
    }

    // @Override
    public long extractFluid(HopperFluidStorage destStorage, long maxExtract, boolean simulate) {
        long extracted = 0;
        try (var tx = Transaction.openOuter()) {
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
        try (var tx = Transaction.openOuter()) {
            inserted = fluidStorage.insert(FluidStackHooksFabric.toFabric(fluid), Math.min(maxInsert, this.transferRate), tx);
            if (inserted > 0 && !simulate)
                tx.commit();
        }
        return inserted;
    }

    // @Override
    public long insertFluid(HopperFluidStorage sourceStorage, long maxInsert, boolean simulate) {
        long inserted = 0;
        try (var tx = Transaction.openOuter()) {
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
        this.markDirty.run();
    }

    @Override
    public Fluid getFluid() {
        return fluidStorage.getResource().getFluid();
    }

    @Override
    public long getAmount() {
        return fluidStorage.getAmount();
    }
    
    @Override
    public void setAmount(long amount) {
        fluidStorage.amount = amount;
        this.markDirty.run();
    }

    @Override
    public long getMaxAmount() {
        return fluidStorage.getCapacity();
    }

    public Storage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }
}    

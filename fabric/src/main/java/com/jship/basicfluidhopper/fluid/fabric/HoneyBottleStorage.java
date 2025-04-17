package com.jship.basicfluidhopper.fluid.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class HoneyBottleStorage implements ExtractionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant> {

    private static final FluidVariant CONTAINED_FLUID = FluidVariant.of(BasicFluidHopper.HONEY_FLUID.get());
    private static final long CONTAINED_AMOUNT = FluidConstants.BOTTLE;

    @Nullable
    public static HoneyBottleStorage find(ContainerItemContext context) {
        return isHoneyBottle(context) ? new HoneyBottleStorage(context) : null;
    }

    private boolean isHoneyBottle() {
        return isHoneyBottle(context);
    }

    private static boolean isHoneyBottle(ContainerItemContext context) {
        ItemVariant variant = context.getItemVariant();
        return variant.isOf(Items.HONEY_BOTTLE);
    }

    private final ContainerItemContext context;

    private HoneyBottleStorage(ContainerItemContext context) {
        this.context = context;
    }

    private ItemVariant mapToGlassBottle() {
        ItemStack newStack = context.getItemVariant().toStack();
        return ItemVariant.of(Items.GLASS_BOTTLE, newStack.getComponentsPatch());
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (!isHoneyBottle()) return 0;

        if (resource.equals(CONTAINED_FLUID) && maxAmount >= CONTAINED_AMOUNT) {
            if (context.exchange(mapToGlassBottle(), 1, transaction) == 1) {
                return CONTAINED_AMOUNT;
            }
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        // Only contains a resource if this is a honey bottle.
        if (isHoneyBottle()) {
            return CONTAINED_FLUID;
        } else {
            return FluidVariant.blank();
        }
    }

    @Override
    public long getAmount() {
        if (isHoneyBottle()) {
            return CONTAINED_AMOUNT;
        } else {
            return 0;
        }
    }

    @Override
    public long getCapacity() {
        // Capacity is the same as the amount.
        return getAmount();
    }

    @Override
    public String toString() {
        return "HoneyBottleStorage[" + context + "]";
    }
}

package com.jship.basicfluidhopper.util.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.util.FluidHopperUtil;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public class FluidHopperUtilImpl extends FluidHopperUtil {

    public static boolean isFluidItem(ItemStack container) {
        return FluidStorage.ITEM.find(container, ContainerItemContext.withConstant(container)) != null;
    }

    public static long getFluidItemCapacity(ItemStack container) {
        long capacity = 0;
        for (var view : FluidStorage.ITEM.find(container, ContainerItemContext.withConstant(container))) {
            if (view.getCapacity() > capacity) capacity = view.getCapacity();
        }
        return capacity;
    }

    public static FluidStack getFluidFromItem(ItemStack filledContainer) {
        var storage = FluidStorage.ITEM.find(filledContainer, ContainerItemContext.withConstant(filledContainer));
        if (storage != null) {
        
            for (var view : storage) {
                var fluidStack = FluidStack.create(view.getResource().getFluid(), view.getAmount());
                fluidStack.applyComponents(view.getResource().getComponents());
                return fluidStack;
            }
        }
        return FluidStack.empty();
    }

    public static ItemStack getItemFromFluid(FluidStack fluid, ItemStack container) {
        var storage = FluidStorage.ITEM.find(container, ContainerItemContext.withConstant(container));
        if (storage != null) {
            try (var tx = Transaction.openOuter()) {
                storage.insert(FluidStackHooksFabric.toFabric(fluid), fluid.getAmount(), tx);
                tx.commit();
                return container;
            }
        }
        return ItemStack.EMPTY;
    }
    
}

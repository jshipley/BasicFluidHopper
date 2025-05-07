package com.jship.basicfluidhopper.fluid.neoforge;

import com.jship.basicfluidhopper.BasicFluidHopper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class BottleFluidHandler implements IFluidHandlerItem {

    protected ItemStack container;
    protected final ItemStack emptyContainer = new ItemStack(Items.GLASS_BOTTLE);
    protected final ItemStack waterContainer = PotionContents.createItemStack(Items.POTION, Potions.WATER);
    protected final ItemStack honeyContainer = new ItemStack(Items.HONEY_BOTTLE);
    // protected final 
    protected final int capacity = 250;

    public BottleFluidHandler(ItemStack container) {
        this.container = container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (container.is(Items.POTION) && container.get(DataComponents.POTION_CONTENTS).is(Potions.WATER)) {
            return new FluidStack(Fluids.WATER, capacity);
        } else if (container.is(Items.HONEY_BOTTLE)) {
            return new FluidStack(BasicFluidHopper.HONEY_FLUID.get(), capacity);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return stack.getAmount() >= capacity && (stack.is(NeoForgeMod.WATER_TYPE.value()) || stack.is(Tags.Fluids.HONEY));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || !isFluidValid(0, resource) || !container.is(Items.GLASS_BOTTLE)) {
            return 0;
        }

        if (action.execute()) {
            if (resource.is(NeoForgeMod.WATER_TYPE.value())) {
                container = waterContainer.copy();
            } else if (resource.is(Tags.Fluids.HONEY)) {
                container = honeyContainer.copy();
            } else {
                // shouldn't happen, because we just checked the fluid validity
                return 0;
            }
        }

        return capacity;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || container.is(Items.GLASS_BOTTLE)) {
            return FluidStack.EMPTY;
        }

        if (getFluidInTank(0).is(NeoForgeMod.WATER_TYPE.value()) && resource.is(NeoForgeMod.WATER_TYPE.value())) {
            return drain(capacity, action);
        } else if (getFluidInTank(0).is(Tags.Fluids.HONEY) && resource.is(Tags.Fluids.HONEY)) {
            return drain(capacity, action);
        }

        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || container.is(Items.GLASS_BOTTLE) || maxDrain < capacity) {
            return FluidStack.EMPTY;
        }

        FluidStack drained;

        if (getFluidInTank(0).is(NeoForgeMod.WATER_TYPE.value())) {
            drained = new FluidStack(Fluids.WATER, capacity);
        } else if (getFluidInTank(0).is(Tags.Fluids.HONEY)) {
            drained = new FluidStack(BasicFluidHopper.HONEY_FLUID.get(), capacity);
        } else {
            drained = FluidStack.EMPTY;
        }

        if (!drained.isEmpty() && action.execute()) {
            container = emptyContainer.copy();
        }

        return drained;        
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }
}

package com.jship.basicfluidhopper.block.fabric;

import com.jship.basicfluidhopper.fabric.BasicFluidHopperFabric;

import net.minecraft.world.level.material.Fluid;

public class BasicFluidHopperBlockImpl {
    public static Fluid getMilk() {
        return BasicFluidHopperFabric.MILK_FLUID.value();
    }
}

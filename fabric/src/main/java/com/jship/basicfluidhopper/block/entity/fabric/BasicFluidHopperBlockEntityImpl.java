package com.jship.basicfluidhopper.block.entity.fabric;

import com.jship.basicfluidhopper.fabric.BasicFluidHopperFabric;

import net.minecraft.world.level.material.Fluid;

public class BasicFluidHopperBlockEntityImpl {
    public static Fluid getMilk() {
        return BasicFluidHopperFabric.MILK_FLUID.value();
    }
}

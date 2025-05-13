package com.jship.basicfluidhopper.block.neoforge;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;

public class BasicFluidHopperBlockImpl {
    public static Fluid getMilk() {
        return NeoForgeMod.MILK.value();
    }    
}

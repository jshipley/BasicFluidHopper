package com.jship.basicfluidhopper.block.entity.neoforge;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;

public class BasicFluidHopperBlockEntityImpl {
    public static Fluid getMilk() {
        return NeoForgeMod.MILK.value();
    }    
}

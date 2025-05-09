package com.jship.basicfluidhopper.vehicle;

import org.jetbrains.annotations.Nullable;

import com.jship.basicfluidhopper.BasicFluidHopper;

import dev.architectury.fluid.FluidStack;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;

public class BasicFluidHopperMinecartRenderState extends MinecartRenderState {
    @Nullable FluidStack fluidContents;
    long fluidCapacity;
    
    public BasicFluidHopperMinecartRenderState() {
        this.displayBlockState = BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get().defaultBlockState();
    }
}

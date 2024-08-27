package com.jship.basicfluidhopper.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.fluid.fabric.HopperFluidStorageImpl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;

public class BasicFluidHopperFabric implements ModInitializer {
        @Override
        public void onInitialize() {
                BasicFluidHopper.init();

                FluidStorage.SIDED.registerForBlockEntity((tank, direction) -> ((HopperFluidStorageImpl)tank.fluidStorage).getFluidStorage(), BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get());
                // FluidStorage.ITEM.registerForItems(ItemApiProvider<Storage<FluidVariant>>, );
        }
}

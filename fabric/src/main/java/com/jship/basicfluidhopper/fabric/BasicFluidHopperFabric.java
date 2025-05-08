package com.jship.basicfluidhopper.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.fluid.fabric.HoneyBottleStorage;
import com.jship.spiritapi.api.fluid.fabric.SpiritFluidStorageImpl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

public class BasicFluidHopperFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        BasicFluidHopper.init();

        FluidStorage.SIDED.registerForBlockEntity(
            (tank, direction) -> ((SpiritFluidStorageImpl)tank.getFluidStorage()).fabricFluidStorage,
            BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get()
        );
        FluidStorage.combinedItemApiProvider(Items.HONEY_BOTTLE).register(HoneyBottleStorage::find);
        FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(context -> {
            return new EmptyItemFluidStorage(
                context,
                emptyBottle -> {
                    return ItemVariant.of(Items.HONEY_BOTTLE);
                },
                BasicFluidHopper.HONEY_FLUID.get(),
                FluidConstants.BOTTLE
            );
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
                content.addAfter(Items.HOPPER, BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
                content.addAfter(Items.HOPPER_MINECART, BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get());
                content.addAfter(Items.LAVA_BUCKET, BasicFluidHopper.HONEY_BUCKET.get());
        });
    }
}

package com.jship.basicfluidhopper;

import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartItem;

import earth.terrarium.common_storage_lib.data.DataManager;
import earth.terrarium.common_storage_lib.data.DataManagerRegistry;
import earth.terrarium.common_storage_lib.fluid.util.FluidStorageData;
import earth.terrarium.common_storage_lib.resources.fluid.util.FluidAmounts;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicFluidHopper {
        public static final String MOD_ID = "basic_fluid_hopper";
        public static final Logger LOGGER = LoggerFactory.getLogger("Basic Fluid Hopper");

        // CONSTANTS - could be configurable //
        public static final long FUEL_CONSUME_STEP = FluidAmounts.NUGGET / 5;

        public static final DataManagerRegistry REGISTRY = new DataManagerRegistry(MOD_ID);
        public static final DataManager<FluidStorageData> FLUID_CONTENTS = REGISTRY.builder(FluidStorageData.DEFAULT)
                        .serialize(FluidStorageData.CODEC)
                        .networkSerializer(FluidStorageData.NETWORK_CODEC)
                        .withDataComponent().copyOnDeath().buildAndRegister("fluids");

        public static Supplier<Block> BASIC_FLUID_HOPPER_BLOCK;
        public static Supplier<Item> BASIC_FLUID_HOPPER_ITEM;
        public static Supplier<BlockEntityType<BasicFluidHopperBlockEntity>> BASIC_FLUID_HOPPER_BLOCK_ENTITY;
        public static Supplier<EntityType<BasicFluidHopperMinecartEntity>> BASIC_FLUID_HOPPER_MINECART_ENTITY;
        public static Supplier<BasicFluidHopperMinecartItem> BASIC_FLUID_HOPPER_MINECART_ITEM;

        public static Supplier<FlowingFluid> HONEY;
        public static Supplier<FlowingFluid> FLOWING_HONEY;
        public static Supplier<Block> HONEY_SOURCE_BLOCK;
        public static Supplier<Item> HONEY_BUCKET;

        public static final TagKey<Fluid> C_HONEY = TagKey.create(Registries.FLUID,
                        ResourceLocation.fromNamespaceAndPath("c", "honey"));
        // Any fluid in tags/c/fluid/fuel will be usable in a furnace and will have the
        // same burn duration as lava
        public static final TagKey<Fluid> C_FLUID_FUEL = TagKey.create(Registries.FLUID,
                        ResourceLocation.fromNamespaceAndPath("c", "fuel"));
        
        public static void init() {
                REGISTRY.init();
        }
}

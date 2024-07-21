package com.jship.basicfluidhopper;

import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.fluid.HoneyFluid;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartItem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicFluidHopper implements ModInitializer {
        public static final String MOD_ID = "basic_fluid_hopper";

        // Don't use FluidConstants.DROPLET. it breaks blast furnaces and smokers (1/2
        // == 0)
        // 200 seems about right, it should be enough to smelt/cook a single item for
        // most recipes
        public static final int FUEL_CONSUME_STEP = 200;

        public static final Logger LOGGER;
        public static final Block BASIC_FLUID_HOPPER_BLOCK;
        public static final Item BASIC_FLUID_HOPPER_ITEM;
        public static final BlockEntityType<BasicFluidHopperBlockEntity> BASIC_FLUID_HOPPER_BLOCK_ENTITY;
        public static final EntityType<BasicFluidHopperMinecartEntity> BASIC_FLUID_HOPPER_MINECART_ENTITY;
        public static final BasicFluidHopperMinecartItem BASIC_FLUID_HOPPER_MINECART_ITEM;

        public static final FlowingFluid HONEY;
        public static final FlowingFluid FLOWING_HONEY;
        public static final Block HONEY_SOURCE_BLOCK;
        public static final Item HONEY_BUCKET;
        public static final TagKey<Fluid> C_HONEY;
        public static final TagKey<Fluid> C_FLUID_FUEL;

        static {
                LOGGER = LoggerFactory.getLogger("Basic Fluid Hopper");

                BASIC_FLUID_HOPPER_BLOCK = Registry.register(
                                Registry.BLOCK,
                                new ResourceLocation(MOD_ID, "basic_fluid_hopper"),
                                new BasicFluidHopperBlock(FabricBlockSettings.copyOf(Blocks.HOPPER)));
                BASIC_FLUID_HOPPER_ITEM = Registry.register(
                                Registry.ITEM,
                                new ResourceLocation(MOD_ID, "basic_fluid_hopper"),
                                new BlockItem(BASIC_FLUID_HOPPER_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_DECORATIONS)));
                BASIC_FLUID_HOPPER_BLOCK_ENTITY = Registry.register(
                                Registry.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(MOD_ID, "basic_fluid_hopper"),
                                FabricBlockEntityTypeBuilder
                                                .create(BasicFluidHopperBlockEntity::new, BASIC_FLUID_HOPPER_BLOCK)
                                                .build(null));
                FluidStorage.SIDED.registerForBlockEntity((tank, direction) -> tank.fluidStorage,
                                BASIC_FLUID_HOPPER_BLOCK_ENTITY);

                BASIC_FLUID_HOPPER_MINECART_ITEM = Registry.register(
                                Registry.ITEM,
                                new ResourceLocation(MOD_ID, "basic_fluid_hopper_minecart"),
                                new BasicFluidHopperMinecartItem(new FabricItemSettings().stacksTo(1)));
                BASIC_FLUID_HOPPER_MINECART_ENTITY = Registry.register(
                                Registry.ENTITY_TYPE,
                                new ResourceLocation(MOD_ID, "basic_fluid_hopper_minecart"),
                                FabricEntityTypeBuilder
                                                .<BasicFluidHopperMinecartEntity>create(MobCategory.MISC,
                                                                BasicFluidHopperMinecartEntity::new)
                                                .dimensions(EntityDimensions.fixed(0.98F, 0.7F)).build());
                // EntityApiLookup.<Storage<FluidVariant>, Direction>register
                // FluidStorage.SIDED.registerForBlockEntity((tank, direction) ->
                // tank.fluidStorage, BASIC_FLUID_HOPPER_BLOCK_ENTITY);

                HONEY = Registry.register(Registry.FLUID, new ResourceLocation(MOD_ID, "honey"),
                                new HoneyFluid.Source());
                FLOWING_HONEY = Registry.register(Registry.FLUID,
                                new ResourceLocation(MOD_ID, "flowing_honey"), new HoneyFluid.Flowing());
                HONEY_SOURCE_BLOCK = Registry.register(Registry.BLOCK, new ResourceLocation(MOD_ID, "honey"),
                                new LiquidBlock(HONEY, FabricBlockSettings.copyOf(Blocks.WATER)
                                                .speedFactor(0.2F)
                                                .jumpFactor(0.3F)
                                                .sound(SoundType.HONEY_BLOCK)));
                HONEY_BUCKET = Registry.register(Registry.ITEM, new ResourceLocation(MOD_ID, "honey_bucket"),
                                new BucketItem(HONEY, new FabricItemSettings().craftRemainder(Items.BUCKET).stacksTo(1)));
                C_HONEY = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation("c", "honey"));

                // Any fluid in tags/c/fluid/fuel will be usable in a furnace and will have the
                // same burn duration as lava
                C_FLUID_FUEL = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation("c", "fuel"));
        }

        @Override
        public void onInitialize() {
        }
}

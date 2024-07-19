package com.jship.basicfluidhopper;

import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.fluid.HoneyFluid;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartItem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTabs;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicFluidHopper implements ModInitializer {
        public static final String MOD_ID = "basic_fluid_hopper";

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
                BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "basic_fluid_hopper"),
                new BasicFluidHopperBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER)));
        BASIC_FLUID_HOPPER_ITEM = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "basic_fluid_hopper"),
                new BlockItem(BASIC_FLUID_HOPPER_BLOCK, new Item.Properties()));
        BASIC_FLUID_HOPPER_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "basic_fluid_hopper"),
                BlockEntityType.Builder.of(BasicFluidHopperBlockEntity::new, BASIC_FLUID_HOPPER_BLOCK).build(null));
        FluidStorage.SIDED.registerForBlockEntity((tank, direction) -> tank.fluidStorage,
                BASIC_FLUID_HOPPER_BLOCK_ENTITY);

        BASIC_FLUID_HOPPER_MINECART_ITEM = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "basic_fluid_hopper_minecart"),
                new BasicFluidHopperMinecartItem(new Item.Properties().stacksTo(1)));
        BASIC_FLUID_HOPPER_MINECART_ENTITY = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "basic_fluid_hopper_minecart"),
                EntityType.Builder
                        .<BasicFluidHopperMinecartEntity>of(BasicFluidHopperMinecartEntity::new, MobCategory.MISC)
                        .sized(0.98F, 0.7F).build());
        // EntityApiLookup.<Storage<FluidVariant>, Direction>register
        // FluidStorage.SIDED.registerForBlockEntity((tank, direction) ->
        // tank.fluidStorage, BASIC_FLUID_HOPPER_BLOCK_ENTITY);

        HONEY = Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "honey"), new HoneyFluid.Source());
        FLOWING_HONEY = Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(MOD_ID, "flowing_honey"), new HoneyFluid.Flowing());
        HONEY_SOURCE_BLOCK = Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "honey"),
                new LiquidBlock(HONEY, BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW)
				.replaceable()
				.noCollission()
				.strength(100.0F)
				.pushReaction(PushReaction.DESTROY)
                                .speedFactor(0.2F)
                                .jumpFactor(0.3F)
				.noLootTable()
				.liquid()
				.sound(SoundType.HONEY_BLOCK)));
        HONEY_BUCKET = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "honey_bucket"),
                new BucketItem(HONEY, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
        C_HONEY = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "honey"));

        // Any fluid in tags/c/fluid/fuel will be usable in a furnace and will have the same burn duration as lava
        C_FLUID_FUEL = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "fuel"));
    }

        @Override
        public void onInitialize() {
                ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
                        content.addAfter(Items.HOPPER, BASIC_FLUID_HOPPER_BLOCK);
                        content.addAfter(Items.HOPPER_MINECART, BASIC_FLUID_HOPPER_MINECART_ITEM);
                        content.addAfter(Items.LAVA_BUCKET, HONEY_BUCKET);
                });
        }
}

package com.jship.basicfluidhopper.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartItem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class BasicFluidHopperFabric implements ModInitializer {
        @Override
        public void onInitialize() {
                BasicFluidHopper.init();
                
                // BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK = () -> Registry.register(
                //                 BuiltInRegistries.BLOCK,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                //                 new BasicFluidHopperBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER)));
                // BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM = () -> Registry.register(
                //                 BuiltInRegistries.ITEM,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                //                 new BlockItem(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get(), new Item.Properties()));
                // BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY = () -> Registry.register(
                //                 BuiltInRegistries.BLOCK_ENTITY_TYPE,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                //                 BlockEntityType.Builder
                //                                 .of(BasicFluidHopperBlockEntity::new,
                //                                                 BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get())
                //                                 .build(null));
                // // FluidStorage.SIDED.registerForBlockEntity((tank, direction) ->
                // // tank.fluidStorage,
                // // BASIC_FLUID_HOPPER_BLOCK_ENTITY);

                // BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM = () -> Registry.register(
                //                 BuiltInRegistries.ITEM,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID,
                //                                 "basic_fluid_hopper_minecart"),
                //                 new BasicFluidHopperMinecartItem(new Item.Properties().stacksTo(1)));
                // BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY = () -> Registry.register(
                //                 BuiltInRegistries.ENTITY_TYPE,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID,
                //                                 "basic_fluid_hopper_minecart"),
                //                 EntityType.Builder
                //                                 .<BasicFluidHopperMinecartEntity>of(BasicFluidHopperMinecartEntity::new,
                //                                                 MobCategory.MISC)
                //                                 .sized(0.98F, 0.7F).build());

                // BasicFluidHopper.HONEY = () -> Registry.register(BuiltInRegistries.FLUID,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey"),
                //                 new HoneyFluid.Source());
                // BasicFluidHopper.FLOWING_HONEY = () -> Registry.register(BuiltInRegistries.FLUID,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "flowing_honey"),
                //                 new HoneyFluid.Flowing());
                // BasicFluidHopper.HONEY_SOURCE_BLOCK = () -> Registry.register(BuiltInRegistries.BLOCK,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey"),
                //                 new LiquidBlock(BasicFluidHopper.HONEY.get(), BlockBehaviour.Properties.of()
                //                                 .mapColor(MapColor.COLOR_YELLOW)
                //                                 .replaceable()
                //                                 .noCollission()
                //                                 .strength(100.0F)
                //                                 .pushReaction(PushReaction.DESTROY)
                //                                 .speedFactor(0.2F)
                //                                 .jumpFactor(0.3F)
                //                                 .noLootTable()
                //                                 .liquid()
                //                                 .sound(SoundType.HONEY_BLOCK)));
                // BasicFluidHopper.HONEY_BUCKET = () -> Registry.register(BuiltInRegistries.ITEM,
                //                 ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey_bucket"),
                //                 new BucketItem(BasicFluidHopper.HONEY.get(),
                //                                 new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

                // ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
                //         content.addAfter(Items.HOPPER, BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
                //         content.addAfter(Items.HOPPER_MINECART,
                //                         BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get());
                //         content.addAfter(Items.LAVA_BUCKET, BasicFluidHopper.HONEY_BUCKET.get());
                // });
        }
}

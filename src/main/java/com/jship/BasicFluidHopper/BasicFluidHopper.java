package com.jship.BasicFluidHopper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jship.BasicFluidHopper.block.BasicFluidHopperBlock;
import com.jship.BasicFluidHopper.block.BasicFluidHopperBlockEntity;
import com.jship.BasicFluidHopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.BasicFluidHopper.vehicle.BasicFluidHopperMinecartItem;

public class BasicFluidHopper implements ModInitializer {
    public static final String MOD_ID = "basic_fluid_hopper";

    public static final Logger LOGGER;
    public static final Block BASIC_FLUID_HOPPER_BLOCK;
    public static final Item BASIC_FLUID_HOPPER_ITEM;
    public static final BlockEntityType<BasicFluidHopperBlockEntity> BASIC_FLUID_HOPPER_BLOCK_ENTITY;
    public static final EntityType<BasicFluidHopperMinecartEntity> BASIC_FLUID_HOPPER_MINECART_ENTITY;
    public static final BasicFluidHopperMinecartItem BASIC_FLUID_HOPPER_MINECART_ITEM;
    public static final Identifier INTERACT_WITH_BASIC_FLUID_HOPPER;

    static {
        LOGGER = LoggerFactory.getLogger("Basic Fluid Hopper");

        BASIC_FLUID_HOPPER_BLOCK = Registry.register(
                Registries.BLOCK,
                Identifier.of(MOD_ID, "basic_fluid_hopper"),
                new BasicFluidHopperBlock(AbstractBlock.Settings.copy(Blocks.HOPPER)));
        BASIC_FLUID_HOPPER_ITEM = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "basic_fluid_hopper"),
                new BlockItem(BASIC_FLUID_HOPPER_BLOCK, new Item.Settings()));
        BASIC_FLUID_HOPPER_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(MOD_ID, "basic_fluid_hopper"),
                BlockEntityType.Builder.create(BasicFluidHopperBlockEntity::new, BASIC_FLUID_HOPPER_BLOCK).build(null));
        FluidStorage.SIDED.registerForBlockEntity((tank, direction) -> tank.fluidStorage,
                BASIC_FLUID_HOPPER_BLOCK_ENTITY);

        BASIC_FLUID_HOPPER_MINECART_ITEM = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "basic_fluid_hopper_minecart"),
                new BasicFluidHopperMinecartItem(new Item.Settings().maxCount(1)));
        BASIC_FLUID_HOPPER_MINECART_ENTITY = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(MOD_ID, "basic_fluid_hopper_minecart"),
                EntityType.Builder
                        .<BasicFluidHopperMinecartEntity>create(BasicFluidHopperMinecartEntity::new, SpawnGroup.MISC)
                        .dimensions(0.98F, 0.7F).build());
        // EntityApiLookup.<Storage<FluidVariant>, Direction>register
        // FluidStorage.SIDED.registerForBlockEntity((tank, direction) ->
        // tank.fluidStorage, BASIC_FLUID_HOPPER_BLOCK_ENTITY);

        INTERACT_WITH_BASIC_FLUID_HOPPER = Identifier.of(MOD_ID, "interact_with_basic_fluid_hopper");
    }

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.addAfter(Items.HOPPER, BASIC_FLUID_HOPPER_BLOCK);
            content.addAfter(Items.HOPPER_MINECART, BASIC_FLUID_HOPPER_MINECART_ITEM);
        });

        Registry.register(Registries.CUSTOM_STAT, "interact_with_basic_fluid_hopper", INTERACT_WITH_BASIC_FLUID_HOPPER);
        Stats.CUSTOM.getOrCreateStat(INTERACT_WITH_BASIC_FLUID_HOPPER, StatFormatter.DEFAULT);
    }
}

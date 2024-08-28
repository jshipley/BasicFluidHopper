package com.jship.basicfluidhopper;

import com.google.common.base.Suppliers;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;
import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartItem;
import com.mojang.logging.LogUtils;

import dev.architectury.core.block.ArchitecturyLiquidBlock;
import dev.architectury.core.fluid.ArchitecturyFlowingFluid;
import dev.architectury.core.fluid.ArchitecturyFluidAttributes;
import dev.architectury.core.fluid.SimpleArchitecturyFluidAttributes;
import dev.architectury.core.item.ArchitecturyBucketItem;
import dev.architectury.fluid.FluidStack;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.event.Level;

public class BasicFluidHopper {
        public static final String MOD_ID = "basic_fluid_hopper";
        public static final boolean DEBUG = false;
        public static final Logger LOGGER = LogUtils.getLogger();

        public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MOD_ID));

        public static final Registrar<Fluid> FLUIDS;
        public static final ArchitecturyFluidAttributes HONEY_FLUID_ATTRIBUTES;
        public static final RegistrySupplier<FlowingFluid> HONEY_FLUID;
        public static final RegistrySupplier<FlowingFluid> HONEY_FLUID_FLOWING;
        
        public static final Registrar<Block> BLOCKS;
        public static final RegistrySupplier<Block> BASIC_FLUID_HOPPER_BLOCK;
        public static final RegistrySupplier<LiquidBlock> HONEY_SOURCE_BLOCK;
        
        public static final Registrar<BlockEntityType<?>> BLOCK_ENTITIES;
        public static final RegistrySupplier<BlockEntityType<BasicFluidHopperBlockEntity>> BASIC_FLUID_HOPPER_BLOCK_ENTITY;

        public static final Registrar<EntityType<?>> ENTITIES;
        public static final RegistrySupplier<EntityType<BasicFluidHopperMinecartEntity>> BASIC_FLUID_HOPPER_MINECART_ENTITY;

        public static final Registrar<Item> ITEMS;
        public static final RegistrySupplier<Item> BASIC_FLUID_HOPPER_ITEM;
        public static final RegistrySupplier<Item> BASIC_FLUID_HOPPER_MINECART_ITEM;
        public static final RegistrySupplier<Item> HONEY_BUCKET;

        public static final TagKey<Fluid> C_HONEY = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation("c", "honey"));
        public static final TagKey<Fluid> C_VISUAL_HONEY = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation("c", "visual/honey"));
        public static final TagKey<Item> C_HONEY_BUCKETS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("c", "buckets/honey"));
        // Any fluid in tags/c/fluid/fuel will be usable in a furnace and will have the
        // same burn duration as lava
        public static final TagKey<Fluid> C_FLUID_FUEL = TagKey.create(Registry.FLUID_REGISTRY, new ResourceLocation("c", "fuel"));

        static {
                HONEY_FLUID_ATTRIBUTES = SimpleArchitecturyFluidAttributes.ofSupplier(() -> BasicFluidHopper.HONEY_FLUID_FLOWING, () -> BasicFluidHopper.HONEY_FLUID)
                        .density(3000).viscosity(4000).dropOff(2).tickDelay(60)
                        .color(0xCCFED167)
                        .emptySound(SoundEvents.BEEHIVE_DRIP).fillSound(SoundEvents.BEEHIVE_DRIP)
                        // .sourceTexture(new ResourceLocation(MOD_ID, "block/honey_still"))
                        .sourceTexture(new ResourceLocation("minecraft", "block/water_still"))
                        // .flowingTexture(new ResourceLocation(MOD_ID, "block/honey_flow"))
                        .flowingTexture(new ResourceLocation("minecraft", "block/water_flow"))
                        .blockSupplier(() -> BasicFluidHopper.HONEY_SOURCE_BLOCK)
                        .bucketItemSupplier(() -> BasicFluidHopper.HONEY_BUCKET);

                FLUIDS = REGISTRIES.get().get(Registry.FLUID);
                HONEY_FLUID = FLUIDS.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "honey"),
                        () -> new ArchitecturyFlowingFluid.Source(BasicFluidHopper.HONEY_FLUID_ATTRIBUTES));
                HONEY_FLUID_FLOWING = FLUIDS.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "flowing_honey"),
                        () -> new ArchitecturyFlowingFluid.Flowing(BasicFluidHopper.HONEY_FLUID_ATTRIBUTES));

                BLOCKS = REGISTRIES.get().get(Registry.BLOCK);
                BASIC_FLUID_HOPPER_BLOCK = BLOCKS.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                        () -> new BasicFluidHopperBlock(BlockBehaviour.Properties.copy(Blocks.HOPPER)));
                HONEY_SOURCE_BLOCK = BLOCKS.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "honey"),
                        () -> new ArchitecturyLiquidBlock(HONEY_FLUID, BlockBehaviour.Properties.of(Material.WATER, MaterialColor.COLOR_YELLOW)
                                                                                                // .replaceable()
                                                                                                .noCollission()
                                                                                                .strength(100.0F)
                                                                                                // .pushReaction(PushReaction.DESTROY)
                                                                                                .speedFactor(0.2F)
                                                                                                .jumpFactor(0.3F)
                                                                                                .noLootTable()
                                                                                                // .liquid()
                                                                                                .sound(SoundType.HONEY_BLOCK)));
                
                BLOCK_ENTITIES = REGISTRIES.get().get(Registry.BLOCK_ENTITY_TYPE);
                BASIC_FLUID_HOPPER_BLOCK_ENTITY = BLOCK_ENTITIES.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                        () -> BlockEntityType.Builder.of(BasicFluidHopperBlockEntity::new, BASIC_FLUID_HOPPER_BLOCK.get()).build(null));

                ENTITIES = REGISTRIES.get().get(Registry.ENTITY_TYPE);
                BASIC_FLUID_HOPPER_MINECART_ENTITY = ENTITIES.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "basic_fluid_hopper_minecart"),
                        () -> EntityType.Builder.<BasicFluidHopperMinecartEntity>of(BasicFluidHopperMinecartEntity::new, MobCategory.MISC).sized(0.98F, 0.7F).build("basic_fluid_hopper_minecart"));
                
                ITEMS = REGISTRIES.get().get(Registry.ITEM);
                BASIC_FLUID_HOPPER_ITEM = ITEMS.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                        () -> new BlockItem(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
                BASIC_FLUID_HOPPER_MINECART_ITEM = ITEMS.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "basic_fluid_hopper_minecart"),
                        () -> new BasicFluidHopperMinecartItem(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_REDSTONE)));
                HONEY_BUCKET = ITEMS.register(
                        new ResourceLocation(BasicFluidHopper.MOD_ID, "honey_bucket"),
                        () -> new ArchitecturyBucketItem(HONEY_FLUID, new Item.Properties().tab(CreativeModeTab.TAB_FOOD).craftRemainder(Items.BUCKET)));
        }

        public static void init() {
                if (DEBUG) LogUtils.configureRootLoggingLevel(Level.DEBUG);
                new BasicFluidHopper();

                // Copy the water bucket dispenser behavior for honey buckets
                DispenseItemBehavior dispenserBehavior = ((DispenserBlock)Blocks.DISPENSER).getDispenseMethod(new ItemStack(Items.WATER_BUCKET));
                DispenserBlock.registerBehavior(HONEY_BUCKET.get(), dispenserBehavior);

                MidnightConfig.init(MOD_ID, BasicFluidHopperConfig.class);
        }
}

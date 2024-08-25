package com.jship.basicfluidhopper;

import com.google.common.base.Suppliers;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;
import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
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
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
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

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.event.Level;

public class BasicFluidHopper {
        public static final String MOD_ID = "basic_fluid_hopper";
        public static final boolean DEBUG = false;
        public static final Logger LOGGER = LogUtils.getLogger();

        public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

        // CONSTANTS - could be configurable
        public static final long FUEL_CONSUME_STEP = FluidStack.bucketAmount() / 20;

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

        public static final TagKey<Fluid> C_HONEY = TagKey.create(Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath("c", "honey"));
        public static final TagKey<Fluid> C_VISUAL_HONEY = TagKey.create(Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath("c", "visual/honey"));
        public static final TagKey<Item> C_HONEY_BUCKETS = TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "buckets/honey"));
        // Any fluid in tags/c/fluid/fuel will be usable in a furnace and will have the
        // same burn duration as lava
        public static final TagKey<Fluid> C_FLUID_FUEL = TagKey.create(Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath("c", "fuel"));

        static {
                HONEY_FLUID_ATTRIBUTES = SimpleArchitecturyFluidAttributes.ofSupplier(() -> BasicFluidHopper.HONEY_FLUID_FLOWING, () -> BasicFluidHopper.HONEY_FLUID)
                        .density(3000).viscosity(4000).dropOff(2).tickDelay(60)
                        .color(0xCCFED167)
                        .emptySound(SoundEvents.BEEHIVE_DRIP).fillSound(SoundEvents.BEEHIVE_DRIP)
                        .sourceTexture(ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/honey_still"))
                        .flowingTexture(ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/honey_flow"))
                        .blockSupplier(() -> BasicFluidHopper.HONEY_SOURCE_BLOCK)
                        .bucketItemSupplier(() -> BasicFluidHopper.HONEY_BUCKET);

                FLUIDS = MANAGER.get().get(Registries.FLUID);
                HONEY_FLUID = FLUIDS.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey"),
                        () -> new ArchitecturyFlowingFluid.Source(BasicFluidHopper.HONEY_FLUID_ATTRIBUTES));
                HONEY_FLUID_FLOWING = FLUIDS.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "flowing_honey"),
                        () -> new ArchitecturyFlowingFluid.Flowing(BasicFluidHopper.HONEY_FLUID_ATTRIBUTES));

                BLOCKS = MANAGER.get().get(Registries.BLOCK);
                BASIC_FLUID_HOPPER_BLOCK = BLOCKS.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                        () -> new BasicFluidHopperBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER)));
                HONEY_SOURCE_BLOCK = BLOCKS.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey"),
                        () -> new ArchitecturyLiquidBlock(HONEY_FLUID, BlockBehaviour.Properties.of()
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
                
                BLOCK_ENTITIES = MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);
                BASIC_FLUID_HOPPER_BLOCK_ENTITY = BLOCK_ENTITIES.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                        () -> BlockEntityType.Builder.of(BasicFluidHopperBlockEntity::new, BASIC_FLUID_HOPPER_BLOCK.get()).build(null));

                ENTITIES = MANAGER.get().get(Registries.ENTITY_TYPE);
                BASIC_FLUID_HOPPER_MINECART_ENTITY = ENTITIES.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "basic_fluid_hopper_minecart"),
                        () -> EntityType.Builder.<BasicFluidHopperMinecartEntity>of(BasicFluidHopperMinecartEntity::new, MobCategory.MISC).sized(0.98F, 0.7F).build(null));
                
                ITEMS = MANAGER.get().get(Registries.ITEM);
                BASIC_FLUID_HOPPER_ITEM = ITEMS.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "basic_fluid_hopper"),
                        () -> new BlockItem(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get(), new Item.Properties().arch$tab(CreativeModeTabs.FUNCTIONAL_BLOCKS)));
                BASIC_FLUID_HOPPER_MINECART_ITEM = ITEMS.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "basic_fluid_hopper_minecart"),
                        () -> new BasicFluidHopperMinecartItem(new Item.Properties().stacksTo(1).arch$tab(CreativeModeTabs.FUNCTIONAL_BLOCKS)));
                HONEY_BUCKET = ITEMS.register(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey_bucket"),
                        () -> new ArchitecturyBucketItem(HONEY_FLUID, new Item.Properties().arch$tab(CreativeModeTabs.FUNCTIONAL_BLOCKS).craftRemainder(Items.BUCKET)));
        }

        public static void init() {
                if (DEBUG) LogUtils.configureRootLoggingLevel(Level.DEBUG);
                new BasicFluidHopper();
        }
}

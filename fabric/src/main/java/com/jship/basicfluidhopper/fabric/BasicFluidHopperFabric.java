package com.jship.basicfluidhopper.fabric;

import java.util.Optional;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.spiritapi.api.fluid.fabric.SpiritFluidStorageImpl;

import dev.architectury.core.block.ArchitecturyLiquidBlock;
import dev.architectury.core.fluid.ArchitecturyFlowingFluid;
import dev.architectury.core.fluid.ArchitecturyFluidAttributes;
import dev.architectury.core.fluid.SimpleArchitecturyFluidAttributes;
import dev.architectury.fluid.FluidStack;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;

public class BasicFluidHopperFabric implements ModInitializer {

    public static final ArchitecturyFluidAttributes MILK_FLUID_ATTRIBUTES = SimpleArchitecturyFluidAttributes
            .ofSupplier(
                    () -> BasicFluidHopperFabric.MILK_FLUID_FLOWING,
                    () -> BasicFluidHopperFabric.MILK_FLUID)
            .sourceTexture(BasicFluidHopper.id("block/milk_still"))
            .flowingTexture(BasicFluidHopper.id("block/milk_flow"))
            .overlayTexture(ResourceLocation.withDefaultNamespace("block/water_overlay"))
            .color(CommonColors.WHITE)
            .blockSupplier(() -> BasicFluidHopperFabric.MILK_SOURCE_BLOCK)
            .bucketItem(() -> Optional.of(Items.MILK_BUCKET));
    public static final RegistrySupplier<FlowingFluid> MILK_FLUID = BasicFluidHopper.FLUIDS.register(
            BasicFluidHopper.id("milk"),
            () -> new ArchitecturyFlowingFluid.Source(MILK_FLUID_ATTRIBUTES));
    public static final RegistrySupplier<FlowingFluid> MILK_FLUID_FLOWING = BasicFluidHopper.FLUIDS.register(
            BasicFluidHopper.id("flowing_milk"),
            () -> new ArchitecturyFlowingFluid.Flowing(MILK_FLUID_ATTRIBUTES));
    public static final RegistrySupplier<LiquidBlock> MILK_SOURCE_BLOCK = BasicFluidHopper.BLOCKS.register(
            BasicFluidHopper.id("milk"),
            () -> new ArchitecturyLiquidBlock(
                    MILK_FLUID,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.SNOW)));

    @Override
    public void onInitialize() {
        BasicFluidHopper.init();

        FluidStorage.SIDED.registerForBlockEntity(
                (tank, direction) -> ((SpiritFluidStorageImpl) tank.getFluidStorage()).fabricFluidStorage,
                BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get());
        BasicFluidHopper.HONEY_FLUID.listen(honey -> {
            FluidStorage.combinedItemApiProvider(Items.HONEY_BOTTLE).register(
                    context -> new FullItemFluidStorage(context, bottle -> ItemVariant.of(Items.GLASS_BOTTLE),
                            FluidVariant.of(honey), FluidStack.bucketAmount() / 3));
            FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(
                    context -> new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(Items.HONEY_BOTTLE), honey,
                            FluidStack.bucketAmount() / 3));
        });
        MILK_FLUID.listen(milk -> {
            FluidStorage.combinedItemApiProvider(Items.MILK_BUCKET).register(
                    context -> new FullItemFluidStorage(context, bucket -> ItemVariant.of(Items.BUCKET),
                            FluidVariant.of(milk), FluidStack.bucketAmount()));
            FluidStorage.combinedItemApiProvider(Items.BUCKET).register(
                    context -> new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(Items.MILK_BUCKET), milk,
                            FluidStack.bucketAmount()));
        });
        BasicFluidHopper.MILK_BOTTLE.listen(milkBottle -> {
            FluidStorage.combinedItemApiProvider(milkBottle).register(
                    context -> new FullItemFluidStorage(context, bottle -> ItemVariant.of(Items.GLASS_BOTTLE),
                            FluidVariant.of(MILK_FLUID.get()), FluidStack.bucketAmount() / 3));
            FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(
                    context -> new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(milkBottle),
                            MILK_FLUID.get(), FluidStack.bucketAmount() / 3));
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content -> {
            content.addAfter(Items.HOPPER, BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
            content.addAfter(Items.HOPPER_MINECART, BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get());
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(content -> {
            content.addAfter(Items.MILK_BUCKET, BasicFluidHopper.HONEY_BUCKET.get());
            content.addBefore(Items.HONEY_BOTTLE, BasicFluidHopper.MILK_BOTTLE.get());
        });
    }
}

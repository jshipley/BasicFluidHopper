package com.jship.basicfluidhopper.neoforge;

import org.jetbrains.annotations.NotNull;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.block.renderer.BasicFluidHopperBlockEntityRenderer;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;
import com.jship.basicfluidhopper.fluid.neoforge.BottleFluidHandler;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntityRenderer;
import com.jship.spiritapi.api.fluid.neoforge.SpiritFluidStorageImpl;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(BasicFluidHopper.MOD_ID)
public final class BasicFluidHopperNeoforge {

    public BasicFluidHopperNeoforge(IEventBus modEventBus) {
        BasicFluidHopper.init();

        NeoForgeMod.enableMilkFluid();

        ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory.class,
            () -> (client, parent) -> BasicFluidHopperConfig.createConfig(parent));

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::initializeClient);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
            event.accept(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(BasicFluidHopper.HONEY_BUCKET.get());
            event.accept(BasicFluidHopper.MILK_BOTTLE.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(),
                (blockEntity,
                        context) -> ((SpiritFluidStorageImpl) ((BasicFluidHopperBlockEntity) blockEntity).fluidStorage).neoFluidTank);

        event.registerEntity(
                Capabilities.FluidHandler.ENTITY,
                BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY.get(),
                (vehicle,
                        context) -> ((SpiritFluidStorageImpl) ((BasicFluidHopperMinecartEntity) vehicle).fluidStorage).neoFluidTank);

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (bottle, context) -> new BottleFluidHandler(bottle), Items.POTION, Items.HONEY_BOTTLE,
                Items.GLASS_BOTTLE, BasicFluidHopper.MILK_BOTTLE.get());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY.get(),
                (context) -> new BasicFluidHopperMinecartEntityRenderer(context, ModelLayers.HOPPER_MINECART));
        event.registerBlockEntityRenderer(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(),
                BasicFluidHopperBlockEntityRenderer::new);
    }

    private void initializeClient(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return BasicFluidHopper.HONEY_FLUID_ATTRIBUTES.getSourceTexture();
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return BasicFluidHopper.HONEY_FLUID_ATTRIBUTES.getFlowingTexture();
            }

            @Override
            public @NotNull int getTintColor() {
                return 0xCCFED167;
            }
        }, BasicFluidHopper.HONEY_FLUID.get().getFluidType());
    }
}

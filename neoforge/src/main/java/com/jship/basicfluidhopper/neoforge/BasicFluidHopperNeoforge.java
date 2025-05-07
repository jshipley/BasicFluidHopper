package com.jship.basicfluidhopper.neoforge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.fluid.neoforge.BottleFluidHandler;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.spiritapi.api.fluid.neoforge.SpiritFluidStorageImpl;

import dev.architectury.core.fluid.ArchitecturyFlowingFluid;
import dev.architectury.core.fluid.ArchitecturyFluidAttributes;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple.SwapEmpty;
import net.neoforged.neoforge.registries.DeferredHolder;

@Mod(BasicFluidHopper.MOD_ID)
public final class BasicFluidHopperNeoforge {

    

    public BasicFluidHopperNeoforge(IEventBus modEventBus) {
        BasicFluidHopper.init();

        NeoForgeMod.enableMilkFluid();

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::initializeClient);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
            event.accept(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get());
            event.accept(BasicFluidHopper.HONEY_BUCKET.get());
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(),
            (blockEntity, context) -> ((SpiritFluidStorageImpl) ((BasicFluidHopperBlockEntity)blockEntity).fluidStorage).neoFluidTank
        );

        event.registerEntity(
            Capabilities.FluidHandler.ENTITY,
            BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY.get(),
            (vehicle, context) -> ((SpiritFluidStorageImpl) ((BasicFluidHopperMinecartEntity)vehicle).fluidStorage).neoFluidTank
        );

        event.registerItem(
            Capabilities.FluidHandler.ITEM,
            (bottle, context) -> new BottleFluidHandler(bottle), Items.POTION, Items.HONEY_BOTTLE, Items.GLASS_BOTTLE);
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY.get(), (context) -> new MinecartRenderer<>(context, ModelLayers.HOPPER_MINECART));
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

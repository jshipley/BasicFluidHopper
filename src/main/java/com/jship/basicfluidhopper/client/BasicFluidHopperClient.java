package com.jship.basicfluidhopper.client;

import com.jship.basicfluidhopper.BasicFluidHopper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class BasicFluidHopperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY,
                context -> new MinecartRenderer<>(context, ModelLayers.HOPPER_MINECART));
        FluidRenderHandlerRegistry.INSTANCE.register(BasicFluidHopper.HONEY, BasicFluidHopper.FLOWING_HONEY,
                new SimpleFluidRenderHandler(
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "block/honey_still"),
                        ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "block/honey_flow"),
                        SimpleFluidRenderHandler.WATER_OVERLAY,
                        0xCCFED167));
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(),
                BasicFluidHopper.HONEY, BasicFluidHopper.FLOWING_HONEY);
    }
}

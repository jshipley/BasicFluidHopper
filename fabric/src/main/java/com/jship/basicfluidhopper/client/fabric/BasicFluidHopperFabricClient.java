package com.jship.basicfluidhopper.client.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;

@Environment(EnvType.CLIENT)
public class BasicFluidHopperFabricClient implements ClientModInitializer {

        @Override
        public void onInitializeClient() {
                EntityRendererRegistry.register(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY.get(),
                                context -> new MinecartRenderer<>(context, ModelLayers.HOPPER_MINECART));
        }
}

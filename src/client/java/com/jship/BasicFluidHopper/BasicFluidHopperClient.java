package com.jship.BasicFluidHopper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;

public class BasicFluidHopperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY,
                context -> new MinecartEntityRenderer<>(context, EntityModelLayers.HOPPER_MINECART));
    }
}

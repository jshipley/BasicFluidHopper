package com.jship.basicfluidhopper.client;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.renderer.BasicFluidHopperBlockEntityRenderer;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntityRenderer;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayers;

public class BasicFluidHopperClient {

    public static void init() {
        EntityRendererRegistry.register(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY,
                context -> new BasicFluidHopperMinecartEntityRenderer(context, ModelLayers.HOPPER_MINECART));
        BlockEntityRendererRegistry.register(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(),
                BasicFluidHopperBlockEntityRenderer::new);
    }
}

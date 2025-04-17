package com.jship.basicfluidhopper.client.fabric;

// import com.jship.basicfluidhopper.block.renderer.BasicFluidHopperBlockEntityRenderer;
import com.jship.basicfluidhopper.client.BasicFluidHopperClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BasicFluidHopperFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BasicFluidHopperClient.init();
        // EntityRendererRegistry.register(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY.get(),
        //                 context -> new MinecartRenderer<>(context, ModelLayers.HOPPER_MINECART));

        // BlockEntityRendererFactories.register(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(), BasicFluidHopperBlockEntityRenderer::new);
    }
}

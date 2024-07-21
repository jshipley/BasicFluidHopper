package com.jship.basicfluidhopper.client.blockrenderer;

import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class BasicFluidHopperFluidRenderer implements BlockEntityRenderer<BasicFluidHopperBlockEntity> {
    public BlockEntityRendererProvider.Context context;
    public BasicFluidHopperFluidRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(BasicFluidHopperBlockEntity blockEntity, float tickDelta, PoseStack poses, MultiBufferSource bufferSource, int light, int overlay) {
        poses.pushPose();
        
        double offset = Math.sin((blockEntity.getLevel().getDayTime()+tickDelta)/8.0)/4.0;
        poses.translate(0.5, 1.25 + offset, 0.5);
        BasicFluidHopper.LOGGER.info("[Basic Fluid Hopper] isResourceBlank? {} resource {}", blockEntity.fluidStorage.isResourceBlank(), blockEntity.fluidStorage.getResource());
        if (!blockEntity.fluidStorage.isResourceBlank()) {
            BasicFluidHopper.LOGGER.info("[Basic Fluid Hopper] trying to render fluid");
            // Minecraft.getInstance().getBlockRenderer().renderLiquid(blockEntity.getBlockPos(), blockEntity.getLevel().getChunk(blockEntity.getBlockPos()), bufferSource.getBuffer(RenderType.translucent()), blockEntity.getBlockState(), blockEntity.fluidStorage.getResource().getFluid().defaultFluidState());
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(BasicFluidHopper.HONEY_SOURCE_BLOCK.defaultBlockState(), poses, bufferSource, light, overlay);
        }

        poses.popPose();
    }
}

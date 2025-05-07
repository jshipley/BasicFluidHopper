package com.jship.basicfluidhopper.block.renderer.fabric;

import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.fluid.FluidStack;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class BasicFluidHopperBlockEntityRendererImpl implements BlockEntityRenderer<BasicFluidHopperBlockEntity> {

    public BasicFluidHopperBlockEntityRendererImpl(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
        BasicFluidHopperBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int light,
        int overlay
    ) {
        FluidStack fluidStack = blockEntity.getFluidStorage().getFluidInTank(0);
        if (!fluidStack.isEmpty()) return;
        // TODO render fluid in the hopper
    }
}

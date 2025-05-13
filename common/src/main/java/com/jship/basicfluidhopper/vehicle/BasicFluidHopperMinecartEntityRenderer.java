package com.jship.basicfluidhopper.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class BasicFluidHopperMinecartEntityRenderer extends MinecartRenderer<BasicFluidHopperMinecartEntity> {

    public BasicFluidHopperMinecartEntityRenderer(Context context, ModelLayerLocation layer) {
        super(context, layer);
    }

    @Override
    protected void renderMinecartContents(BasicFluidHopperMinecartEntity entity, float partialTicks, BlockState state,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.renderMinecartContents(entity, partialTicks, state, poseStack, buffer, packedLight);

        FluidStack fluidStack = entity.getFluidStack();
        if (fluidStack.isEmpty())
            return;

        TextureAtlasSprite stillSprite = FluidStackHooks.getStillTexture(fluidStack);
        if (stillSprite == null)
            return;

        Level level = entity.level();
        FluidState fluidState = fluidStack.getFluid().defaultFluidState();
        int tintColor = FluidStackHooks.getColor(level, entity.getOnPos(), fluidState);
        // If the alpha is almost 0, set it higher
        // I'm doing this because the alpha for water was 0 on Fabric, and water was
        // invisible
        if ((tintColor & 0xFF000000) < 0x0F000000) {
            tintColor |= 0xCF000000;
        }

        float height = Math.min(1.0f,
                fluidStack.getAmount() / (float) entity.getFluidStorage().getTankCapacity(0))
                * (4.8f / 16f) + (11.01f / 16f);

        VertexConsumer builder = buffer.getBuffer(ItemBlockRenderTypes.getRenderLayer(fluidState));
        drawVertex(builder, poseStack, 2f / 16f, height, 2f / 16f, stillSprite.getU0(), stillSprite.getV0(),
                packedLight, OverlayTexture.NO_OVERLAY, tintColor);
        drawVertex(builder, poseStack, 2f / 16f, height, 14f / 16f, stillSprite.getU0(), stillSprite.getV1(),
                packedLight, OverlayTexture.NO_OVERLAY, tintColor);
        drawVertex(builder, poseStack, 14f / 16f, height, 14f / 16f, stillSprite.getU1(), stillSprite.getV1(),
                packedLight, OverlayTexture.NO_OVERLAY, tintColor);
        drawVertex(builder, poseStack, 14f / 16f, height, 2f / 16f, stillSprite.getU1(), stillSprite.getV0(),
                packedLight, OverlayTexture.NO_OVERLAY, tintColor);
    }

    private static void drawVertex(VertexConsumer builder, PoseStack poseStack, float x, float y, float z, float u,
            float v, int packedLight, int packedOverlay, int tintColor) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
                .setUv(u, v)
                .setLight(packedLight)
                .setColor(tintColor)
                .setNormal(0, 1, 0);
    }
}

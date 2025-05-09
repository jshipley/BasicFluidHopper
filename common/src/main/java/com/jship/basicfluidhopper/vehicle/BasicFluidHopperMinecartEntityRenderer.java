package com.jship.basicfluidhopper.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.architectury.hooks.fluid.FluidStackHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class BasicFluidHopperMinecartEntityRenderer extends AbstractMinecartRenderer<BasicFluidHopperMinecartEntity, BasicFluidHopperMinecartRenderState> {

    public BasicFluidHopperMinecartEntityRenderer(Context context, ModelLayerLocation layer) {
        super(context, layer);
    }

    @Override
    public BasicFluidHopperMinecartRenderState createRenderState() {
        return new BasicFluidHopperMinecartRenderState();
    }

    @Override
    public void extractRenderState(BasicFluidHopperMinecartEntity minecartEntity, BasicFluidHopperMinecartRenderState renderState, float f) {
        super.extractRenderState(minecartEntity, renderState, f);

        renderState.fluidContents = minecartEntity.getFluidStack();
        renderState.fluidCapacity = minecartEntity.fluidStorage.getTankCapacity(0);
    }

    @Override
    protected void renderMinecartContents(BasicFluidHopperMinecartRenderState renderState, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.renderMinecartContents(renderState, state, poseStack, buffer, packedLight);

        if (renderState.fluidContents.isEmpty())
            return;

        TextureAtlasSprite stillSprite = FluidStackHooks.getStillTexture(renderState.fluidContents);
        if (stillSprite == null)
            return;

        Level level = Minecraft.getInstance().level;
        FluidState fluidState = renderState.fluidContents.getFluid().defaultFluidState();
        int tintColor = FluidStackHooks.getColor(level, BlockPos.containing(renderState.posOnRail.x, renderState.posOnRail.y, renderState.posOnRail.z), fluidState);

        float height = (float) renderState.fluidContents.getAmount() / (float) renderState.fluidCapacity
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

package com.jship.basicfluidhopper.jade.fabric;

import java.util.List;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.fluid.fabric.HopperFluidStorageImpl;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

public enum HopperMinecartFluidStorageProvider implements IServerExtensionProvider<BasicFluidHopperMinecartEntity, CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
    INSTANCE;

    public static final ResourceLocation FLUID_STORAGE = new ResourceLocation(BasicFluidHopper.MOD_ID, "fluid_storage");

    @Override
    public ResourceLocation getUid() {
        return FLUID_STORAGE;
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
        return ClientViewGroup.map(groups, FluidView::read, null);
    }

    @Override
    public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel level, BasicFluidHopperMinecartEntity entity, boolean showDetails) {
        return FluidView.fromStorage(((HopperFluidStorageImpl)entity.getFluidStorage()).getFluidStorage());
    }
}

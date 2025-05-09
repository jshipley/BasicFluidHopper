package com.jship.basicfluidhopper.jade.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.spiritapi.api.fluid.SpiritFluidStorage;
import com.jship.spiritapi.api.fluid.fabric.SpiritFluidStorageImpl;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.util.JadeFabricUtils;

public enum HopperMinecartFluidStorageProvider implements
    IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {
    INSTANCE;

    public static final ResourceLocation FLUID_STORAGE = ResourceLocation.fromNamespaceAndPath(
        BasicFluidHopper.MOD_ID,
        "fluid_storage"
    );

    @Override
    public ResourceLocation getUid() {
        return FLUID_STORAGE;
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<FluidView.Data>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, null);
    }

    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor<?> accessor) {
        return JadeFabricUtils.fromFluidStorage(
            ((SpiritFluidStorageImpl) ((BasicFluidHopperMinecartEntity) accessor.getTarget()).getFluidStorage()).fabricFluidStorage
        );
    }
}

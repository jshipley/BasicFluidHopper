package com.jship.basicfluidhopper.jade.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.fluid.fabric.HopperFluidStorageImpl;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.spiritapi.api.fluid.SpiritFluidStorage;

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
    IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
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
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, null);
    }

    @Override
    public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        // return JadeFabricUtils.fromFluidStorage(
        //     ((SpiritFluidStorageImpl) ((BasicFluidHopperMinecartEntity) accessor.getTarget()).getFluidStorage()).fabricFluidStorage
        // );
        return List.of();
    }
}

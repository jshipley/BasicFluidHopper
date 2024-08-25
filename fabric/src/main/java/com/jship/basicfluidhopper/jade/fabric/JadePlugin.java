package com.jship.basicfluidhopper.jade.fabric;

import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerFluidStorage(HopperMinecartFluidStorageProvider.INSTANCE, BasicFluidHopperMinecartEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerFluidStorageClient(HopperMinecartFluidStorageProvider.INSTANCE);
    }
}

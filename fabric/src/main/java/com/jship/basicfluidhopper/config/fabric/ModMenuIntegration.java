package com.jship.basicfluidhopper.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> BasicFluidHopperConfig.createConfig(parentScreen);
    }
}

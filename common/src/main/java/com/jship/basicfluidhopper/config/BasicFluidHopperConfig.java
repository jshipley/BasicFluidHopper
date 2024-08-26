package com.jship.basicfluidhopper.config;

import dev.architectury.fluid.FluidStack;
import eu.midnightdust.lib.config.MidnightConfig;

public class BasicFluidHopperConfig extends MidnightConfig {
    @Entry(min=0, max=100)
    public static final int TRANSFER_COOLDOWN = 8;
    @Entry(min=1, max=1000)
    public static final int BUCKET_CAPACITY = 1;
    @Entry(min=0.25, max=1000, precision=4)
    public static final float MAX_TRANSFER = 1.0F;
}

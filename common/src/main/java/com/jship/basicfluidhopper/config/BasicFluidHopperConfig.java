package com.jship.basicfluidhopper.config;

import dev.architectury.fluid.FluidStack;
import eu.midnightdust.lib.config.MidnightConfig;

public class BasicFluidHopperConfig extends MidnightConfig {
    @Entry(min=0, max=100)
    public static int TRANSFER_COOLDOWN = 8;
    @Entry(min=1, max=1000)
    public static int BUCKET_CAPACITY = 1;
    @Entry(min=0.25F, max=1000F, precision=4)
    public static float MAX_TRANSFER = 1.0F;
    @Entry(min=0.1F)
    public static float FUEL_CONSUME_STEP = 0.2F;
}

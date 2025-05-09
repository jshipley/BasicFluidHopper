package com.jship.basicfluidhopper.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BasicFluidHopperConfig {
    public static final BasicFluidHopperConfig INSTANCE = new BasicFluidHopperConfig();

    private int transferCooldown = 8;
    private int hopperCapacity = 1;
    private float transferRate = 1.0f;
    private float fuelConsumeStep = 0.1f;

    public static int transferCooldown() {
        return INSTANCE.transferCooldown;
    }

    public static int hopperCapacity() {
        return INSTANCE.hopperCapacity;
    }

    public static float transferRate() {
        return INSTANCE.transferRate;
    }

    public static float fuelConsumeStep() {
        return INSTANCE.fuelConsumeStep;
    }

    public static ValueFormatter<Integer> tickFormatter = new ValueFormatter<Integer>() {
        public Component format(Integer value) {
            return Component.translatable("config.basic_fluid_hopper.tick_formatter", value);
        }
    };

    public static ValueFormatter<Integer> intBucketFormatter = new ValueFormatter<Integer>() {
        public Component format(Integer value) {
            return Component.translatable("config.basic_fluid_hopper.int_bucket_formatter", value);
        }
    };

    public static ValueFormatter<Float> floatBucketFormatter = new ValueFormatter<Float>() {
        public Component format(Float value) {
            return Component.translatable("config.basic_fluid_hopper.float_bucket_formatter", value);
        }
    };

    public static Screen createConfig(Screen parentScreen) {
        return YetAnotherConfigLib.createBuilder()
            .title(Component.literal("Basic Fluid Hopper"))
            
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Basic Fluid Hopper"))
                .option(Option.<Integer>createBuilder()
                    .name(Component.translatable("config.basic_fluid_hopper.transfer_cooldown"))
                    .description(OptionDescription.of(Component.translatable("config.basic_fluid_hopper.transfer_cooldown.desc")))
                    .binding(8, () -> INSTANCE.transferCooldown, newVal -> INSTANCE.transferCooldown = newVal)
                    .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(0, 100)
                        .formatValue(tickFormatter))
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.translatable("config.basic_fluid_hopper.hopper_capacity"))
                    .description(OptionDescription.of(Component.translatable("config.basic_fluid_hopper.hopper_capacity.desc")))
                    .binding(1, () -> INSTANCE.hopperCapacity, newVal -> INSTANCE.hopperCapacity = newVal)
                    .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(0, 1000)
                        .formatValue(intBucketFormatter))
                    .build())
                .option(Option.<Float>createBuilder()
                    .name(Component.translatable("config.basic_fluid_hopper.transfer_rate"))
                    .description(OptionDescription.of(Component.translatable("config.basic_fluid_hopper.transfer_rate.desc")))
                    .binding(1.0f, () -> INSTANCE.transferRate, newVal -> INSTANCE.transferRate = newVal)
                    .controller(opt -> FloatFieldControllerBuilder.create(opt)
                        .range(0.25f, 1000.0f)
                        .formatValue(floatBucketFormatter))
                    .build())
                .option(Option.<Float>createBuilder()
                    .name(Component.translatable("config.basic_fluid_hopper.fuel_consume_step"))
                    .description(OptionDescription.of(Component.translatable("config.basic_fluid_hopper.fuel_consume_step.desc")))
                    .binding(0.1f, () -> INSTANCE.fuelConsumeStep, newVal -> INSTANCE.fuelConsumeStep = newVal)
                    .controller(opt -> FloatFieldControllerBuilder.create(opt)
                        .range(0.01f, 1.0f)
                        .formatValue(floatBucketFormatter))
                    .build())
                .build()
            ).build()
            .generateScreen(parentScreen);
    }
}

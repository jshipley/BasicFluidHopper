package com.jship.basicfluidhopper.config;

import com.google.gson.GsonBuilder;
import com.jship.basicfluidhopper.BasicFluidHopper;

import dev.architectury.platform.Platform;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Accessors(fluent=true)
public class BasicFluidHopperConfig {
    public static ConfigClassHandler<BasicFluidHopperConfig> HANDLER = ConfigClassHandler.createBuilder(BasicFluidHopperConfig.class)
        .id(BasicFluidHopper.id("basic_fluid_hopper_config"))
        .serializer(config -> GsonConfigSerializerBuilder.create(config)
            .setPath(Platform.getConfigFolder().resolve("basic_fluid_hopper.json5"))
            .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
            .setJson5(true)
            .build())
        .build();

    public static final int defaultTransferCooldown = 8;
    @Getter @Setter @SerialEntry
    public static int transferCooldown = defaultTransferCooldown;

    public static final int defaultHopperCapacity = 1;
    @Getter @Setter @SerialEntry
    public static int hopperCapacity = defaultHopperCapacity;

    public static final float defaultTransferRate = 1.0f;
    @Getter @Setter @SerialEntry
    public static float transferRate = defaultTransferRate;

    public static final float defaultFuelConsumeStep = 0.1f;
    @Getter @Setter @SerialEntry
    public static float fuelConsumeStep = defaultFuelConsumeStep;

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
            .save(() -> HANDLER.save())
            .title(Component.literal("Basic Fluid Hopper"))
            
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Basic Fluid Hopper"))
                .option(Option.<Integer>createBuilder()
                    .name(Component.translatable("config.basic_fluid_hopper.transfer_cooldown"))
                    .description(OptionDescription.of(Component.translatable("config.basic_fluid_hopper.transfer_cooldown.desc")))
                    .binding(defaultTransferCooldown, BasicFluidHopperConfig::transferCooldown, BasicFluidHopperConfig::transferCooldown)
                    .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(0, 100)
                        .formatValue(tickFormatter))
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.translatable("config.basic_fluid_hopper.hopper_capacity"))
                    .description(OptionDescription.of(Component.translatable("config.basic_fluid_hopper.hopper_capacity.desc")))
                    .binding(defaultHopperCapacity, BasicFluidHopperConfig::hopperCapacity, BasicFluidHopperConfig::hopperCapacity)
                    .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(0, 1000)
                        .formatValue(intBucketFormatter))
                    .build())
                .option(Option.<Float>createBuilder()
                    .name(Component.translatable("config.basic_fluid_hopper.transfer_rate"))
                    .description(OptionDescription.of(Component.translatable("config.basic_fluid_hopper.transfer_rate.desc")))
                    .binding(defaultTransferRate, BasicFluidHopperConfig::transferRate, BasicFluidHopperConfig::transferRate)
                    .controller(opt -> FloatFieldControllerBuilder.create(opt)
                        .range(0.25f, 1000.0f)
                        .formatValue(floatBucketFormatter))
                    .build())
                .option(Option.<Float>createBuilder()
                    .name(Component.translatable("config.basic_fluid_hopper.fuel_consume_step"))
                    .description(OptionDescription.of(Component.translatable("config.basic_fluid_hopper.fuel_consume_step.desc")))
                    .binding(defaultFuelConsumeStep, BasicFluidHopperConfig::fuelConsumeStep, BasicFluidHopperConfig::fuelConsumeStep)
                    .controller(opt -> FloatFieldControllerBuilder.create(opt)
                        .range(0.01f, 1.0f)
                        .formatValue(floatBucketFormatter))
                    .build())
                .build()
            ).build()
            .generateScreen(parentScreen);
    }
}

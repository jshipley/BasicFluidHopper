package com.jship.basicfluidhopper.neoforge;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;
import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(BasicFluidHopper.MOD_ID)
public final class BasicFluidHopperNeoforge {
    //     public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK,
    //             BasicFluidHopper.MOD_ID);
    //     public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister
    //             .create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BasicFluidHopper.MOD_ID);
    //     public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM,
    //             BasicFluidHopper.MOD_ID);
    //     public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
    //             .create(BuiltInRegistries.ENTITY_TYPE, BasicFluidHopper.MOD_ID);
    //     public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID,
    //             BasicFluidHopper.MOD_ID);

    //     static {
    //         BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK = BLOCKS.register("basic_fluid_hopper",
    //                 () -> new BasicFluidHopperBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER)));
    //         BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM = ITEMS.register("basic_fluid_hopper",
    //                 () -> new BlockItem(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get(), new Item.Properties()));
    //         BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("basic_fluid_hopper",
    //                 () -> BlockEntityType.Builder
    //                         .of(BasicFluidHopperBlockEntity::new,
    //                                 BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get())
    //                         .build(null));

    //         BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM = ITEMS.register("basic_fluid_hopper_minecart",
    //                 () -> new BasicFluidHopperMinecartItem(new Item.Properties().stacksTo(1)));
    //         BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ENTITY = ENTITY_TYPES.register("basic_fluid_hopper_minecart",
    //                 () -> EntityType.Builder
    //                         .<BasicFluidHopperMinecartEntity>of(BasicFluidHopperMinecartEntity::new,
    //                                 MobCategory.MISC)
    //                         .sized(0.98F, 0.7F).build("basic_fluid_hopper_minecart"));

    //         BasicFluidHopper.HONEY = FLUIDS.register("honey",
    //                 HoneyFluid.Source::new);
    //         BasicFluidHopper.FLOWING_HONEY = FLUIDS.register("flowing_honey",
    //                 HoneyFluid.Flowing::new);
    //         BasicFluidHopper.HONEY_SOURCE_BLOCK = BLOCKS.register("honey",
    //                 () -> new LiquidBlock(BasicFluidHopper.HONEY.get(), BlockBehaviour.Properties.of()
    //                         .mapColor(MapColor.COLOR_YELLOW)
    //                         .replaceable()
    //                         .noCollission()
    //                         .strength(100.0F)
    //                         .pushReaction(PushReaction.DESTROY)
    //                         .speedFactor(0.2F)
    //                         .jumpFactor(0.3F)
    //                         .noLootTable()
    //                         .liquid()
    //                         .sound(SoundType.HONEY_BLOCK)));
    //         BasicFluidHopper.HONEY_BUCKET = ITEMS.register("honey_bucket",
    //                 () -> new BucketItem(BasicFluidHopper.HONEY.get(),
    //                         new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    //     }

    //     public BasicFluidHopperNeoforge(IEventBus modEventBus) {
    //         BasicFluidHopper.init();

    //         modEventBus.addListener(this::addCreative);
    //     }

    //     private void addCreative(BuildCreativeModeTabContentsEvent event) {
    //         if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
    //             event.accept(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
    //             event.accept(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get());
    //             event.accept(BasicFluidHopper.HONEY_BUCKET.get());
    //         }
    //     }
}

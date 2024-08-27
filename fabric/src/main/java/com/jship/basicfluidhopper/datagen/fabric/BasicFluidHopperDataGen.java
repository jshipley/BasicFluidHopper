package com.jship.basicfluidhopper.datagen.fabric;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.jship.basicfluidhopper.BasicFluidHopper;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class BasicFluidHopperDataGen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(BFHFluidTagGenerator::new);
        pack.addProvider(BFHItemTagGenerator::new);
        pack.addProvider(BFHLootTableGenerator::new);
        pack.addProvider(BFHBlockTagGenerator::new);
        pack.addProvider(BFHRecipeGenerator::new);
    }

    private static class BFHFluidTagGenerator extends FabricTagProvider.FluidTagProvider {
        public BFHFluidTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            getOrCreateTagBuilder(FluidTags.WATER).add(BasicFluidHopper.HONEY_FLUID.get());
            getOrCreateTagBuilder(BasicFluidHopper.C_HONEY).add(BasicFluidHopper.HONEY_FLUID.get()).add(BasicFluidHopper.HONEY_FLUID_FLOWING.get());
            getOrCreateTagBuilder(BasicFluidHopper.C_VISUAL_HONEY).add(BasicFluidHopper.HONEY_FLUID.get()).add(BasicFluidHopper.HONEY_FLUID_FLOWING.get());
            getOrCreateTagBuilder(BasicFluidHopper.C_FLUID_FUEL).add(Fluids.LAVA);
        }
    }

    private static class BFHItemTagGenerator extends FabricTagProvider.ItemTagProvider {
        public BFHItemTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            getOrCreateTagBuilder(BasicFluidHopper.C_HONEY_BUCKETS).add(BasicFluidHopper.HONEY_BUCKET.get());
        }
    }

    private static class BFHBlockTagGenerator extends FabricTagProvider.BlockTagProvider {
        public BFHBlockTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE).add(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
        }
    }

    private static class BFHLootTableGenerator extends FabricBlockLootTableProvider {
        public BFHLootTableGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generate() {
            dropSelf(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
        }
    }

    private static class BFHRecipeGenerator extends FabricRecipeProvider {
        private BFHRecipeGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void buildRecipes(Consumer<FinishedRecipe> exporter) {
            
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get())
                .define('I', Items.IRON_INGOT).define('B', Items.BUCKET)
                .pattern("I I")
                .pattern("IBI")
                .pattern(" I ")
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(exporter, new ResourceLocation(BasicFluidHopper.MOD_ID, "fluid_hopper"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get())
                .requires(Items.MINECART).requires(BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM.get())
                .unlockedBy("has_fluid_hopper_and_minecart", inventoryTrigger(new ItemPredicate[]{
                    ItemPredicate.Builder.item().of(BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM.get()).build(),
                    ItemPredicate.Builder.item().of(Items.MINECART).build()}))
                .save(exporter, new ResourceLocation(BasicFluidHopper.MOD_ID, "fluid_hopper_minecart"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, Items.HONEY_BLOCK)
                .requires(BasicFluidHopper.C_HONEY_BUCKETS)
                .unlockedBy("has_honey_bucket", has(BasicFluidHopper.C_HONEY_BUCKETS))
                .save(exporter, new ResourceLocation(BasicFluidHopper.MOD_ID, "honey_block"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BasicFluidHopper.HONEY_BUCKET.get())
                .requires(Items.HONEY_BLOCK).requires(Items.BUCKET)
                .unlockedBy(getHasName(Items.HONEY_BLOCK), has(Items.HONEY_BLOCK))
                .save(exporter, new ResourceLocation(BasicFluidHopper.MOD_ID, "honey_bucket_from_block"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.HONEY_BOTTLE, 3)
                .requires(BasicFluidHopper.C_HONEY_BUCKETS).requires(Items.GLASS_BOTTLE, 3)
                .unlockedBy("has_honey_bucket", has(BasicFluidHopper.C_HONEY_BUCKETS))
                .save(exporter, new ResourceLocation(BasicFluidHopper.MOD_ID, "honey_bottles_from_bucket"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.HONEY_BOTTLE, 3)
                .requires(Items.HONEY_BLOCK).requires(Items.GLASS_BOTTLE, 3)
                .unlockedBy(getHasName(Items.HONEY_BLOCK), has(Items.HONEY_BLOCK))
                .save(exporter, new ResourceLocation(BasicFluidHopper.MOD_ID, "honey_bottles_from_block"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BasicFluidHopper.HONEY_BUCKET.get())
                .requires(Items.BUCKET).requires(Items.HONEY_BOTTLE, 3)
                .unlockedBy(getHasName(Items.HONEY_BOTTLE), has(Items.HONEY_BOTTLE))
                .save(exporter, new ResourceLocation(BasicFluidHopper.MOD_ID, "empty_bottles"));
        }
    }
}

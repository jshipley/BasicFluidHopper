package com.jship.basicfluidhopper.datagen.fabric;

import com.jship.basicfluidhopper.BasicFluidHopper;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.blockstates.VariantProperties.Rotation;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
        pack.addProvider(BFHModelGenerator::new);
    }

    private static class BFHFluidTagGenerator extends FabricTagProvider.FluidTagProvider {

        public BFHFluidTagGenerator(
                FabricDataOutput output,
                CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            getOrCreateTagBuilder(FluidTags.WATER).add(BasicFluidHopper.HONEY_FLUID.get());
            getOrCreateTagBuilder(BasicFluidHopper.C_HONEY)
                    .add(BasicFluidHopper.HONEY_FLUID.get())
                    .add(BasicFluidHopper.HONEY_FLUID_FLOWING.get());
            getOrCreateTagBuilder(BasicFluidHopper.C_VISUAL_HONEY)
                    .add(BasicFluidHopper.HONEY_FLUID.get())
                    .add(BasicFluidHopper.HONEY_FLUID_FLOWING.get());
            getOrCreateTagBuilder(BasicFluidHopper.C_FLUID_FUEL).add(Fluids.LAVA);
        }
    }

    private static class BFHItemTagGenerator extends FabricTagProvider.ItemTagProvider {

        public BFHItemTagGenerator(
                FabricDataOutput output,
                CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            getOrCreateTagBuilder(BasicFluidHopper.C_HONEY_BUCKETS).add(BasicFluidHopper.HONEY_BUCKET.get());
        }
    }

    private static class BFHBlockTagGenerator extends FabricTagProvider.BlockTagProvider {

        public BFHBlockTagGenerator(
                FabricDataOutput output,
                CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE).add(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
        }
    }

    private static class BFHLootTableGenerator extends FabricBlockLootTableProvider {

        public BFHLootTableGenerator(
                FabricDataOutput output,
                CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        public void generate() {
            dropSelf(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
        }
    }

    private static class BFHRecipeGenerator extends FabricRecipeProvider {

        private BFHRecipeGenerator(
                FabricDataOutput output,
                CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        public void buildRecipes(RecipeOutput exporter) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get())
                    .define('I', Items.IRON_INGOT)
                    .define('B', Items.BUCKET)
                    .pattern("I I")
                    .pattern("IBI")
                    .pattern(" I ")
                    .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .save(exporter, ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "fluid_hopper"));
            ShapelessRecipeBuilder.shapeless(
                    RecipeCategory.MISC,
                    BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get())
                    .requires(Items.MINECART)
                    .requires(BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM.get())
                    .unlockedBy(
                            "has_fluid_hopper_and_minecart",
                            inventoryTrigger(
                                    ItemPredicate.Builder.item().of(BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM.get()),
                                    ItemPredicate.Builder.item().of(Items.MINECART)))
                    .save(
                            exporter,
                            ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "fluid_hopper_minecart"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, Items.HONEY_BLOCK)
                    .requires(BasicFluidHopper.C_HONEY_BUCKETS)
                    .unlockedBy("has_honey_bucket", has(BasicFluidHopper.C_HONEY_BUCKETS))
                    .save(
                            exporter,
                            ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey_block_from_bucket"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BasicFluidHopper.HONEY_BUCKET.get())
                    .requires(Items.HONEY_BLOCK)
                    .requires(Items.BUCKET)
                    .unlockedBy(getHasName(Items.HONEY_BLOCK), has(Items.HONEY_BLOCK))
                    .save(
                            exporter,
                            ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey_bucket_from_block"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.HONEY_BOTTLE, 3)
                    .requires(BasicFluidHopper.C_HONEY_BUCKETS)
                    .requires(Items.GLASS_BOTTLE, 3)
                    .unlockedBy("has_honey_bucket", has(BasicFluidHopper.C_HONEY_BUCKETS))
                    .save(
                            exporter,
                            ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID,
                                    "honey_bottles_from_bucket"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, Items.HONEY_BOTTLE, 3)
                    .requires(Items.HONEY_BLOCK)
                    .requires(Items.GLASS_BOTTLE, 3)
                    .unlockedBy(getHasName(Items.HONEY_BLOCK), has(Items.HONEY_BLOCK))
                    .save(
                            exporter,
                            ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID, "honey_bottles_from_block"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BasicFluidHopper.HONEY_BUCKET.get())
                    .requires(Items.BUCKET)
                    .requires(Items.HONEY_BOTTLE, 3)
                    .unlockedBy(getHasName(Items.HONEY_BOTTLE), has(Items.HONEY_BOTTLE))
                    .save(
                            exporter,
                            ResourceLocation.fromNamespaceAndPath(BasicFluidHopper.MOD_ID,
                                    "honey_bucket_from_bottles"));
        }
    }

    private static class BFHModelGenerator extends FabricModelProvider {

        public BFHModelGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
            blockStateModelGenerator.createTrivialBlock(BasicFluidHopper.HONEY_SOURCE_BLOCK.get(), TexturedModel.PARTICLE_ONLY);

            ResourceLocation hopper = ModelLocationUtils.getModelLocation(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get());
            ResourceLocation hopperSide = ModelLocationUtils.getModelLocation(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get(), "_side");
            blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get())
                    .with(PropertyDispatch.property(BlockStateProperties.FACING_HOPPER)
                            .select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, hopper))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, hopperSide))
                            .select(Direction.EAST, Variant.variant().with(VariantProperties.MODEL, hopperSide).with(VariantProperties.Y_ROT, Rotation.R90))
                            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, hopperSide).with(VariantProperties.Y_ROT, Rotation.R180))
                            .select(Direction.WEST, Variant.variant().with(VariantProperties.MODEL, hopperSide).with(VariantProperties.Y_ROT, Rotation.R270))));
            
        }

        @Override
        public void generateItemModels(ItemModelGenerators itemModelGenerator) {
            itemModelGenerator.generateFlatItem(BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM.get(), ModelTemplates.FLAT_ITEM);
            itemModelGenerator.generateFlatItem(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get(), ModelTemplates.FLAT_ITEM);
            itemModelGenerator.generateFlatItem(BasicFluidHopper.HONEY_BUCKET.get(), ModelTemplates.FLAT_ITEM);
        }

    }
}

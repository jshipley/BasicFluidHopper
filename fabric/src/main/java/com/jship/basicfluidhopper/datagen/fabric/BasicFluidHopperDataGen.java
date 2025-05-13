package com.jship.basicfluidhopper.datagen.fabric;

import java.util.concurrent.CompletableFuture;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.fabric.BasicFluidHopperFabric;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalFluidTags;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
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
            getOrCreateTagBuilder(ConventionalFluidTags.HONEY)
                    .add(BasicFluidHopper.HONEY_FLUID.get())
                    .add(BasicFluidHopper.HONEY_FLUID_FLOWING.get());
            getOrCreateTagBuilder(BasicFluidHopper.C_VISUAL_HONEY)
                    .add(BasicFluidHopper.HONEY_FLUID.get())
                    .add(BasicFluidHopper.HONEY_FLUID_FLOWING.get());
            getOrCreateTagBuilder(ConventionalFluidTags.MILK)                    
                    .addOptional(BasicFluidHopperFabric.MILK_FLUID.getId())
                    .addOptional(BasicFluidHopperFabric.MILK_FLUID_FLOWING.getId());
            getOrCreateTagBuilder(BasicFluidHopper.C_VISUAL_MILK)
                    .addOptional(BasicFluidHopperFabric.MILK_FLUID.getId())
                    .addOptional(BasicFluidHopperFabric.MILK_FLUID_FLOWING.getId());
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
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput output) {
            return new RecipeProvider(registryLookup, output) {
                @Override
                public void buildRecipes() {
                    HolderLookup<Item> itemLookup = registryLookup.lookupOrThrow(Registries.ITEM);
                    ShapedRecipeBuilder
                            .shaped(itemLookup, RecipeCategory.MISC, BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get())
                            .define('I', Items.IRON_INGOT)
                            .define('B', Items.BUCKET)
                            .pattern("I I")
                            .pattern("IBI")
                            .pattern(" I ")
                            .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                            .save(output, recipeKey("fluid_hopper"));
                    ShapelessRecipeBuilder.shapeless(
                            itemLookup,
                            RecipeCategory.MISC,
                            BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get())
                            .requires(Items.MINECART)
                            .requires(BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM.get())
                            .unlockedBy(
                                    "has_fluid_hopper_and_minecart",
                                    inventoryTrigger(
                                            ItemPredicate.Builder.item().of(itemLookup,
                                                    BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM.get()),
                                            ItemPredicate.Builder.item().of(itemLookup, Items.MINECART)))
                            .save(
                                    output,
                                    recipeKey("fluid_hopper_minecart"));
                    ShapelessRecipeBuilder.shapeless(itemLookup, RecipeCategory.REDSTONE, Items.HONEY_BLOCK)
                            .requires(BasicFluidHopper.C_HONEY_BUCKETS)
                            .unlockedBy("has_honey_bucket", has(BasicFluidHopper.C_HONEY_BUCKETS))
                            .save(
                                    output,
                                    recipeKey("honey_block_from_bucket"));
                    ShapelessRecipeBuilder
                            .shapeless(itemLookup, RecipeCategory.FOOD, BasicFluidHopper.HONEY_BUCKET.get())
                            .requires(Items.HONEY_BLOCK)
                            .requires(Items.BUCKET)
                            .unlockedBy(getHasName(Items.HONEY_BLOCK), has(Items.HONEY_BLOCK))
                            .save(
                                    output,
                                    recipeKey("honey_bucket_from_block"));
                    ShapelessRecipeBuilder.shapeless(itemLookup, RecipeCategory.FOOD, Items.HONEY_BOTTLE, 3)
                            .requires(BasicFluidHopper.C_HONEY_BUCKETS)
                            .requires(Items.GLASS_BOTTLE, 3)
                            .unlockedBy("has_honey_bucket", has(BasicFluidHopper.C_HONEY_BUCKETS))
                            .save(
                                    output,
                                    recipeKey(
                                            "honey_bottles_from_bucket"));
                    ShapelessRecipeBuilder.shapeless(itemLookup, RecipeCategory.FOOD, Items.HONEY_BOTTLE, 3)
                            .requires(Items.HONEY_BLOCK)
                            .requires(Items.GLASS_BOTTLE, 3)
                            .unlockedBy(getHasName(Items.HONEY_BLOCK), has(Items.HONEY_BLOCK))
                            .save(
                                    output,
                                    recipeKey("honey_bottles_from_block"));
                    ShapelessRecipeBuilder
                            .shapeless(itemLookup, RecipeCategory.FOOD, BasicFluidHopper.HONEY_BUCKET.get())
                            .requires(Items.BUCKET)
                            .requires(Items.HONEY_BOTTLE, 3)
                            .unlockedBy(getHasName(Items.HONEY_BOTTLE), has(Items.HONEY_BOTTLE))
                            .save(
                                    output,
                                    recipeKey(
                                            "honey_bucket_from_bottles"));
                }
            };
        }

        @Override
        public String getName() {
            return "[BasicFluidHopper] RecipeGenerator";
        }

        private ResourceKey<Recipe<?>> recipeKey(String path) {
            return ResourceKey.create(Registries.RECIPE, BasicFluidHopper.id(path));
        }
    }

    private static class BFHModelGenerator extends FabricModelProvider {

        public BFHModelGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
            blockStateModelGenerator.createNonTemplateModelBlock(BasicFluidHopper.HONEY_SOURCE_BLOCK.get());

            MultiVariant multiVariant = BlockModelGenerators
                    .plainVariant(ModelLocationUtils.getModelLocation(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get()));
            MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(
                    ModelLocationUtils.getModelLocation(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get(), "_side"));
            blockStateModelGenerator.registerSimpleFlatItemModel(BasicFluidHopper.BASIC_FLUID_HOPPER_ITEM.get());
            blockStateModelGenerator.blockStateOutput
                    .accept(MultiVariantGenerator.dispatch(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK.get())
                            .with(PropertyDispatch.initial(BlockStateProperties.FACING_HOPPER)
                                    .select(Direction.DOWN, multiVariant).select(Direction.NORTH, multiVariant2)
                                    .select(Direction.EAST, multiVariant2.with(BlockModelGenerators.Y_ROT_90))
                                    .select(Direction.SOUTH, multiVariant2.with(BlockModelGenerators.Y_ROT_180))
                                    .select(Direction.WEST, multiVariant2.with(BlockModelGenerators.Y_ROT_270))));
        }

        @Override
        public void generateItemModels(ItemModelGenerators itemModelGenerator) {
            itemModelGenerator.generateFlatItem(BasicFluidHopper.BASIC_FLUID_HOPPER_MINECART_ITEM.get(),
                    ModelTemplates.FLAT_ITEM);
            itemModelGenerator.generateFlatItem(BasicFluidHopper.HONEY_BUCKET.get(), ModelTemplates.FLAT_ITEM);
            itemModelGenerator.generateFlatItem(BasicFluidHopper.MILK_BOTTLE.get(), ModelTemplates.FLAT_ITEM);
        }
    }
}

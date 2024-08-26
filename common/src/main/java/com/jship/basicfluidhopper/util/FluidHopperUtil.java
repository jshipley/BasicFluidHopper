package com.jship.basicfluidhopper.util;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;

import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidHopperUtil {
    public static FluidStack getFluidFromItem(ItemStack filledContainer) {
        if (filledContainer.getItem() instanceof BucketItem bucket) {
            BasicFluidHopper.LOGGER.info("It's a bucket!");
            return FluidStack.create(bucket.arch$getFluid(), FluidStack.bucketAmount());
        } else if (filledContainer.is(Items.HONEY_BOTTLE) ) {
            return FluidStack.create(BasicFluidHopper.HONEY_FLUID.get(), FluidStack.bucketAmount() / 4);
        } else if (filledContainer.is(Items.POTION) && filledContainer.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
            return FluidStack.create(Fluids.WATER, FluidStack.bucketAmount() / 4);
        }
        return FluidStack.empty();
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getItemFromFluid(FluidStack fluid, ItemStack container) {
        if (container.getItem() instanceof BucketItem bucket || container.getItem() == Items.BUCKET) {
            BasicFluidHopper.LOGGER.info("It's a bucket but not checking fluid");
        } else {
            BasicFluidHopper.LOGGER.info("item? {} - {}", container.getItem(), container.getItem().arch$registryName());
        }
        if (container.getItem() instanceof BucketItem bucket && bucket.arch$getFluid() == Fluids.EMPTY) {
            if (!fluid.isEmpty() && fluid.getAmount() >= FluidStack.bucketAmount()) {
                return new ItemStack(fluid.getFluid().getBucket());
            }
        } else if (container.is(Items.GLASS_BOTTLE)) {
            if (fluid.getFluid().is(BasicFluidHopper.C_HONEY)) {
                return new ItemStack(Items.HONEY_BOTTLE);
            } else if (fluid.getFluid().isSame(Fluids.WATER)) {
                return Items.POTION.getDefaultInstance();
            }
        }
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static BasicFluidHopperBlockEntity getHopperInsertingFluid(Level level, BlockPos pos, BlockState blockState,
            BlockEntity blockEntity, boolean burnable, @Nullable ItemStack item) {
        
        if (level == null || level.isClientSide()) {
            return null;
        }

        // Randomize the direction to look for hoppers, so that pulling from hoppers
        // will be somewhat distributed
        for (Direction direction : Direction.allShuffled(level.getRandom())) {
            BasicFluidHopperBlockEntity hopperEntity;

            // Look for a fluid hopper that is facing blockEntity and is not empty
            if (level != null && !level.isClientSide()
                    && level.getBlockEntity(pos.relative(direction)) instanceof BasicFluidHopperBlockEntity ent
                    && ent.getFacing() == direction.getOpposite()
                    && !ent.getFluidStorage().isEmpty()) {
                hopperEntity = ent;
            } else {
                continue;
            }

            Optional<FluidStack> hopperFluid = hopperEntity.getFluidStorage().getFluidStack();
            if (item == null && hopperFluid.isPresent() && burnable
                    && hopperFluid.get().getFluid().is(BasicFluidHopper.C_FLUID_FUEL)
                    && hopperFluid.get().getAmount() >= (long)(BasicFluidHopperConfig.FUEL_CONSUME_STEP * FluidStack.bucketAmount())) {
                // Enough fuel to power a furnace for a bit longer was found in this hopper
                return hopperEntity;
            } else if (item.is(Items.GLASS_BOTTLE)
                    && hopperEntity.getFluidStorage().getAmount() >= FluidStack.bucketAmount() / 4
                    && (hopperFluid.get().getFluid().isSame(Fluids.WATER) || hopperFluid.get().getFluid().is(BasicFluidHopper.C_HONEY))) {
                // Enough fluid that can go in a bottle was found in this hopper
                // TODO support other mods that add additional bottled fluids
                return hopperEntity;
            } else if (item.getItem() instanceof BucketItem bucket && bucket.arch$getFluid() == Fluids.EMPTY
                    && hopperFluid.get().getFluid().getBucket() != null
                    && hopperFluid.get().getAmount() >= FluidStack.bucketAmount()) {
                // Enough fluid that can go in a bucket was found in this hopper
                return hopperEntity;
            }
        }
        // No hopper found, carry on as usual
        return null;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack fillDispenserItemFromHopper(ItemStack emptyItem, BlockSource dispenser) {
        ServerLevel level = dispenser.level();
        BlockPos pos = dispenser.pos();
        if (emptyItem.is(Items.GLASS_BOTTLE)) {
            BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(level, pos, level.getBlockState(pos), level.getBlockEntity(pos), false, emptyItem);
            if (fluidHopper == null) {
                return new ItemStack(Items.AIR);
            }
            long bottleAmount = FluidStack.bucketAmount() / 4;
            if (fluidHopper.getFluidStorage().getFluid().isSame(Fluids.WATER) && fluidHopper.getFluidStorage().remove(bottleAmount, true)  == bottleAmount) {
                    fluidHopper.getFluidStorage().remove(bottleAmount, false);
                    return PotionContents.createItemStack(Items.POTION, Potions.WATER);
            } else if (fluidHopper.getFluidStorage().getFluid().is(BasicFluidHopper.C_HONEY) && fluidHopper.getFluidStorage().remove(bottleAmount, true) == bottleAmount) {
                fluidHopper.getFluidStorage().remove(bottleAmount, false);
                return new ItemStack(Items.HONEY_BOTTLE);
            }            
        }
        if (emptyItem.getItem() instanceof BucketItem bucket && bucket.arch$getFluid() == Fluids.EMPTY) {
            BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(level, pos, level.getBlockState(pos), level.getBlockEntity(pos), false, emptyItem);
            if (fluidHopper == null) {
                return new ItemStack(Items.AIR);
            }
            Item bucketItem = fluidHopper.getFluidStorage().getFluid().getBucket();
            if (bucketItem != Items.AIR && fluidHopper.getFluidStorage().remove(FluidStack.bucketAmount(), true) == FluidStack.bucketAmount()) {
                fluidHopper.getFluidStorage().remove(FluidStack.bucketAmount(), false);
                return new ItemStack(bucketItem);
            }
        }
        return new ItemStack(Items.AIR);
    }
}

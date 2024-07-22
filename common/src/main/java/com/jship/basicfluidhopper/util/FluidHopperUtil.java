package com.jship.basicfluidhopper.util;

import org.jetbrains.annotations.Nullable;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;

import earth.terrarium.common_storage_lib.resources.fluid.FluidResource;
import earth.terrarium.common_storage_lib.resources.fluid.util.FluidAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class FluidHopperUtil {
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
                    && ent.isFacing(direction.getOpposite())
                    && !ent.isEmpty()) {
                hopperEntity = ent;
            } else {
                continue;
            }

            FluidResource hopperFluid = hopperEntity.fluidStorage.getResource(0);
            if (item == null && burnable
                    && hopperEntity.fluidStorage.getResource(0).is(BasicFluidHopper.C_FLUID_FUEL)
                    && hopperEntity.fluidStorage.getAmount(0) >= BasicFluidHopper.FUEL_CONSUME_STEP) {
                // Enough fuel to power a furnace for a bit longer was found in this hopper
                return hopperEntity;
            } else if (item.is(Items.GLASS_BOTTLE)
                    && hopperEntity.fluidStorage.getAmount(0) >= FluidAmounts.BOTTLE
                    && (hopperFluid.isOf(Fluids.WATER) || hopperFluid.is(BasicFluidHopper.C_HONEY))) {
                // Enough fluid that can go in a bottle was found in this hopper
                // TODO support other mods that add additional bottled fluids
                return hopperEntity;
            } else if (item.is(Items.BUCKET)
                    && hopperFluid.getType().getBucket() != null
                    && hopperEntity.fluidStorage.getAmount(0) >= FluidAmounts.BUCKET) {
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
            if (fluidHopper.fluidStorage.getResource(0).isOf(Fluids.WATER) && BasicFluidHopperBlockEntity.extract(fluidHopper, FluidAmounts.BOTTLE)) {
                return PotionContents.createItemStack(Items.POTION, Potions.WATER);
            } else if (fluidHopper.fluidStorage.getResource(0).getType().is(BasicFluidHopper.C_HONEY) && BasicFluidHopperBlockEntity.extract(fluidHopper, FluidAmounts.BOTTLE)) {
                return new ItemStack(Items.HONEY_BOTTLE);
            }            
        }
        if (emptyItem.is(Items.BUCKET)) {
            BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(level, pos, level.getBlockState(pos), level.getBlockEntity(pos), false, emptyItem);
            if (fluidHopper == null) {
                return new ItemStack(Items.AIR);
            }
            Item bucketItem = fluidHopper.fluidStorage.getResource(0).getType().getBucket();
            if (bucketItem != Items.AIR && BasicFluidHopperBlockEntity.extract(fluidHopper, FluidAmounts.BUCKET)) {
                return new ItemStack(bucketItem);
            }
        }
        return new ItemStack(Items.AIR);
    }
}

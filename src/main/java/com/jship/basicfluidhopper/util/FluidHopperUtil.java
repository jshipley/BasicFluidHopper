package com.jship.basicfluidhopper.util;

import org.jetbrains.annotations.Nullable;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidHopperUtil {
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
                    && ent.isFacing(direction.getOpposite())
                    && !ent.fluidStorage.isResourceBlank()) {
                hopperEntity = ent;
            } else {
                continue;
            }

            Fluid hopperFluid = hopperEntity.fluidStorage.getResource().getFluid();
            if (item == null && burnable
                    && hopperEntity.fluidStorage.getResource().getFluid().is(BasicFluidHopper.C_FLUID_FUEL)
                    && hopperEntity.fluidStorage.getAmount() >= BasicFluidHopper.FUEL_CONSUME_STEP) {
                // Enough fuel to power a furnace for a bit longer was found in this hopper
                return hopperEntity;
            } else if (item != null && item.is(Items.GLASS_BOTTLE)
                    && hopperEntity.fluidStorage.getAmount() >= FluidConstants.BOTTLE
                    && (hopperFluid.isSame(Fluids.WATER) || hopperFluid.is(BasicFluidHopper.C_HONEY))) {
                // Enough fluid that can go in a bottle was found in this hopper
                // TODO support other mods that add additional bottled fluids
                return hopperEntity;
            } else if (item != null && item.is(Items.BUCKET)
                    && hopperFluid.getBucket() != null
                    && hopperEntity.fluidStorage.getAmount() >= FluidConstants.BUCKET) {
                // Enough fluid that can go in a bucket was found in this hopper
                return hopperEntity;
            }
        }
        // No hopper found, carry on as usual
        return null;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack fillDispenserItemFromHopper(ItemStack emptyItem, BlockSource dispenser) {
        ServerLevel level = dispenser.getLevel();
        BlockPos pos = dispenser.getPos();
        if (emptyItem.is(Items.GLASS_BOTTLE)) {
            BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(level, pos, level.getBlockState(pos), level.getBlockEntity(pos), false, emptyItem);
            if (fluidHopper == null) {
                return new ItemStack(Items.AIR);
            }
            if (fluidHopper.fluidStorage.getResource().isOf(Fluids.WATER) && BasicFluidHopperBlockEntity.extract(fluidHopper, FluidConstants.BOTTLE)) {
                return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
            } else if (fluidHopper.fluidStorage.getResource().getFluid().is(BasicFluidHopper.C_HONEY) && BasicFluidHopperBlockEntity.extract(fluidHopper, FluidConstants.BOTTLE)) {
                return new ItemStack(Items.HONEY_BOTTLE);
            }            
        }
        if (emptyItem.is(Items.BUCKET)) {
            BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(level, pos, level.getBlockState(pos), level.getBlockEntity(pos), false, emptyItem);
            if (fluidHopper == null) {
                return new ItemStack(Items.AIR);
            }
            Item bucketItem = fluidHopper.fluidStorage.getResource().getFluid().getBucket();
            if (bucketItem != Items.AIR && BasicFluidHopperBlockEntity.extract(fluidHopper, FluidConstants.BUCKET)) {
                return new ItemStack(bucketItem)                ;
            }
        }
        return new ItemStack(Items.AIR);
    }
}

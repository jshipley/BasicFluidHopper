package com.jship.basicfluidhopper.util;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;
import dev.architectury.fluid.FluidStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public abstract class FluidHopperUtil {

    @SuppressWarnings("deprecation")
    @Nullable
    public static BasicFluidHopperBlockEntity getHopperInsertingFluid(
        Level level,
        BlockPos pos,
        BlockState blockState,
        BlockEntity blockEntity,
        boolean burnable,
        @Nullable ItemStack item
    ) {
        if (level == null || level.isClientSide()) {
            return null;
        }

        // Randomize the direction to look for hoppers, so that pulling from hoppers
        // will be somewhat distributed
        for (Direction direction : Direction.allShuffled(level.getRandom())) {
            BasicFluidHopperBlockEntity hopperEntity;

            // Look for a fluid hopper that is facing blockEntity and is not empty
            if (
                level != null &&
                !level.isClientSide() &&
                level.getBlockEntity(pos.relative(direction)) instanceof BasicFluidHopperBlockEntity ent &&
                ent.getFacing() == direction.getOpposite() &&
                !ent.getFluidStorage().getFluidInTank(0).isEmpty()
            ) {
                hopperEntity = ent;
            } else {
                continue;
            }

            FluidStack hopperFluid = hopperEntity.getFluidStorage().getFluidInTank(0);
            if (
                item == null &&
                !hopperFluid.isEmpty() &&
                burnable &&
                hopperFluid.getFluid().is(BasicFluidHopper.C_FLUID_FUEL) &&
                hopperFluid.getAmount() >=
                (long) (BasicFluidHopperConfig.fuelConsumeStep() * FluidStack.bucketAmount())
            ) {
                // Enough fuel to power a furnace for a bit longer was found in this hopper
                return hopperEntity;
            } else if (
                item != null &&
                item.is(Items.GLASS_BOTTLE) &&
                hopperFluid.getAmount() >= FluidStack.bucketAmount() / 4 &&
                (hopperFluid.getFluid().isSame(Fluids.WATER) ||
                    hopperFluid.getFluid().is(BasicFluidHopper.C_HONEY))
            ) {
                // Enough fluid that can go in a bottle was found in this hopper
                // TODO support other mods that add additional bottled fluids
                return hopperEntity;
            } else if (
                item != null &&
                item.getItem() instanceof BucketItem bucket &&
                bucket.arch$getFluid() == Fluids.EMPTY &&
                hopperFluid.getFluid().getBucket() != null &&
                hopperFluid.getAmount() >= FluidStack.bucketAmount()
            ) {
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
            BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(
                level,
                pos,
                level.getBlockState(pos),
                level.getBlockEntity(pos),
                false,
                emptyItem
            );
            if (fluidHopper == null) {
                return new ItemStack(Items.AIR);
            }
            long bottleAmount = FluidStack.bucketAmount() / 4;
            if (
                fluidHopper.getFluidStorage().getFluidInTank(0).getFluid().isSame(Fluids.WATER) &&
                fluidHopper.getFluidStorage().drain(bottleAmount, true).getAmount() == bottleAmount
            ) {
                fluidHopper.getFluidStorage().drain(bottleAmount, false);
                return PotionContents.createItemStack(Items.POTION, Potions.WATER);
            } else if (
                // TODO use honey tag
                fluidHopper.getFluidStorage().getFluidInTank(0).getFluid().is(BasicFluidHopper.C_HONEY) &&
                fluidHopper.getFluidStorage().drain(bottleAmount, true).getAmount() == bottleAmount
            ) {
                fluidHopper.getFluidStorage().drain(bottleAmount, false);
                return new ItemStack(Items.HONEY_BOTTLE);
            }
        }
        if (emptyItem.getItem() instanceof BucketItem bucket && bucket.arch$getFluid() == Fluids.EMPTY) {
            BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(
                level,
                pos,
                level.getBlockState(pos),
                level.getBlockEntity(pos),
                false,
                emptyItem
            );
            if (fluidHopper == null) {
                return new ItemStack(Items.AIR);
            }
            Item bucketItem = fluidHopper.getFluidStorage().getFluidInTank(0).getFluid().getBucket();
            if (
                bucketItem != Items.AIR &&
                fluidHopper.getFluidStorage().drain(FluidStack.bucketAmount(), true).getAmount() == FluidStack.bucketAmount()
            ) {
                fluidHopper.getFluidStorage().drain(FluidStack.bucketAmount(), false);
                return new ItemStack(bucketItem);
            }
        }
        return new ItemStack(Items.AIR);
    }
}

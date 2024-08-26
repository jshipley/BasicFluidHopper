package com.jship.basicfluidhopper.fluid;

import java.util.List;
import java.util.Optional;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;

import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

public interface FluidHopper {
    /**
     * Call markDirty for the entity
     */
    public abstract void markDirty();

    /**
     * @return a fluid storage for the fluid hopper
     */
    public abstract HopperFluidStorage getFluidStorage();

    public abstract void setTransferCooldown(int transferCooldown);

    public abstract boolean needsCooldown();

    public abstract boolean isEnabled();

    public abstract Direction getFacing();

    /**
     * Drain fluid storage (or source blocks) from above, fill facing storages
     * @param level the game world
     * @param pos the hopper's position
     * @param state the hopper's state
     * @param fluidHopperEntity the hopper's entity
     * @return
     */
    public static boolean fillAndDrain(Level level, BlockPos pos, BlockState state, FluidHopper fluidHopper) {
        if (level.isClientSide) return false;
        if (!fluidHopper.needsCooldown() && fluidHopper.isEnabled()) {
            if ((!fluidHopper.getFluidStorage().isEmpty() && FluidHopper.fill(level, pos, fluidHopper))
                || (!fluidHopper.getFluidStorage().isFull() && FluidHopper.drain(level, pos, fluidHopper))) {
                // fill or drain succeeded
                fluidHopper.setTransferCooldown(BasicFluidHopperConfig.TRANSFER_COOLDOWN);
                fluidHopper.markDirty();
                return true;
            }
        }
        return false;
    }

    /**
     * Reduce the fluid stored in the hopper without inserting it anywhere
     * Could be used for filling furnaces or dispensers
     * @param blockEntity
     * @param amount
     * @return
     */
    public static boolean remove(FluidHopper fluidHopper, long amount) {
        if (fluidHopper.getFluidStorage().isEmpty()) {
            return false;
        }
        if (fluidHopper.getFluidStorage().remove(amount, true) == amount) {
            fluidHopper.getFluidStorage().remove(amount, false);
            return true;
        }
        return false;
    }

    /**
     * Get all of the vehicle entities in the block
     * @param level the game world
     * @param pos the block position to search
     * @return a list of vehicle entities
     */
    public static List<VehicleEntity> getVehicles(Level level, BlockPos pos) {
        return level.getEntities((Entity) null, new AABB(
            pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)).stream()
                .filter((entity) -> entity instanceof VehicleEntity)
                .map((entity) -> (VehicleEntity)entity)
                .toList();
    }

    /**
     * Fill the fluid storage of the block or entity the hopper is facing
     * @param level the game world
     * @param pos the hopper position
     * @param hopperEntity the hopper entity
     * @return whether the hopper was able to fill a fluid storage of a block/entity
     */
    private static boolean fill(Level level, BlockPos pos, FluidHopper fluidHopper) {
        // Try to fill a facing block
        long filled = fluidHopper.getFluidStorage().fillBlockPos(level, pos.relative(fluidHopper.getFacing()), fluidHopper.getFacing(), true);
        if (filled > 0) {
            fluidHopper.getFluidStorage().fillBlockPos(level, pos.relative(fluidHopper.getFacing()), fluidHopper.getFacing(), false);
            return true;
        }

        // Try to fill any vehicle in the facing block
        for (VehicleEntity vehicle : getVehicles(level, pos.relative(fluidHopper.getFacing()))) {
            filled = fluidHopper.getFluidStorage().fillVehicle(level, vehicle, true);
            if (filled > 0) {
                fluidHopper.getFluidStorage().fillVehicle(level, vehicle, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Drain the fluid storage (or pseudo storage) of any block or vehicle above the hopper
     * @param level the game world
     * @param pos the hopper position
     * @param hopperEntity the hopper entity
     * @return
     */
    public static boolean drain(Level level, BlockPos pos, FluidHopper fluidHopper) {
        // Try to drain a fluid storage above
        long drained = fluidHopper.getFluidStorage().drainBlockPos(level, pos.above(), true);
        if (drained > 0) {
            fluidHopper.getFluidStorage().drainBlockPos(level, pos.above(), false);
            return true;
        }

        BlockState aboveState = level.getBlockState(pos.above());

        if (aboveState.getBlock() instanceof BeehiveBlock) {
            int honey_level = BeehiveBlockEntity.getHoneyLevel(aboveState);
            if (honey_level > 0) {
                long bottleAmount = FluidStack.bucketAmount() / 4;
                FluidStack honey = FluidStack.create(BasicFluidHopper.HONEY_FLUID.get(), bottleAmount);
                long added = fluidHopper.getFluidStorage().add(honey, bottleAmount, true);
                if (added == bottleAmount) {
                    fluidHopper.getFluidStorage().add(honey, bottleAmount, false);
                    level.setBlockAndUpdate(pos.above(), aboveState.setValue(BeehiveBlock.HONEY_LEVEL, honey_level - 1));
                    return true;
                }
            }
        }

        // try to drain a source block above the hopper
        if (aboveState.getBlock() instanceof BucketPickup bucketPickup) {
            FluidState aboveFluidState = aboveState.getFluidState();
            if (aboveFluidState.isSource()) {
                FluidStack fluid = FluidStack.create(aboveFluidState.getType(), FluidStack.bucketAmount());
                long inserted = fluidHopper.getFluidStorage().add(fluid, FluidStack.bucketAmount(), true);
                if (inserted == FluidStack.bucketAmount()) {
                    ItemStack bucket = bucketPickup.pickupBlock(null, level, pos.above(), aboveState);
                    if (!bucket.isEmpty()) {
                        fluidHopper.getFluidStorage().add(fluid, FluidStack.bucketAmount(), false);
                        return true;
                    }
                }
            }
        }

        // Try to drain any vehicle in the above block
        for (VehicleEntity vehicle : FluidHopper.getVehicles(level, pos.above())) {
            drained = fluidHopper.getFluidStorage().drainVehicle(level, vehicle, true);
            if (drained > 0) {
                fluidHopper.getFluidStorage().drainVehicle(level, vehicle, false);
                return true;
            }
        }

        return false;
    }

    public static boolean tryFillBucket(ItemStack item, Level level, BlockPos pos, Player player, InteractionHand hand, HopperFluidStorage fluidStorage) {
        if (fluidStorage.isEmpty()) return false;
        Optional<FluidStack> fluid = fluidStorage.getFluidStack();
        if (!fluid.isPresent()) return false;

        long removed = fluidStorage.remove(FluidStack.bucketAmount(), true);
        if (removed != FluidStack.bucketAmount()) return false;

        Item bucket = fluid.get().getFluid().getBucket();
        ItemStack emptyBucket = player.getItemInHand(hand);
        if (bucket != Items.AIR && emptyBucket.is(Items.BUCKET)) {
            ItemStack bucketStack = new ItemStack(bucket);
            bucketStack.applyComponents(emptyBucket.getComponents());
            player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, bucketStack));
            SoundEvent pickupSound;
            if (bucket == Items.LAVA_BUCKET) {
                pickupSound = SoundEvents.BUCKET_FILL_LAVA;
            } else if (bucket == BasicFluidHopper.HONEY_BUCKET.get()) {
                pickupSound = SoundEvents.BEEHIVE_DRIP;
            } else {
                pickupSound = SoundEvents.BUCKET_FILL;
            }
            level.playSound(null, pos, pickupSound, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            fluidStorage.remove(FluidStack.bucketAmount(), false);
            return true;
        }
        return false;
    }

    public static boolean tryDrainBucket(ItemStack item, Level level, BlockPos pos, Player player, InteractionHand hand, HopperFluidStorage fluidStorage) {
        if (!(item.getItem() instanceof BucketItem bucketItem)) return false;
        
        FluidStack fluid = FluidStack.create(bucketItem.arch$getFluid(), FluidStack.bucketAmount());
        if (fluid.isEmpty()) return false;
        
        long inserted = fluidStorage.add(fluid, FluidStack.bucketAmount(), true);
        if (inserted != FluidStack.bucketAmount()) return false;

        SoundEvent bucketEmpty;
        if (item.is(Items.LAVA_BUCKET)) {
            bucketEmpty = SoundEvents.BUCKET_EMPTY_LAVA;
        } else if (item.is(BasicFluidHopper.HONEY_BUCKET.get())) {
            bucketEmpty = SoundEvents.BEEHIVE_DRIP;
        } else {
            bucketEmpty = SoundEvents.BUCKET_EMPTY;
        }
        level.playSound(null, pos, bucketEmpty, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        
        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
        emptyBucket.applyComponents(bucketItem.components());
        player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, emptyBucket));
        fluidStorage.add(fluid, FluidStack.bucketAmount(), false);
        
        return true;
    }
}

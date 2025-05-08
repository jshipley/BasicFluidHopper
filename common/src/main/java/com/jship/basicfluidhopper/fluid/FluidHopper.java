package com.jship.basicfluidhopper.fluid;

import java.util.List;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;
import com.jship.spiritapi.api.fluid.SpiritFluidStorage;
import com.jship.spiritapi.api.fluid.SpiritFluidUtil;

import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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
    public abstract SpiritFluidStorage getFluidStorage();

    public abstract void setTransferCooldown(int transferCooldown);

    public abstract boolean needsCooldown();

    public abstract boolean isEnabled();

    public abstract Direction getFacing();

    /**
     * Drain fluid storage (or source blocks) from above, fill facing storages
     * 
     * @param level             the game world
     * @param pos               the hopper's position
     * @param state             the hopper's state
     * @param fluidHopperEntity the hopper's entity
     * @return
     */
    public static boolean fillAndDrain(Level level, BlockPos pos, BlockState state, FluidHopper fluidHopper) {
        if (level.isClientSide)
            return false;
        if (!fluidHopper.needsCooldown() && fluidHopper.isEnabled()) {
            if ((!fluidHopper.getFluidStorage().getFluidInTank(0).isEmpty()
                    && FluidHopper.fill(level, pos, fluidHopper)) ||
                    ((fluidHopper.getFluidStorage().getFluidInTank(0).getAmount() < fluidHopper.getFluidStorage()
                            .getTankCapacity(0)) && FluidHopper.drain(level, pos, Direction.UP, fluidHopper))) {
                // fill or drain succeeded
                fluidHopper.setTransferCooldown(BasicFluidHopperConfig.transferCooldown());
                fluidHopper.markDirty();
                return true;
            }
        }
        return false;
    }

    public static ItemInteractionResult useFluidItem(
            Level level,
            Player player,
            InteractionHand hand,
            FluidHopper fluidHopper) {
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;

        ItemStack item = player.getItemInHand(hand);
        if (SpiritFluidUtil.isFluidItem(item)) {
            FluidStack playerFluid = SpiritFluidUtil.getFluidFromItem(item);
            FluidStack storageFluid = fluidHopper.getFluidStorage().getFluidInTank(0);

            // item is empty
            if (playerFluid.isEmpty()) {
                if (SpiritFluidUtil.fillItem(fluidHopper.getFluidStorage(), player, hand, true)) {
                    SpiritFluidUtil.fillItem(fluidHopper.getFluidStorage(), player, hand, false);
                    return ItemInteractionResult.SUCCESS;
                }
                // hopper can hold more
            } else if (storageFluid.getAmount() < fluidHopper.getFluidStorage().getTankCapacity(0)) {
                if (SpiritFluidUtil.drainItem(fluidHopper.getFluidStorage(), player, hand, true)) {
                    SpiritFluidUtil.drainItem(fluidHopper.getFluidStorage(), player, hand, false);
                    return ItemInteractionResult.SUCCESS;
                }
                // hopper is full, try to drain it
                // this will fail for buckets/bottles, but could still succeed for tanks
            } else if (storageFluid.getAmount() >= fluidHopper.getFluidStorage().getTankCapacity(0)) {
                if (SpiritFluidUtil.fillItem(fluidHopper.getFluidStorage(), player, hand, true)) {
                    SpiritFluidUtil.fillItem(fluidHopper.getFluidStorage(), player, hand, false);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            // It is a fluid item that should interact with the hopper, but none of the
            // available actions succeeded.
            return ItemInteractionResult.CONSUME;
        }

        // Not a fluid item, let the block handle the rest
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Reduce the fluid stored in the hopper without inserting it anywhere
     * Could be used for filling furnaces or dispensers
     * 
     * @param blockEntity
     * @param amount
     * @return
     */
    public static boolean remove(FluidHopper fluidHopper, long amount) {
        if (fluidHopper.getFluidStorage().getFluidInTank(0).isEmpty()) {
            return false;
        }
        if (fluidHopper.getFluidStorage().drain(amount, true).getAmount() == amount) {
            fluidHopper.getFluidStorage().drain(amount, false);
            return true;
        }
        return false;
    }

    /**
     * Get all of the vehicle entities in the block
     * 
     * @param level the game world
     * @param pos   the block position to search
     * @return a list of vehicle entities
     */
    public static List<VehicleEntity> getVehicles(Level level, BlockPos pos) {
        return level
                .getEntities(
                        (Entity) null,
                        new AABB(
                                pos.getX() - 0.5,
                                pos.getY() - 0.5,
                                pos.getZ() - 0.5,
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5))
                .stream()
                .filter(entity -> entity instanceof VehicleEntity)
                .map(entity -> (VehicleEntity) entity)
                .toList();
    }

    /**
     * Fill the fluid storage of the block or entity the hopper is facing
     * 
     * @param level        the game world
     * @param pos          the hopper position
     * @param hopperEntity the hopper entity
     * @return whether the hopper was able to fill a fluid storage of a block/entity
     */
    private static boolean fill(Level level, BlockPos pos, FluidHopper fluidHopper) {
        // Try to fill a facing block
        long filled = SpiritFluidUtil.fillBlockPos(
                fluidHopper.getFluidStorage(), level, pos.relative(fluidHopper.getFacing()), fluidHopper.getFacing(),
                true);
        if (filled > 0) {
            SpiritFluidUtil.fillBlockPos(
                    fluidHopper.getFluidStorage(), level, pos.relative(fluidHopper.getFacing()),
                    fluidHopper.getFacing(), false);
            return true;
        }

        // Try to fill any vehicle in the facing block
        for (VehicleEntity vehicle : getVehicles(level, pos.relative(fluidHopper.getFacing()))) {
            filled = SpiritFluidUtil.fillVehicle(fluidHopper.getFluidStorage(), level, vehicle, true);
            if (filled > 0) {
                SpiritFluidUtil.fillVehicle(fluidHopper.getFluidStorage(), level, vehicle, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Drain the fluid storage (or pseudo storage) of any block or vehicle above the
     * hopper
     * 
     * @param level        the game world
     * @param pos          the hopper position
     * @param facing       the direction of the block the hopper is trying to drain
     * @param hopperEntity the hopper entity
     * @return
     */
    public static boolean drain(Level level, BlockPos pos, Direction facing, FluidHopper fluidHopper) {
        // Try to drain a fluid storage above
        long drained = SpiritFluidUtil.drainBlockPos(fluidHopper.getFluidStorage(), level, pos.above(), facing, true);
        if (drained > 0) {
            SpiritFluidUtil.drainBlockPos(fluidHopper.getFluidStorage(), level, pos.above(), facing, false);
            return true;
        }

        BlockState aboveState = level.getBlockState(pos.above());

        if (aboveState.getBlock() instanceof BeehiveBlock) {
            int honey_level = BeehiveBlockEntity.getHoneyLevel(aboveState);
            if (honey_level > 0) {
                long bottleAmount = FluidStack.bucketAmount() / 4;
                FluidStack honey = FluidStack.create(BasicFluidHopper.HONEY_FLUID.get(), bottleAmount);
                long added = fluidHopper.getFluidStorage().fill(honey, true);
                if (added == bottleAmount) {
                    fluidHopper.getFluidStorage().fill(honey, false);
                    level.setBlockAndUpdate(
                            pos.above(),
                            aboveState.setValue(BeehiveBlock.HONEY_LEVEL, honey_level - 1));
                    return true;
                }
            }
        }

        // try to drain a source block above the hopper
        if (aboveState.getBlock() instanceof BucketPickup bucketPickup) {
            FluidState aboveFluidState = aboveState.getFluidState();
            if (aboveFluidState.isSource()) {
                FluidStack fluid = FluidStack.create(aboveFluidState.getType(), FluidStack.bucketAmount());
                long inserted = fluidHopper.getFluidStorage().fill(fluid, true);
                if (inserted == FluidStack.bucketAmount()) {
                    ItemStack bucket = bucketPickup.pickupBlock(null, level, pos.above(), aboveState);
                    if (!bucket.isEmpty()) {
                        fluidHopper.getFluidStorage().fill(fluid, false);
                        return true;
                    }
                }
            }
        }

        // Try to drain any vehicle in the above block
        for (VehicleEntity vehicle : FluidHopper.getVehicles(level, pos.above())) {
            drained = SpiritFluidUtil.drainVehicle(fluidHopper.getFluidStorage(), level, vehicle, true);
            if (drained > 0) {
                SpiritFluidUtil.drainVehicle(fluidHopper.getFluidStorage(), level, vehicle, false);
                return true;
            }
        }

        return false;
    }

    public static boolean tryFillBucket(
            ItemStack item,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            SpiritFluidStorage fluidStorage) {
        FluidStack fluid = fluidStorage.getFluidInTank(0);
        if (fluid.isEmpty())
            return false;

        FluidStack removed = fluidStorage.drain(FluidStack.bucketAmount(), true);
        if (removed.getAmount() != FluidStack.bucketAmount())
            return false;

        Item bucket = fluid.getFluid().getBucket();
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
            fluidStorage.drain(FluidStack.bucketAmount(), false);
            return true;
        }
        return false;
    }

    public static boolean tryDrainBucket(
            ItemStack item,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            SpiritFluidStorage fluidStorage) {
        if (!(item.getItem() instanceof BucketItem bucketItem))
            return false;

        FluidStack fluid = FluidStack.create(bucketItem.arch$getFluid(), FluidStack.bucketAmount());
        if (fluid.isEmpty())
            return false;

        long inserted = fluidStorage.fill(fluid, true);
        if (inserted != FluidStack.bucketAmount())
            return false;

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
        fluidStorage.fill(FluidStack.create(fluid, FluidStack.bucketAmount()), false);

        return true;
    }
}

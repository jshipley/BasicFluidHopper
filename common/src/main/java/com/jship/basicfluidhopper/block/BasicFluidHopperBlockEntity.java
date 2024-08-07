package com.jship.basicfluidhopper.block;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;

import earth.terrarium.common_storage_lib.fluid.FluidApi;
import earth.terrarium.common_storage_lib.fluid.impl.SimpleFluidStorage;
import earth.terrarium.common_storage_lib.fluid.util.FluidProvider;
import earth.terrarium.common_storage_lib.resources.fluid.FluidResource;
import earth.terrarium.common_storage_lib.resources.fluid.util.FluidAmounts;
import earth.terrarium.common_storage_lib.storage.base.CommonStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

public class BasicFluidHopperBlockEntity extends BlockEntity implements FluidProvider.BlockEntity {
    public static final int TRANSFER_COOLDOWN = 8;
    public static final int BUCKET_CAPACITY = 1;
    public final SimpleFluidStorage fluidStorage = new SimpleFluidStorage(this, BasicFluidHopper.FLUID_CONTENTS, 1, FluidAmounts.BUCKET);
    private int transferCooldown = -1;
    private Direction facing;

    public BasicFluidHopperBlockEntity(BlockPos pos, BlockState state) {
        super(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(), pos, state);
        this.facing = state.getValue(BasicFluidHopperBlock.FACING);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        // fluidStorage should be managed by FLUID_CONTENTS data manager
        this.transferCooldown = nbt.getInt("TransferCooldown");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        // fluidStorage should be managed by FLUID_CONTENTS data manager
        nbt.putInt("TransferCooldown", this.transferCooldown);
    }

    public static void pushItemsTick(Level level, BlockPos pos, BlockState state, BasicFluidHopperBlockEntity blockEntity) {
        if (blockEntity.transferCooldown > 0) {
            --blockEntity.transferCooldown;
        }
        if (!blockEntity.needsCooldown()) {
            BasicFluidHopperBlockEntity.insertAndExtract(level, pos, state, blockEntity,
                    () -> BasicFluidHopperBlockEntity.extract(level, pos, blockEntity));
        }
    }

    private static boolean insertAndExtract(Level level, BlockPos pos, BlockState state,
            BasicFluidHopperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
        if (level.isClientSide) {
            return false;
        }
        if (!blockEntity.needsCooldown() && state.getValue(BasicFluidHopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            if (!blockEntity.isEmpty()) {
                // try to insert
                bl = BasicFluidHopperBlockEntity.insert(level, pos, blockEntity);
            }
            if (!blockEntity.isFull()) {
                // try to extract
                bl |= booleanSupplier.getAsBoolean();
            }
            if (bl) {
                // insert or extract succeeded
                blockEntity.setTransferCooldown(TRANSFER_COOLDOWN);
                BasicFluidHopperBlockEntity.setChanged(level, pos, state);
                return true;
            }
        }
        return false;
    }

    public boolean isFull() {
        FluidResource resource = fluidStorage.getResource(0);
        return !resource.isBlank() && fluidStorage.getAmount(0) >= fluidStorage.getLimit(0, resource);
    }

    public boolean isEmpty() {
        FluidResource resource = fluidStorage.getResource(0);
        return resource.isBlank() || fluidStorage.getAmount(0) == 0;
    }

    // Decrease the stored fluid
    // Could be used when fluid is used as furnace fuel or used to fill bottles
    public static boolean extract(BasicFluidHopperBlockEntity blockEntity, long amount) {
        if (blockEntity.isEmpty()) {
            return false;
        }
        if (blockEntity.fluidStorage.extract(blockEntity.fluidStorage.getResource(0), amount, true) == amount) {
            blockEntity.fluidStorage.extract(blockEntity.fluidStorage.getResource(0), amount, false);
            return true;
        }
        return false;
    }

    private static boolean insert(Level level, BlockPos pos, BasicFluidHopperBlockEntity blockEntity) {
        SimpleFluidStorage inputFluidStorage = blockEntity.fluidStorage;
        CommonStorage<FluidResource> outputFluidStorage = BasicFluidHopperBlockEntity.getOutputFluidStorage(level, pos,
                blockEntity);
        if (outputFluidStorage == null || blockEntity.isEmpty()) {
            return false;
        }
        FluidResource inputFluid = inputFluidStorage.getResource(0);
        long inserted = outputFluidStorage.insert(inputFluid, Math.min(inputFluidStorage.getAmount(0), FluidAmounts.BUCKET), true);
        long extracted = inputFluidStorage.extract(inputFluid, inserted, true);
        if (inserted == extracted) {
            outputFluidStorage.insert(inputFluid, Math.min(inputFluidStorage.getAmount(0), FluidAmounts.BUCKET), false);
            inputFluidStorage.extract(inputFluid, inserted, false);
            return true;
        }
        return false;
    }

    public boolean isFacing(Direction direction) {
        return direction == facing;
    }

    public static boolean extract(Level level, BlockPos pos, BasicFluidHopperBlockEntity blockEntity) {
        CommonStorage<FluidResource> inputFluidStorage = getInputFluidStorage(level, pos);
        SimpleFluidStorage outputFluidStorage = blockEntity.fluidStorage;
        if (inputFluidStorage != null) {
            return extract(inputFluidStorage, outputFluidStorage);
        }

        BlockState aboveBlockState = level.getBlockState(pos.above());
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        if (aboveFluidState.isSource() || aboveBlockState.getBlock() instanceof BeehiveBlock) {
            return extract(level, pos, outputFluidStorage, aboveBlockState);
        }

        return false;
    }

    public static boolean extract(Level level, BasicFluidHopperMinecartEntity vehicleEntity) {
        CommonStorage<FluidResource> inputFluidStorage = getInputFluidStorage(level, vehicleEntity.blockPosition());
        SimpleFluidStorage outputFluidStorage = vehicleEntity.fluidStorage;
        if (inputFluidStorage != null) {
            return extract(inputFluidStorage, outputFluidStorage);
        }

        return false;
    }

    // Extract from any block with CommonStorage<FluidResource>
    private static boolean extract(CommonStorage<FluidResource> inputFluidStorage, SimpleFluidStorage outputFluidStorage) {
        long totalExtracted = 0;

        for (int i = 0; i < inputFluidStorage.size(); i++) {
            if (inputFluidStorage.getResource(i).isBlank()) {
                continue;
            }
            FluidResource resource = inputFluidStorage.getResource(i);
            long inserted = outputFluidStorage.insert(resource, Math.min(inputFluidStorage.getAmount(i), FluidAmounts.BUCKET - totalExtracted), true);
            long extracted = inputFluidStorage.extract(resource, inserted, true);
            if (inserted == extracted) {
                outputFluidStorage.insert(resource, Math.min(inputFluidStorage.getAmount(i), FluidAmounts.BUCKET - totalExtracted), false);
                inputFluidStorage.extract(resource, inserted, true);
                totalExtracted += extracted;
            }
        }
        return totalExtracted > 0;
    }

    // Extract from fluid source blocks in the level
    private static boolean extract(Level level, BlockPos pos, SimpleFluidStorage outputFluidStorage,
            BlockState aboveBlockState) {
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        if ((aboveBlockState.getBlock() instanceof BucketPickup)) {
            BucketPickup aboveBlock = (BucketPickup) aboveBlockState.getBlock();
            long inserted = outputFluidStorage.insert(FluidResource.of(aboveFluidState.getType()), FluidAmounts.BUCKET, true);
            if (inserted == FluidAmounts.BUCKET) {
                ItemStack bucket = aboveBlock.pickupBlock(null, level, pos.above(), aboveBlockState);
                if (!bucket.isEmpty()) {
                    outputFluidStorage.insert(FluidResource.of(aboveFluidState.getType()), FluidAmounts.BUCKET, false);
                    return true;
                }
            }
        } else if (aboveBlockState.getBlock() instanceof BeehiveBlock) {
                int honey_level = BeehiveBlockEntity.getHoneyLevel(aboveBlockState);
                if (honey_level > 0) {
                    long inserted = outputFluidStorage.insert(FluidResource.of(BasicFluidHopper.HONEY.get()), FluidAmounts.BOTTLE, true);
                    if (inserted == FluidAmounts.BOTTLE) {
                        level.setBlockAndUpdate(pos.above(), aboveBlockState.setValue(BeehiveBlock.HONEY_LEVEL, honey_level - 1));
                        outputFluidStorage.insert(FluidResource.of(BasicFluidHopper.HONEY.get()), FluidAmounts.BOTTLE, false);
                        return true;
                    }
                }
        }
        return false;
    }

    // TODO this should work with any fluid with a bucket
    public static boolean tryFillBucket(ItemStack item, Level level, BlockPos pos, Player player, InteractionHand hand,
            SimpleFluidStorage fluidStorage) {
        FluidResource resource = fluidStorage.getResource(0);
        if (resource.isBlank()) {
            return false;
        }
        long extracted = fluidStorage.extract(resource, FluidAmounts.BUCKET, true);
        if (extracted != FluidAmounts.BUCKET) {
            return false;
        }
        Item bucket = resource.getType().getBucket();
        if (bucket != Items.AIR) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, new ItemStack(bucket)));
            player.awardStat(Stats.ITEM_USED.get(item.getItem()));
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
            fluidStorage.extract(resource, FluidAmounts.BUCKET, true);
            return true;
        }
        return false;
    }

    // TODO use FluidAPI and Lookup to find the fluid from a bucket
    public static boolean tryDrainBucket(ItemStack item, Level level, BlockPos pos, Player player, InteractionHand hand,
        SimpleFluidStorage fluidStorage) {
        if (!(item.getItem() instanceof BucketItem)) {
            return false;
        }
        FluidResource resource = FluidResource.of(((BucketItem) item.getItem()).content);
        if (resource.isBlank()) {
            return false;
        }
        long inserted = fluidStorage.insert(resource, FluidAmounts.BUCKET, true);
        if (inserted != FluidAmounts.BUCKET) {
            return false;
        }

        // TODO do the right thing for creative mode (infinite inventory)
        player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, new ItemStack(Items.BUCKET)));
        player.awardStat(Stats.ITEM_USED.get(item.getItem()));
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
        fluidStorage.insert(resource, FluidAmounts.BUCKET, false);
        return true;
    }

    @Nullable
    private static CommonStorage<FluidResource> getFluidStorageAt(Level level, BlockPos pos, Direction direction) {
        CommonStorage<FluidResource> fluidStorage = FluidApi.BLOCK.find(level, pos, direction);
        if (fluidStorage != null) {
            return fluidStorage;
        }

        List<Entity> entities = level.getEntities((Entity) null,
                new AABB(
                        pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

        for (Entity e : entities) {
            if (e instanceof BasicFluidHopperMinecartEntity) {
                return ((BasicFluidHopperMinecartEntity) e).fluidStorage;
            }
        }

        return null;
    }

    @Nullable
    private static CommonStorage<FluidResource> getOutputFluidStorage(Level level, BlockPos pos,
            BasicFluidHopperBlockEntity blockEntity) {
        return getFluidStorageAt(level, pos.relative(blockEntity.facing), blockEntity.facing.getOpposite());
    }

    @Nullable
    private static CommonStorage<FluidResource> getInputFluidStorage(Level level, BlockPos pos) {
        return getFluidStorageAt(level, pos.above(), Direction.DOWN);
    }

    private void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    private boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    public int getAnalogOutputSignal() {
        // Comparator Output should be the number of bottles (1/4 bucket) currently in
        // the hopper, capped at 15.
        long amount = fluidStorage.getAmount(0);
        long limit = fluidStorage.getLimit(0, fluidStorage.getResource(0));
        return Math.min(15, (int) (amount / limit) * BUCKET_CAPACITY * 4);
    }

    @Override
    public CommonStorage<FluidResource> getFluids(@Nullable Direction direction) {
        // Fluid can be pulled from any direction.
        return fluidStorage;
    }
}

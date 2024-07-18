package com.jship.basicfluidhopper.block;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.vehicle.BasicFluidHopperMinecartEntity;

import java.util.List;
import java.util.function.BooleanSupplier;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
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

import org.jetbrains.annotations.Nullable;

public class BasicFluidHopperBlockEntity extends BlockEntity {
    public static final int TRANSFER_COOLDOWN = 8;
    public static final int BUCKET_CAPACITY = 1;
    public final SingleFluidStorage fluidStorage = SingleFluidStorage
            .withFixedCapacity(BUCKET_CAPACITY * FluidConstants.BUCKET, () -> setChanged());
    private int transferCooldown = -1;
    private Direction facing;

    public BasicFluidHopperBlockEntity(BlockPos pos, BlockState state) {
        super(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY, pos, state);
        this.facing = state.getValue(BasicFluidHopperBlock.FACING);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        SingleVariantStorage.readNbt(fluidStorage, FluidVariant.CODEC, FluidVariant::blank, nbt, registryLookup);
        this.transferCooldown = nbt.getInt("TransferCooldown");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        SingleVariantStorage.writeNbt(fluidStorage, FluidVariant.CODEC, nbt, registryLookup);
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

    private boolean isFull() {
        return fluidStorage.getAmount() >= fluidStorage.getCapacity();
    }

    private boolean isEmpty() {
        return fluidStorage.getAmount() == 0;
    }

    private static boolean insert(Level level, BlockPos pos, BasicFluidHopperBlockEntity blockEntity) {
        SingleFluidStorage inputFluidStorage = blockEntity.fluidStorage;
        Storage<FluidVariant> outputFluidStorage = BasicFluidHopperBlockEntity.getOutputFluidStorage(level, pos,
                blockEntity);
        if (outputFluidStorage == null || blockEntity.isEmpty()) {
            return false;
        }
        try (Transaction tx = Transaction.openOuter()) {
            FluidVariant inputFluid = inputFluidStorage.getResource();
            long inserted = outputFluidStorage.insert(inputFluid,
                    Math.min(inputFluidStorage.getAmount(), FluidConstants.BUCKET), tx);
            long extracted = inputFluidStorage.extract(inputFluid, inserted, tx);
            if (inserted == extracted) {
                tx.commit();
                return true;
            }
        }
        return false;
    }

    public static boolean extract(Level level, BlockPos pos, BasicFluidHopperBlockEntity blockEntity) {
        Storage<FluidVariant> inputFluidStorage = getInputFluidStorage(level, pos);
        SingleFluidStorage outputFluidStorage = blockEntity.fluidStorage;
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
        Storage<FluidVariant> inputFluidStorage = getInputFluidStorage(level, vehicleEntity.blockPosition());
        SingleFluidStorage outputFluidStorage = vehicleEntity.fluidStorage;
        if (inputFluidStorage != null) {
            return extract(inputFluidStorage, outputFluidStorage);
        }

        return false;
    }

    // Extract from any block with Storage<FluidVariant>
    private static boolean extract(Storage<FluidVariant> inputFluidStorage, SingleFluidStorage outputFluidStorage) {
        var total_extracted = new Object() {
            long value = 0;
        };
        try (Transaction tx = Transaction.openOuter()) {
            inputFluidStorage.forEach(view -> {
                if (view.isResourceBlank()) {
                    return;
                }
                try (Transaction nested_tx = tx.openNested()) {
                    FluidVariant inputFluid = view.getResource();
                    long inserted = outputFluidStorage.insert(inputFluid,
                            Math.min(view.getAmount(), FluidConstants.BUCKET - total_extracted.value), tx);
                    long extracted = inputFluidStorage.extract(inputFluid, inserted, tx);
                    if (inserted == extracted) {
                        nested_tx.commit();
                        total_extracted.value += extracted;
                    }
                }
            });
            if (total_extracted.value > 0) {
                tx.commit();
                return true;
            }
        }
        return false;
    }

    // Extract from fluid source blocks in the level
    private static boolean extract(Level level, BlockPos pos, SingleFluidStorage outputFluidStorage,
            BlockState aboveBlockState) {
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        if ((aboveBlockState.getBlock() instanceof BucketPickup)) {
            BucketPickup aboveBlock = (BucketPickup) aboveBlockState.getBlock();
            try (Transaction tx = Transaction.openOuter()) {

                long inserted = outputFluidStorage.insert(FluidVariant.of(aboveFluidState.getType()),
                        FluidConstants.BUCKET, tx);
                if (inserted == FluidConstants.BUCKET) {
                    ItemStack bucket = aboveBlock.pickupBlock(null, level, pos.above(), aboveBlockState);
                    if (!bucket.isEmpty()) {
                        tx.commit();
                        return true;
                    }
                }
            }
        } else if (aboveBlockState.getBlock() instanceof BeehiveBlock) {
                int honey_level = BeehiveBlockEntity.getHoneyLevel(aboveBlockState);
                if (honey_level > 0) {
                    try (Transaction tx = Transaction.openOuter()) {
                        long inserted = outputFluidStorage.insert(FluidVariant.of(BasicFluidHopper.HONEY), FluidConstants.BOTTLE, tx);
                        if (inserted == FluidConstants.BOTTLE) {
                            level.setBlockAndUpdate(pos.above(), aboveBlockState.setValue(BeehiveBlock.HONEY_LEVEL, honey_level - 1));
                            tx.commit();
                            return true;
                        }
                    }
                }
        }
        return false;
    }

    public static boolean tryFillBucket(ItemStack item, Level level, BlockPos pos, Player player, InteractionHand hand,
            SingleFluidStorage fluidStorage) {
        try (Transaction tx = Transaction.openOuter()) {
            FluidVariant fluidVariant = fluidStorage.getResource();
            if (fluidVariant.isBlank()) {
                return false;
            }
            long extracted = fluidStorage.extract(fluidVariant, FluidConstants.BUCKET, tx);
            if (extracted != FluidConstants.BUCKET) {
                return false;
            }

            if (fluidVariant.isOf(Fluids.WATER)) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, new ItemStack(Items.WATER_BUCKET)));
                player.awardStat(Stats.ITEM_USED.get(item.getItem()));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                tx.commit();
                return true;
            } else if (fluidVariant.isOf(Fluids.LAVA)) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, new ItemStack(Items.LAVA_BUCKET)));
                player.awardStat(Stats.ITEM_USED.get(item.getItem()));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                tx.commit();
                return true;
            } else if (fluidVariant.isOf(BasicFluidHopper.HONEY)) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, new ItemStack(BasicFluidHopper.HONEY_BUCKET)));
                player.awardStat(Stats.ITEM_USED.get(item.getItem()));
                level.playSound(null, pos, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                tx.commit();
                return true;
            }
        }
        return false;
    }

    public static boolean tryDrainBucket(ItemStack item, Level level, BlockPos pos, Player player, InteractionHand hand,
            SingleFluidStorage fluidStorage) {
        try (Transaction tx = Transaction.openOuter()) {
            FluidVariant fluidVariant;
            if (item.is(Items.WATER_BUCKET)) {
                fluidVariant = FluidVariant.of(Fluids.WATER);
            } else if (item.is(Items.LAVA_BUCKET)) {
                fluidVariant = FluidVariant.of(Fluids.LAVA);
            } else if (item.is(BasicFluidHopper.HONEY_BUCKET)) {
                fluidVariant = FluidVariant.of(BasicFluidHopper.HONEY);
            } else {
                return false;
            }

            long inserted = fluidStorage.insert(fluidVariant, FluidConstants.BUCKET, tx);
            if (inserted != FluidConstants.BUCKET) {
                return false;
            }

            player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.ITEM_USED.get(item.getItem()));
            SoundEvent bucketEmpty;
            if (item.is(Items.LAVA_BUCKET)) {
                bucketEmpty = SoundEvents.BUCKET_EMPTY_LAVA;
            } else if (item.is(BasicFluidHopper.HONEY_BUCKET)) {
                bucketEmpty = SoundEvents.BEEHIVE_DRIP;
            } else {
                bucketEmpty = SoundEvents.BUCKET_EMPTY;
            }
            level.playSound(null, pos, bucketEmpty, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            tx.commit();
            return true;
        }
    }

    @Nullable
    private static Storage<FluidVariant> getFluidStorageAt(Level level, BlockPos pos, Direction direction) {
        Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(level, pos, direction);
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
    private static Storage<FluidVariant> getOutputFluidStorage(Level level, BlockPos pos,
            BasicFluidHopperBlockEntity blockEntity) {
        return getFluidStorageAt(level, pos.relative(blockEntity.facing), blockEntity.facing.getOpposite());
    }

    @Nullable
    private static Storage<FluidVariant> getInputFluidStorage(Level level, BlockPos pos) {
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
        return Math.max(15, (int) (fluidStorage.getAmount() / fluidStorage.getCapacity()) * BUCKET_CAPACITY * 4);
    }
}

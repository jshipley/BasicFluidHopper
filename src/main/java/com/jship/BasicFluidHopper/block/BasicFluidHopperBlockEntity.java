package com.jship.BasicFluidHopper.block;

import com.jship.BasicFluidHopper.BasicFluidHopper;
import com.jship.BasicFluidHopper.vehicle.BasicFluidHopperMinecartEntity;

import java.util.List;
import java.util.function.BooleanSupplier;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import org.jetbrains.annotations.Nullable;

public class BasicFluidHopperBlockEntity extends BlockEntity {
    public static final int TRANSFER_COOLDOWN = 8;
    public static final int BUCKET_CAPACITY = 1;
    public final SingleFluidStorage fluidStorage = SingleFluidStorage
            .withFixedCapacity(BUCKET_CAPACITY * FluidConstants.BUCKET, () -> markDirty());
    private int transferCooldown = -1;
    private Direction facing;

    public BasicFluidHopperBlockEntity(BlockPos pos, BlockState state) {
        super(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY, pos, state);
        this.facing = state.get(BasicFluidHopperBlock.FACING);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        SingleVariantStorage.readNbt(fluidStorage, FluidVariant.CODEC, FluidVariant::blank, nbt, registryLookup);
        this.transferCooldown = nbt.getInt("TransferCooldown");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        SingleVariantStorage.writeNbt(fluidStorage, FluidVariant.CODEC, nbt, registryLookup);
        nbt.putInt("TransferCooldown", this.transferCooldown);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BasicFluidHopperBlockEntity blockEntity) {
        if (blockEntity.transferCooldown > 0) {
            --blockEntity.transferCooldown;
        }
        if (!blockEntity.needsCooldown()) {
            BasicFluidHopperBlockEntity.insertAndExtract(world, pos, state, blockEntity,
                    () -> BasicFluidHopperBlockEntity.extract(world, pos, blockEntity));
        }
    }

    private static boolean insertAndExtract(World world, BlockPos pos, BlockState state,
            BasicFluidHopperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
        if (world.isClient) {
            return false;
        }
        if (!blockEntity.needsCooldown() && state.get(BasicFluidHopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            if (!blockEntity.isEmpty()) {
                // try to insert
                bl = BasicFluidHopperBlockEntity.insert(world, pos, blockEntity);
            }
            if (!blockEntity.isFull()) {
                // try to extract
                bl |= booleanSupplier.getAsBoolean();
            }
            if (bl) {
                // insert or extract succeeded
                blockEntity.setTransferCooldown(TRANSFER_COOLDOWN);
                BasicFluidHopperBlockEntity.markDirty(world, pos, state);
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

    private static boolean insert(World world, BlockPos pos, BasicFluidHopperBlockEntity blockEntity) {
        SingleFluidStorage inputFluidStorage = blockEntity.fluidStorage;
        Storage<FluidVariant> outputFluidStorage = BasicFluidHopperBlockEntity.getOutputFluidStorage(world, pos,
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

    public static boolean extract(World world, BlockPos pos, BasicFluidHopperBlockEntity blockEntity) {
        Storage<FluidVariant> inputFluidStorage = getInputFluidStorage(world, pos);
        SingleFluidStorage outputFluidStorage = blockEntity.fluidStorage;
        if (inputFluidStorage != null) {
            return extract(inputFluidStorage, outputFluidStorage);
        }

        BlockState aboveBlockState = world.getBlockState(pos.offset(Direction.UP));
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        if (aboveFluidState.isStill()) {
            return extract(world, pos, outputFluidStorage, aboveBlockState);
        }

        return false;
    }

    public static boolean extract(World world, BasicFluidHopperMinecartEntity vehicleEntity) {
        Storage<FluidVariant> inputFluidStorage = getInputFluidStorage(world, vehicleEntity.getBlockPos());
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

    // Extract from fluid source blocks in the world
    private static boolean extract(World world, BlockPos pos, SingleFluidStorage outputFluidStorage,
            BlockState aboveBlockState) {
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        if (!(aboveBlockState.getBlock() instanceof FluidDrainable)) {
            return false;
        }
        FluidDrainable aboveBlock = (FluidDrainable) aboveBlockState.getBlock();
        try (Transaction tx = Transaction.openOuter()) {

            long inserted = outputFluidStorage.insert(FluidVariant.of(aboveFluidState.getFluid()),
                    FluidConstants.BUCKET, tx);
            if (inserted == FluidConstants.BUCKET) {
                ItemStack bucket = aboveBlock.tryDrainFluid(null, world, pos.offset(Direction.UP), aboveBlockState);
                if (!bucket.isEmpty()) {
                    tx.commit();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean tryFillBucket(ItemStack item, World world, BlockPos pos, PlayerEntity player, Hand hand,
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
                player.setStackInHand(hand, ItemUsage.exchangeStack(item, player, new ItemStack(Items.WATER_BUCKET)));
                player.incrementStat(Stats.USED.getOrCreateStat(item.getItem()));
                world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                tx.commit();
                return true;
            } else if (fluidVariant.isOf(Fluids.LAVA)) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(item, player, new ItemStack(Items.LAVA_BUCKET)));
                player.incrementStat(Stats.USED.getOrCreateStat(item.getItem()));
                world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                tx.commit();
                return true;
            }
        }
        return false;
    }

    public static boolean tryDrainBucket(ItemStack item, World world, BlockPos pos, PlayerEntity player, Hand hand,
            SingleFluidStorage fluidStorage) {
        try (Transaction tx = Transaction.openOuter()) {
            FluidVariant fluidVariant;
            if (item.isOf(Items.WATER_BUCKET)) {
                fluidVariant = FluidVariant.of(Fluids.WATER);
            } else if (item.isOf(Items.LAVA_BUCKET)) {
                fluidVariant = FluidVariant.of(Fluids.LAVA);
            } else {
                return false;
            }

            long inserted = fluidStorage.insert(fluidVariant, FluidConstants.BUCKET, tx);
            if (inserted != FluidConstants.BUCKET) {
                return false;
            }

            player.setStackInHand(hand, ItemUsage.exchangeStack(item, player, new ItemStack(Items.BUCKET)));
            player.incrementStat(Stats.USED.getOrCreateStat(item.getItem()));
            world.playSound(null, pos,
                    item.isOf(Items.LAVA_BUCKET) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            tx.commit();
            return true;
        }
    }

    @Nullable
    private static Storage<FluidVariant> getFluidStorageAt(World world, BlockPos pos, Direction direction) {
        Storage<FluidVariant> fluidStorage = FluidStorage.SIDED.find(world, pos, direction);
        if (fluidStorage != null) {
            return fluidStorage;
        }

        List<Entity> entities = world.getOtherEntities((Entity) null,
                new Box(
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
    private static Storage<FluidVariant> getOutputFluidStorage(World world, BlockPos pos,
            BasicFluidHopperBlockEntity blockEntity) {
        return getFluidStorageAt(world, pos.offset(blockEntity.facing), blockEntity.facing.getOpposite());
    }

    @Nullable
    private static Storage<FluidVariant> getInputFluidStorage(World world, BlockPos pos) {
        return getFluidStorageAt(world, pos.offset(Direction.UP), Direction.DOWN);
    }

    private void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    private boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    public int getComparatorOutput() {
        // Comparator Output should be the number of bottles (1/4 bucket) currently in
        // the hopper, capped at 15.
        return Math.max(15, (int) (fluidStorage.getAmount() / fluidStorage.getCapacity()) * BUCKET_CAPACITY * 4);
    }
}

package com.jship.basicfluidhopper.block.entity;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;
import com.jship.basicfluidhopper.fluid.FluidHopper;
import com.jship.spiritapi.api.fluid.SpiritFluidStorage;
import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BasicFluidHopperBlockEntity extends BlockEntity implements FluidHopper {

    public final SpiritFluidStorage fluidStorage;
    private int transferCooldown = -1;

    public BasicFluidHopperBlockEntity(BlockPos pos, BlockState state) {
        super(BasicFluidHopper.BASIC_FLUID_HOPPER_BLOCK_ENTITY.get(), pos, state);
        fluidStorage = SpiritFluidStorage.create(
            FluidStack.bucketAmount() * BasicFluidHopperConfig.hopperCapacity(),
            (long) (FluidStack.bucketAmount() * BasicFluidHopperConfig.transferRate()),
            () -> {
                if (!level.isClientSide())
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                this.markDirty();
            }
        );
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        fluidStorage.deserializeNbt(registryLookup, nbt);
        this.transferCooldown = nbt.getInt("transfer_cooldown").orElse(0);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        nbt.merge(fluidStorage.serializeNbt(registryLookup));
        nbt.putInt("transfer_cooldown", this.transferCooldown);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = super.getUpdateTag(registryLookup);
        saveAdditional(nbt, registryLookup);
        return nbt;
    }

    @Override
    public SpiritFluidStorage getFluidStorage() {
        return fluidStorage;
    }

    @Override
    public boolean isEnabled() {
        return this.getBlockState().getValue(BasicFluidHopperBlock.ENABLED).booleanValue();
    }

    @Override
    public Direction getFacing() {
        return this.getBlockState().getValue(BasicFluidHopperBlock.FACING);
    }

    /**
     * Try to drain fluids from above and push fluids into facing storages when not on cooldown.
     * @param level the game world
     * @param pos the hopper's position
     * @param state the hopper's BlockState
     * @param blockEntity the hopper's BlockEntity
     */
    public static void pushFluidTick(
        Level level,
        BlockPos pos,
        BlockState state,
        BasicFluidHopperBlockEntity blockEntity
    ) {
        if (blockEntity.transferCooldown > 0) {
            --blockEntity.transferCooldown;
        }
        if (!blockEntity.needsCooldown()) {
            FluidHopper.fillAndDrain(level, pos, state, (FluidHopper) blockEntity);
        }
    }

    @Override
    public void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    @Override
    public boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    @Override
    public void markDirty() {
        this.setChanged();
        // if (!level.isClientSide()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    /**
     * @return 0-15 depending on how full the hopper is
     */
    public int getAnalogOutputSignal() {
        long amount = fluidStorage.getFluidInTank(0).getAmount();
        long limit = fluidStorage.getTankCapacity(0);
        return (int) ((15 * amount) / limit);
    }
}

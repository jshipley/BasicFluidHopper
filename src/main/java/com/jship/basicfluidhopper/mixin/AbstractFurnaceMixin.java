package com.jship.basicfluidhopper.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.BasicFluidHopperBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceMixin {
    // Trying DROPLET (1) for a continuous burn. Consider using NUGGET (BUCKET / 81)
    // instead if DROPLET doesn't feel right.
    private static final int FUEL_CONSUME_STEP = (int) FluidConstants.DROPLET;

    @Inject(at = @At("HEAD"), method = "getBurnDuration(Lnet/minecraft/world/item/ItemStack;)I", cancellable = true)
    private void getFluidBurnDuration(ItemStack fuel, CallbackInfoReturnable<Integer> cir) {
        AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity) (Object) this;
        BlockPos pos = blockEntity.getBlockPos();

        if (fuelFluidAmountAvailable(blockEntity.getLevel(), pos, blockEntity.getBlockState(),
                blockEntity) >= FUEL_CONSUME_STEP) {
            cir.setReturnValue(FUEL_CONSUME_STEP);
        }
    }

    // Should change
    // blockEntity.isLit() || hasFuel && hasInput
    // to
    // blockEntity.isLit() || hasFluidFuel || hasFuel && hasInput
    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;isLit()Z", ordinal = 2), method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)V")
    private static boolean hasFluidFuel(boolean isLit, @Local LocalRef<Level> level, @Local LocalRef<BlockPos> blockPos,
            @Local LocalRef<BlockState> state, @Local LocalRef<AbstractFurnaceBlockEntity> blockEntity) {
        return isLit || fuelFluidAmountAvailable(level.get(), blockPos.get(), state.get(),
                blockEntity.get()) >= FUEL_CONSUME_STEP;
    }

    // If fluid fuel can be used then use it and set b4 (hasFuel) to false so that the furnace doesn't
    // try to use anything in the fuel slot
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;isLit()Z", ordinal = 4, shift = At.Shift.AFTER), method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)V")
    private static void setFuelAvailable2(Level level, BlockPos pos, BlockState blockState,
            AbstractFurnaceBlockEntity blockEntity, CallbackInfo cbi, @Local(ordinal = 3) LocalBooleanRef b4,
            @Local(ordinal = 0) LocalRef<ItemStack> inputItem, @Local(ordinal = 1) LocalRef<ItemStack> fuelItem) {
        if (fuelFluidAmountAvailable(level, pos, blockState, blockEntity) >= FUEL_CONSUME_STEP
                && extractFuelFluid(level, pos, blockState, blockEntity)) {
            b4.set(false);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    private static BasicFluidHopperBlockEntity getHopperInsertingFluidFuel(Level level, BlockPos pos, BlockState blockState,
            AbstractFurnaceBlockEntity blockEntity) {
        for (Direction direction : new Direction[] { Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH,
                Direction.WEST }) {
            if (level != null && !level.isClientSide()
                    && level.getBlockEntity(pos.relative(direction)) instanceof BasicFluidHopperBlockEntity hopperEntity
                    && hopperEntity.isFacing(direction.getOpposite())
                    && hopperEntity.fluidStorage.variant.getFluid().is(BasicFluidHopper.C_FLUID_FUEL)
                    && hopperEntity.fluidStorage.getAmount() >= FUEL_CONSUME_STEP) {
                return hopperEntity;
            }
        }
        return null;
    }

    private static int fuelFluidAmountAvailable(Level level, BlockPos pos, BlockState blockState,
            AbstractFurnaceBlockEntity blockEntity) {
        BasicFluidHopperBlockEntity hopperEntity = getHopperInsertingFluidFuel(level, pos, blockState, blockEntity);
        return hopperEntity != null ? (int)hopperEntity.fluidStorage.getAmount() : 0;
    }

    private static boolean extractFuelFluid(Level level, BlockPos pos, BlockState blockState,
            AbstractFurnaceBlockEntity blockEntity) {
        BasicFluidHopperBlockEntity hopperEntity = getHopperInsertingFluidFuel(level, pos, blockState, blockEntity);
        return hopperEntity != null ? BasicFluidHopperBlockEntity.extract(hopperEntity, FUEL_CONSUME_STEP) : false;
    }
}

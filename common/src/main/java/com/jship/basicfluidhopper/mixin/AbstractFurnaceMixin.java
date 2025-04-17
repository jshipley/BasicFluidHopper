package com.jship.basicfluidhopper.mixin;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.block.entity.BasicFluidHopperBlockEntity;
import com.jship.basicfluidhopper.config.BasicFluidHopperConfig;
import com.jship.basicfluidhopper.util.FluidHopperUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.architectury.fluid.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceMixin {

    @Inject(at = @At("HEAD"), method = "getBurnDuration(Lnet/minecraft/world/item/ItemStack;)I", cancellable = true)
    private void getFluidBurnDuration(ItemStack fuel, CallbackInfoReturnable<Integer> cir) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        BlockPos pos = blockEntity.getBlockPos();

        BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(
            blockEntity.getLevel(),
            pos,
            blockEntity.getBlockState(),
            blockEntity,
            true,
            null
        );

        if (fluidHopper != null) {
            cir.setReturnValue((int) (BasicFluidHopperConfig.FUEL_CONSUME_STEP * FluidStack.bucketAmount()));
        }
    }

    // Should change
    // blockEntity.isLit() || hasFuel && hasInput
    // to
    // blockEntity.isLit() || hasFluidFuel || hasFuel && hasInput
    @ModifyExpressionValue(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;isLit()Z",
            ordinal = 2
        ),
        method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)V"
    )
    private static boolean hasFluidFuel(
        boolean isLit,
        @Local Level level,
        @Local BlockPos blockPos,
        @Local BlockState state,
        @Local AbstractFurnaceBlockEntity blockEntity
    ) {
        return (
            isLit || FluidHopperUtil.getHopperInsertingFluid(level, blockPos, state, blockEntity, true, null) != null
        );
    }

    // If fluid fuel can be used then use it and set b4 (hasFuel) to false so that
    // the furnace doesn't try to use anything in the fuel slot
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;isLit()Z",
            ordinal = 4,
            shift = At.Shift.AFTER
        ),
        method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)V"
    )
    private static void consumeFuel(
        Level level,
        BlockPos pos,
        BlockState blockState,
        AbstractFurnaceBlockEntity blockEntity,
        CallbackInfo cbi,
        @Local(ordinal = 3) LocalBooleanRef b4
    ) {
        BasicFluidHopperBlockEntity fluidHopper = FluidHopperUtil.getHopperInsertingFluid(
            level,
            pos,
            blockState,
            blockEntity,
            true,
            null
        );
        if (
            fluidHopper != null &&
            !fluidHopper.getFluidStorage().isEmpty() &&
            fluidHopper.getFluidStorage().getFluid().is(BasicFluidHopper.C_FLUID_FUEL)
        ) {
            long fuel_consume_step = (long) (BasicFluidHopperConfig.FUEL_CONSUME_STEP * FluidStack.bucketAmount());
            if (fluidHopper.getFluidStorage().remove(fuel_consume_step, true) == fuel_consume_step) {
                fluidHopper.getFluidStorage().remove(fuel_consume_step, false);
                // Set b4 to false here to bypass the logic that uses traditional fuel
                b4.set(false);
            }
        }
    }
}

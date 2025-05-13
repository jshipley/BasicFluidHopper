package com.jship.basicfluidhopper.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.jship.basicfluidhopper.block.BasicFluidHopperBlock;

import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PointedDripstoneBlock.class)
public abstract class PointedDripstoneBlockMixin {
    @ModifyVariable(method = "findFillableCauldronBelowStalactiteTip", at = @At("STORE"), ordinal = 0)
    private static Predicate<BlockState> isFluidHopper(Predicate<BlockState> predicate) {
        return predicate.or(state -> state.getBlock() instanceof BasicFluidHopperBlock);
    }
}

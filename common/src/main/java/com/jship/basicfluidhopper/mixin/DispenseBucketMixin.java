package com.jship.basicfluidhopper.mixin;

import com.jship.basicfluidhopper.BasicFluidHopper;
import com.jship.basicfluidhopper.util.FluidHopperUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$7")
public abstract class DispenseBucketMixin extends DefaultDispenseItemBehavior {
    @Inject(at = @At(value = "RETURN", shift = At.Shift.BEFORE, ordinal = 1), method = "execute(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", cancellable = true)
    private void dispenseBucket(BlockSource blockSource, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        DefaultDispenseItemBehavior dispenseItemBehavior = (DefaultDispenseItemBehavior) (Object) this;
        ItemStack filledItem = FluidHopperUtil.fillDispenserItemFromHopper(itemStack, blockSource);
        if (!filledItem.is(Items.AIR)) {
            blockSource.level().gameEvent(null, GameEvent.FLUID_PICKUP, blockSource.pos());
            // itemStack.shrink(1);
            //dispenseItemBehavior.dispense(blockSource, filledItem);
            DispenserBlock.DISPENSER_REGISTRY.get(filledItem.getItem()).dispense(blockSource, filledItem);
            cir.setReturnValue(itemStack);
        }
    }
}

package com.jship.basicfluidhopper.mixin;

import com.jship.basicfluidhopper.util.FluidHopperUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$14")
public abstract class DispenseGlassBottleMixin extends OptionalDispenseItemBehavior {
    @Inject(at = @At(value = "RETURN", ordinal = 2, shift = At.Shift.BEFORE), method = "execute(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", cancellable = true)
    private void dispenseGlassBottle(BlockSource blockSource, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        OptionalDispenseItemBehavior dispenseItemBehavior = (OptionalDispenseItemBehavior) (Object) this;
        ItemStack filledItem = FluidHopperUtil.fillDispenserItemFromHopper(itemStack, blockSource);
        if (!filledItem.is(Items.AIR)) {
            dispenseItemBehavior.setSuccess(true);
            blockSource.level().gameEvent(null, GameEvent.FLUID_PICKUP, blockSource.pos());
            itemStack.shrink(1);
            Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
            DefaultDispenseItemBehavior.spawnItem(blockSource.level(), filledItem, 6, direction, DispenserBlock.getDispensePosition(blockSource));
            blockSource.level().levelEvent(1000, blockSource.pos(), 0);
            blockSource.level().levelEvent(2000, blockSource.pos(), direction.get3DDataValue());
            cir.setReturnValue(itemStack);
        }
    }
}

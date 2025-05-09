package com.jship.basicfluidhopper.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.jship.basicfluidhopper.util.FluidHopperUtil;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$11")
public abstract class DispenseGlassBottleMixin extends OptionalDispenseItemBehavior {

    // Replaces default action
    // If there is fluid being fed into the dispenser from a hopper, use that fluid
    // to fill empty bottles. Otherwise continue to run the default action
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/OptionalDispenseItemBehavior;execute(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onExecute(OptionalDispenseItemBehavior behavior, BlockSource blockSource, ItemStack itemStack) {
        ItemStack filledItem = FluidHopperUtil.fillDispenserItemFromHopper(itemStack, blockSource);
        if (!filledItem.isEmpty()) {
            ((OptionalDispenseItemBehavior) (Object) this).setSuccess(true);
            blockSource.level().gameEvent((Entity) null, GameEvent.FLUID_PICKUP, blockSource.pos());
            return this.consumeWithRemainder(blockSource, itemStack, new ItemStack(Items.HONEY_BOTTLE));
        } else {
            return super.execute(blockSource, itemStack);
        }
    }
}

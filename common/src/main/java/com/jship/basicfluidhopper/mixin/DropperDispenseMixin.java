package com.jship.basicfluidhopper.mixin;

import com.jship.basicfluidhopper.util.FluidHopperUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Items;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.jetbrains.annotations.Nullable;

@Mixin(DropperBlock.class)
public class DropperDispenseMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"), method = "dispenseFrom(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V")
    public ItemStack fillAndDispense(DispenseItemBehavior dispenseItemBehavior, BlockSource blockSource, ItemStack itemStack) {
        ItemStack filledItem = FluidHopperUtil.fillDispenserItemFromHopper(itemStack, blockSource);
        if (filledItem.is(Items.AIR)) {
            return dispenseItemBehavior.dispense(blockSource, itemStack);
        } else {
            dispenseItemBehavior.dispense(blockSource, filledItem);
            itemStack.shrink(1);
            return itemStack;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"), method = "dispenseFrom(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V")
    public ItemStack fillAndInsert(Container source, Container destination, ItemStack itemStack, @Nullable Direction direction, @Local BlockSourceImpl blockSource) {
        ItemStack filledItem = FluidHopperUtil.fillDispenserItemFromHopper(itemStack, blockSource);
        if (filledItem.is(Items.AIR)) {
            return HopperBlockEntity.addItem(source, destination, itemStack, direction);
        } else {
            HopperBlockEntity.addItem(source, destination, filledItem, direction);
            itemStack.shrink(1);
            return itemStack;
        }
    }
}

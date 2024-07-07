package com.jship.BasicFluidHopper.vehicle;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.RailShape;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;

public class BasicFluidHopperMinecartItem extends Item {
   private static final ItemDispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior();

   public BasicFluidHopperMinecartItem(Item.Settings settings) {
      super(settings);
      DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World world = context.getWorld();
      BlockPos blockPos = context.getBlockPos();
      BlockState blockState = world.getBlockState(blockPos);
      if (!blockState.isIn(BlockTags.RAILS)) {
         return ActionResult.FAIL;
      } else {
         ItemStack itemStack = context.getStack();
         if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            RailShape railShape = blockState.getBlock() instanceof AbstractRailBlock
                  ? (RailShape) blockState.get(((AbstractRailBlock) blockState.getBlock()).getShapeProperty())
                  : RailShape.NORTH_SOUTH;
            double d = 0.0;
            if (railShape.isAscending()) {
               d = 0.5;
            }

            BasicFluidHopperMinecartEntity basicHopperFluidMinecartEntity = BasicFluidHopperMinecartEntity.create(
                  serverWorld, (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 0.0625 + d,
                  (double) blockPos.getZ() + 0.5);
            serverWorld.spawnEntity(basicHopperFluidMinecartEntity);
            serverWorld.emitGameEvent(GameEvent.ENTITY_PLACE, blockPos,
                  Emitter.of(context.getPlayer(), serverWorld.getBlockState(blockPos.down())));
         }

         itemStack.decrement(1);
         return ActionResult.success(world.isClient);
      }
   }
}

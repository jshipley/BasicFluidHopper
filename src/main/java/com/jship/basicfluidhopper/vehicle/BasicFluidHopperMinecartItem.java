package com.jship.basicfluidhopper.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

public class BasicFluidHopperMinecartItem extends Item {
   private static final DefaultDispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior();

   public BasicFluidHopperMinecartItem(Item.Properties properties) {
      super(properties);
      DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos blockPos = context.getClickedPos();
      BlockState blockState = level.getBlockState(blockPos);
      if (!blockState.is(BlockTags.RAILS)) {
         return InteractionResult.FAIL;
      } else {
         ItemStack itemStack = context.getItemInHand();
         if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            RailShape railShape = blockState.getBlock() instanceof BaseRailBlock
                  ? (RailShape) blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty())
                  : RailShape.NORTH_SOUTH;
            double d = 0.0;
            if (railShape.isAscending()) {
               d = 0.5;
            }

            BasicFluidHopperMinecartEntity basicHopperFluidMinecartEntity = BasicFluidHopperMinecartEntity.create(
                  serverLevel, (double) blockPos.getX() + 0.5, (double) blockPos.getY() + 0.0625 + d,
                  (double) blockPos.getZ() + 0.5);
            serverLevel.addFreshEntity(basicHopperFluidMinecartEntity);
            serverLevel.gameEvent(GameEvent.ENTITY_PLACE, blockPos,
                  GameEvent.Context.of(context.getPlayer(), serverLevel.getBlockState(blockPos.below())));
         }

         itemStack.shrink(1);
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }
}

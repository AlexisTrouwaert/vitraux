package com.alexis.vitraux.block;

import com.alexis.vitraux.item.TemplateItem;
import com.alexis.vitraux.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlankVitrauxPaneBlock extends PaneBlock {

    public BlankVitrauxPaneBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world,
                                             BlockPos pos, PlayerEntity player, Hand hand,
                                             BlockHitResult hit) {
        if (stack.isOf(ModItems.TEMPLATE)) {
            if (!world.isClient) {
                TemplateItem.applyTemplate(stack, world, pos);
            }
            return ItemActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }
}

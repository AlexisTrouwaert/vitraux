package com.alexis.vitraux.block;

import com.alexis.vitraux.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GlazedVitrauxPaneBlock extends StainedGlassPaneBlock {

    // 0=0°  1=90°  2=180°  3=270° — rotates the glazed terracotta pattern on the pane face
    public static final IntProperty TEXTURE_FACING = IntProperty.of("texture_facing", 0, 3);

    public GlazedVitrauxPaneBlock(DyeColor color, Settings settings) {
        super(color, settings);
        setDefaultState(stateManager.getDefaultState().with(TEXTURE_FACING, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TEXTURE_FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        int facing = switch (ctx.getHorizontalPlayerFacing()) {
            case EAST  -> 1;
            case SOUTH -> 2;
            case WEST  -> 3;
            default    -> 0; // NORTH
        };
        return state.with(TEXTURE_FACING, facing);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world,
                                             BlockPos pos, PlayerEntity player, Hand hand,
                                             BlockHitResult hit) {
        if (stack.isOf(ModItems.PINCETTE)) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(TEXTURE_FACING, (state.get(TEXTURE_FACING) + 1) % 4));
            }
            return ItemActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }
}

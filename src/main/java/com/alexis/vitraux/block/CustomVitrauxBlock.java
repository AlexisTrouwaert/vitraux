package com.alexis.vitraux.block;

import com.alexis.vitraux.block.entity.CustomVitrauxBlockEntity;
import com.alexis.vitraux.registry.ModBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class CustomVitrauxBlock extends PaneBlock implements BlockEntityProvider {

    public static final BooleanProperty CONNECTED_UP   = BooleanProperty.of("connected_up");
    public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.of("connected_down");

    public CustomVitrauxBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CONNECTED_UP, CONNECTED_DOWN);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        WorldAccess world = ctx.getWorld();
        return super.getPlacementState(ctx)
            .with(CONNECTED_UP,   world.getBlockState(pos.up()).getBlock()   instanceof CustomVitrauxBlock)
            .with(CONNECTED_DOWN, world.getBlockState(pos.down()).getBlock() instanceof CustomVitrauxBlock);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        state = super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        return switch (direction) {
            case UP   -> state.with(CONNECTED_UP,   neighborState.getBlock() instanceof CustomVitrauxBlock);
            case DOWN -> state.with(CONNECTED_DOWN, neighborState.getBlock() instanceof CustomVitrauxBlock);
            default   -> state;
        };
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomVitrauxBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}

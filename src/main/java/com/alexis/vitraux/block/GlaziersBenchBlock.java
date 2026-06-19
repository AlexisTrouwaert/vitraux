package com.alexis.vitraux.block;

import com.alexis.vitraux.block.entity.GlaziersBenchBlockEntity;
import com.alexis.vitraux.network.CanvasSyncS2CPayload;
import com.alexis.vitraux.screen.GlaziersBenchScreenHandler;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GlaziersBenchBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public GlaziersBenchBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        return MapCodec.unit(this);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GlaziersBenchBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof GlaziersBenchBlockEntity bench) {
            var factory = new SimpleNamedScreenHandlerFactory(
                (syncId, inv, p) -> new GlaziersBenchScreenHandler(syncId, inv, bench),
                Text.translatable("container.vitraux.glaziers_bench")
            );
            // openHandledScreen returns the assigned syncId; use it to send initial canvas
            player.openHandledScreen(factory).ifPresent(syncId -> {
                if (player instanceof ServerPlayerEntity sp) {
                    ServerPlayNetworking.send(sp, new CanvasSyncS2CPayload(
                        syncId, bench.getCanvasWidth(), bench.getCanvasHeight(), bench.getActivePixels()
                    ));
                }
            });
        }
        return ActionResult.SUCCESS;
    }
}

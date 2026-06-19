package com.alexis.vitraux.network;

import com.alexis.vitraux.block.entity.GlaziersBenchBlockEntity;
import com.alexis.vitraux.item.TemplateItem;
import com.alexis.vitraux.registry.ModItems;
import com.alexis.vitraux.screen.GlaziersBenchScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModNetworking {

    public static void registerServerReceivers() {
        PayloadTypeRegistry.playC2S().register(SetPixelC2SPayload.ID,        SetPixelC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDimensionsC2SPayload.ID,   SetDimensionsC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CreateTemplateC2SPayload.ID,  CreateTemplateC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FillCellC2SPayload.ID,        FillCellC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FillRectC2SPayload.ID,        FillRectC2SPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(CanvasSyncS2CPayload.ID,      CanvasSyncS2CPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SetPixelC2SPayload.ID, (payload, ctx) -> {
            ctx.server().execute(() -> {
                ServerPlayerEntity player = ctx.player();
                ScreenHandler handler = player.currentScreenHandler;
                if (handler instanceof GlaziersBenchScreenHandler gbh
                        && gbh.syncId == payload.syncId()) {
                    GlaziersBenchBlockEntity be = gbh.getBlockEntity();
                    if (be != null) {
                        be.setPixel(payload.pixelX(), payload.pixelY(), payload.colorIndex());
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SetDimensionsC2SPayload.ID, (payload, ctx) -> {
            ctx.server().execute(() -> {
                ServerPlayerEntity player = ctx.player();
                ScreenHandler handler = player.currentScreenHandler;
                if (handler instanceof GlaziersBenchScreenHandler gbh
                        && gbh.syncId == payload.syncId()) {
                    GlaziersBenchBlockEntity be = gbh.getBlockEntity();
                    if (be != null) {
                        be.setDimensions(payload.width(), payload.height());
                        // Send updated canvas back
                        ServerPlayNetworking.send(player, new CanvasSyncS2CPayload(
                            gbh.syncId, be.getCanvasWidth(), be.getCanvasHeight(), be.getActivePixels()
                        ));
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(FillCellC2SPayload.ID, (payload, ctx) -> {
            ctx.server().execute(() -> {
                ServerPlayerEntity player = ctx.player();
                ScreenHandler handler = player.currentScreenHandler;
                if (handler instanceof GlaziersBenchScreenHandler gbh
                        && gbh.syncId == payload.syncId()) {
                    GlaziersBenchBlockEntity be = gbh.getBlockEntity();
                    if (be == null) return;
                    int baseX = (payload.cellCol() & 0xFF) * GlaziersBenchBlockEntity.CELL;
                    int baseY = (payload.cellRow() & 0xFF) * GlaziersBenchBlockEntity.CELL;
                    byte color = payload.colorIndex();
                    for (int py = 0; py < GlaziersBenchBlockEntity.CELL; py++)
                        for (int px = 0; px < GlaziersBenchBlockEntity.CELL; px++)
                            be.setPixel(baseX + px, baseY + py, color);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(FillRectC2SPayload.ID, (payload, ctx) -> {
            ctx.server().execute(() -> {
                ServerPlayerEntity player = ctx.player();
                ScreenHandler handler = player.currentScreenHandler;
                if (handler instanceof GlaziersBenchScreenHandler gbh
                        && gbh.syncId == payload.syncId()) {
                    GlaziersBenchBlockEntity be = gbh.getBlockEntity();
                    if (be == null) return;
                    int x0 = Math.min(payload.gx0(), payload.gx1());
                    int x1 = Math.max(payload.gx0(), payload.gx1());
                    int y0 = Math.min(payload.gy0(), payload.gy1());
                    int y1 = Math.max(payload.gy0(), payload.gy1());
                    byte color = payload.colorIndex();
                    for (int gy = y0; gy <= y1; gy++)
                        for (int gx = x0; gx <= x1; gx++)
                            be.setPixel(gx, gy, color);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(CreateTemplateC2SPayload.ID, (payload, ctx) -> {
            ctx.server().execute(() -> {
                ServerPlayerEntity player = ctx.player();
                ScreenHandler handler = player.currentScreenHandler;
                if (handler instanceof GlaziersBenchScreenHandler gbh
                        && gbh.syncId == payload.syncId()) {
                    GlaziersBenchBlockEntity be = gbh.getBlockEntity();
                    if (be == null) return;

                    ItemStack template = new ItemStack(ModItems.TEMPLATE);
                    TemplateItem.writeData(template, be.getCanvasWidth(), be.getCanvasHeight(), be.getActivePixels());
                    // Give to player or drop
                    if (!player.getInventory().insertStack(template)) {
                        player.dropItem(template, false);
                    }
                }
            });
        });
    }
}

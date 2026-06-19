package com.alexis.vitraux;

import com.alexis.vitraux.block.entity.CustomVitrauxBlockEntity;
import com.alexis.vitraux.client.VitrauxIrisCompat;
import com.alexis.vitraux.client.VitrauxShaderCompat;
import com.alexis.vitraux.client.VitrauxShaderInstaller;
import com.alexis.vitraux.client.renderer.CustomVitrauxBlockEntityRenderer;
import com.alexis.vitraux.network.CanvasSyncS2CPayload;
import com.alexis.vitraux.registry.ModBlockEntities;
import com.alexis.vitraux.registry.ModBlocks;
import com.alexis.vitraux.screen.GlaziersBenchScreen;
import com.alexis.vitraux.screen.GlaziersBenchScreenHandler;
import com.alexis.vitraux.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.DyeColor;

@Environment(EnvType.CLIENT)
public class VitrauxModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        VitrauxShaderCompat.init();
        VitrauxIrisCompat.init();
        VitrauxShaderInstaller.installIfNeeded();

        // Render layers
        for (DyeColor color : DyeColor.values()) {
            BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.VITRAUX_BY_COLOR.get(color), RenderLayer.getTranslucent());
        }
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BLANK_VITRAUX,  RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CUSTOM_VITRAUX, RenderLayer.getTranslucent());

        // Block color: tint the white_tint_pane geometry with the pixel art's average color
        // so shaders (Iris/OptiFine colored shadows) pick up the right hue automatically.
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null
                    && world.getBlockEntity(pos) instanceof CustomVitrauxBlockEntity be) {
                return be.getAverageColor();
            }
            return 0xFFFFFF;
        }, ModBlocks.CUSTOM_VITRAUX);

        // Block entity renderer
        BlockEntityRendererRegistry.register(ModBlockEntities.CUSTOM_VITRAUX, CustomVitrauxBlockEntityRenderer::new);

        // GUI screen
        HandledScreens.register(ModScreenHandlers.GLAZIERS_BENCH, GlaziersBenchScreen::new);

        // Canvas sync S2C
        ClientPlayNetworking.registerGlobalReceiver(CanvasSyncS2CPayload.ID, (payload, ctx) -> {
            ctx.client().execute(() -> {
                var screen = ctx.client().currentScreen;
                if (screen instanceof GlaziersBenchScreen gbScreen) {
                    GlaziersBenchScreenHandler handler = gbScreen.getScreenHandler();
                    if (handler.syncId == payload.syncId()) {
                        handler.receiveCanvasSync(payload.width(), payload.height(), payload.pixels());
                    }
                }
            });
        });
    }
}

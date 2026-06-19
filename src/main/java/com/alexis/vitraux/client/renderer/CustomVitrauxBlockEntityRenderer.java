package com.alexis.vitraux.client.renderer;

import com.alexis.vitraux.VitrauxMod;
import com.alexis.vitraux.block.entity.CustomVitrauxBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class CustomVitrauxBlockEntityRenderer implements BlockEntityRenderer<CustomVitrauxBlockEntity> {

    private static final double LOD_DIST_SQ = 24.0 * 24.0;
    private static final int    SIZE        = CustomVitrauxBlockEntity.SIZE;
    private static final int[]  DYE_ARGB    = buildDyeArgb();

    // Pane face positions: offset 0.003f outward from the block model faces (7/16, 9/16)
    // so the BER quads always win the depth test and z-fighting is eliminated.
    private static final float PE = 9f / 16 + 0.003f; // east / south outer face
    private static final float PW = 7f / 16 - 0.003f; // west / north outer face

    private final Map<BlockPos, NativeImageBackedTexture> texCache = new HashMap<>();
    private final Map<BlockPos, Identifier>               idCache  = new HashMap<>();

    public CustomVitrauxBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(CustomVitrauxBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider consumers, int light, int overlay) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d cam = camera.getPos();
        BlockPos pos = entity.getPos();

        double dx = pos.getX() + 0.5 - cam.x;
        double dy = pos.getY() + 0.5 - cam.y;
        double dz = pos.getZ() + 0.5 - cam.z;
        if (dx*dx + dy*dy + dz*dz > LOD_DIST_SQ) return;

        if (entity.isTextureDirty()) rebuildTexture(entity);

        Identifier texId = idCache.get(pos);
        if (texId == null) return;

        BlockState state = entity.getCachedState();
        boolean north = state.get(PaneBlock.NORTH);
        boolean south = state.get(PaneBlock.SOUTH);
        boolean east  = state.get(PaneBlock.EAST);
        boolean west  = state.get(PaneBlock.WEST);

        boolean ns = north || south || (!east && !west);
        boolean ew = east  || west  || (!north && !south);

        RenderLayer layer = RenderLayer.getEntityTranslucent(texId);
        VertexConsumer vc = consumers.getBuffer(layer);
        MatrixStack.Entry entry = matrices.peek();

        if (ns) {
            // N-S pane: two outward-offset faces visible from E/W
            quad(vc, entry, light, overlay,
                PE,0,0,  PE,1,0,  PE,1,1,  PE,0,1,
                0,1, 0,0, 1,0, 1,1,  1,0,0);
            quad(vc, entry, light, overlay,
                PW,0,1,  PW,1,1,  PW,1,0,  PW,0,0,
                1,1, 1,0, 0,0, 0,1,  -1,0,0);
        }
        if (ew) {
            // E-W pane: two outward-offset faces visible from S/N
            quad(vc, entry, light, overlay,
                1,0,PE,  1,1,PE,  0,1,PE,  0,0,PE,
                1,1, 1,0, 0,0, 0,1,  0,0,1);
            quad(vc, entry, light, overlay,
                0,0,PW,  0,1,PW,  1,1,PW,  1,0,PW,
                0,1, 0,0, 1,0, 1,1,  0,0,-1);
        }
    }

    // ── Quad helper ───────────────────────────────────────────────────────────

    private void quad(VertexConsumer vc, MatrixStack.Entry e, int light, int overlay,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float u0, float v0, float u1, float v1,
                      float u2, float v2, float u3, float v3,
                      float nx, float ny, float nz) {
        vc.vertex(e.getPositionMatrix(),x0,y0,z0).color(255,255,255,255).texture(u0,v0).overlay(overlay).light(light).normal(e,nx,ny,nz);
        vc.vertex(e.getPositionMatrix(),x1,y1,z1).color(255,255,255,255).texture(u1,v1).overlay(overlay).light(light).normal(e,nx,ny,nz);
        vc.vertex(e.getPositionMatrix(),x2,y2,z2).color(255,255,255,255).texture(u2,v2).overlay(overlay).light(light).normal(e,nx,ny,nz);
        vc.vertex(e.getPositionMatrix(),x3,y3,z3).color(255,255,255,255).texture(u3,v3).overlay(overlay).light(light).normal(e,nx,ny,nz);
    }

    // ── Texture management ────────────────────────────────────────────────────

    private void rebuildTexture(CustomVitrauxBlockEntity entity) {
        BlockPos pos = entity.getPos();

        NativeImageBackedTexture old = texCache.remove(pos);
        if (old != null) {
            Identifier oldId = idCache.remove(pos);
            if (oldId != null) MinecraftClient.getInstance().getTextureManager().destroyTexture(oldId);
            old.close();
        }

        byte[] pixels = entity.getPixelsCopy();
        NativeImage img = new NativeImage(NativeImage.Format.RGBA, SIZE, SIZE, false);
        for (int py = 0; py < SIZE; py++) {
            for (int px = 0; px < SIZE; px++) {
                int idx  = pixels[py * SIZE + px] & 0xFF;
                int argb = (idx <= 15) ? DYE_ARGB[idx] : 0x00000000;
                // NativeImage.setColor takes ABGR; convert from ARGB
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8)  & 0xFF;
                int b =  argb        & 0xFF;
                img.setColor(px, py, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }

        NativeImageBackedTexture tex = new NativeImageBackedTexture(img);
        Identifier id = Identifier.of(VitrauxMod.MOD_ID, "cv_" + Long.toHexString(pos.asLong()));
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);

        texCache.put(pos, tex);
        idCache.put(pos, id);
        entity.clearTextureDirty();
    }

    // ── Palette ───────────────────────────────────────────────────────────────

    private static int[] buildDyeArgb() {
        DyeColor[] colors = DyeColor.values();
        int[] out = new int[16];
        for (int i = 0; i < 16; i++) {
            int rgb = colors[i].getEntityColor() & 0x00FFFFFF;
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8)  & 0xFF;
            int b =  rgb        & 0xFF;
            // Dark colours → opaque, bright → transparent (matches pre-made vitraux)
            int lum   = (int)(0.299f * r + 0.587f * g + 0.114f * b);
            int alpha = Math.clamp(255 - lum, 60, 220);
            out[i] = (alpha << 24) | rgb;
        }
        return out;
    }
}

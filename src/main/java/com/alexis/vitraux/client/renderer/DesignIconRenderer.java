package com.alexis.vitraux.client.renderer;

import com.alexis.vitraux.VitrauxMod;
import com.alexis.vitraux.item.TemplateItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renders the actual saved canvas (pixels + a distinguishing frame) directly on the
 * Template/Blueprint item, in every display context (inventory, hand, ground, etc.).
 */
@Environment(EnvType.CLIENT)
public class DesignIconRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private static final int CACHE_LIMIT = 96;

    private record Entry(Identifier id, NativeImageBackedTexture texture) {}

    private static final Map<Long, Entry> CACHE = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Entry> eldest) {
            if (size() > CACHE_LIMIT) {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(eldest.getValue().id());
                eldest.getValue().texture().close();
                return true;
            }
            return false;
        }
    };

    private final boolean finished;

    public DesignIconRenderer(boolean finished) {
        this.finished = finished;
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light, int overlay) {
        TemplateItem.CanvasData data = TemplateItem.readData(stack);
        int w = data != null ? data.width()  : 1;
        int h = data != null ? data.height() : 1;
        byte[] pixels = data != null ? data.pixels() : new byte[16 * 16];
        if (data == null) Arrays.fill(pixels, (byte) 16);

        long key = (((long) w << 56) | ((long) h << 48) | (Arrays.hashCode(pixels) & 0xFFFFFFFFL))
            ^ (finished ? 0x1L : 0L);
        Identifier texId = getOrBuildTexture(key, w, h, pixels);

        RenderLayer layer = RenderLayer.getEntityTranslucentCull(texId);
        VertexConsumer vc = vertexConsumers.getBuffer(layer);

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(0.7f, -0.7f, 0.7f);
        MatrixStack.Entry e = matrices.peek();

        float s = 0.5f;
        // Front face
        quad(vc, e, light, overlay,
            -s, -s, 0.001f,  s, -s, 0.001f,  s, s, 0.001f,  -s, s, 0.001f,
            0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1);
        // Back face (so the icon reads correctly from both sides while spinning on the ground / in hand)
        quad(vc, e, light, overlay,
            s, -s, -0.001f,  -s, -s, -0.001f,  -s, s, -0.001f,  s, s, -0.001f,
            0, 1, 1, 1, 1, 0, 0, 0, 0, 0, -1);

        matrices.pop();
    }

    private void quad(VertexConsumer vc, MatrixStack.Entry e, int light, int overlay,
                       float x0, float y0, float z0, float x1, float y1, float z1,
                       float x2, float y2, float z2, float x3, float y3, float z3,
                       float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3,
                       float nx, float ny, float nz) {
        vc.vertex(e.getPositionMatrix(), x0, y0, z0).color(255,255,255,255).texture(u0,v0).overlay(overlay).light(light).normal(e, nx, ny, nz);
        vc.vertex(e.getPositionMatrix(), x1, y1, z1).color(255,255,255,255).texture(u1,v1).overlay(overlay).light(light).normal(e, nx, ny, nz);
        vc.vertex(e.getPositionMatrix(), x2, y2, z2).color(255,255,255,255).texture(u2,v2).overlay(overlay).light(light).normal(e, nx, ny, nz);
        vc.vertex(e.getPositionMatrix(), x3, y3, z3).color(255,255,255,255).texture(u3,v3).overlay(overlay).light(light).normal(e, nx, ny, nz);
    }

    private Identifier getOrBuildTexture(long key, int w, int h, byte[] pixels) {
        Entry cached = CACHE.get(key);
        if (cached != null) return cached.id();

        NativeImage img = DesignTextureBuilder.build(w, h, pixels, finished);
        NativeImageBackedTexture tex = new NativeImageBackedTexture(img);
        Identifier id = Identifier.of(VitrauxMod.MOD_ID, "design_icon_" + Long.toHexString(key));
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);

        CACHE.put(key, new Entry(id, tex));
        return id;
    }
}

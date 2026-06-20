package com.alexis.vitraux.client.renderer;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.DyeColor;

/** Builds a dynamic preview texture (design pixels + a distinguishing frame) for Template/Blueprint items. */
public final class DesignTextureBuilder {

    private static final int[] DYE_ARGB = buildDyeArgb();

    private DesignTextureBuilder() {}

    /**
     * @param finished true = Blueprint (solid frame, "finished" look), false = Template (dashed frame, "sketch" look)
     */
    public static NativeImage build(int cellsW, int cellsH, byte[] pixels, boolean finished) {
        int w = Math.max(1, cellsW) * 16;
        int h = Math.max(1, cellsH) * 16;
        NativeImage img = new NativeImage(NativeImage.Format.RGBA, w, h, false);

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int idx = (py * w + px < pixels.length) ? (pixels[py * w + px] & 0xFF) : 16;
                int argb = (idx <= 15) ? DYE_ARGB[idx] : 0x00000000;
                setPixelArgb(img, px, py, argb);
            }
        }

        int borderArgb = finished ? 0xFF8B5A2B : 0xFFBEBEBE;
        for (int px = 0; px < w; px++) {
            boolean dash = (px / 2) % 2 == 0;
            if (finished || dash) {
                setPixelArgb(img, px, 0, borderArgb);
                setPixelArgb(img, px, h - 1, borderArgb);
            }
        }
        for (int py = 0; py < h; py++) {
            boolean dash = (py / 2) % 2 == 0;
            if (finished || dash) {
                setPixelArgb(img, 0, py, borderArgb);
                setPixelArgb(img, w - 1, py, borderArgb);
            }
        }

        return img;
    }

    private static void setPixelArgb(NativeImage img, int x, int y, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16)  & 0xFF;
        int g = (argb >> 8)   & 0xFF;
        int b =  argb         & 0xFF;
        // NativeImage.setColor takes ABGR; convert from ARGB
        img.setColor(x, y, (a << 24) | (b << 16) | (g << 8) | r);
    }

    private static int[] buildDyeArgb() {
        DyeColor[] colors = DyeColor.values();
        int[] out = new int[16];
        for (int i = 0; i < 16; i++) {
            int rgb = colors[i].getEntityColor() & 0x00FFFFFF;
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8)  & 0xFF;
            int b =  rgb        & 0xFF;
            int lum   = (int) (0.299f * r + 0.587f * g + 0.114f * b);
            int alpha = Math.clamp(255 - lum, 60, 220);
            out[i] = (alpha << 24) | rgb;
        }
        return out;
    }
}

package com.alexis.vitraux.block.entity;

import com.alexis.vitraux.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CustomVitrauxBlockEntity extends BlockEntity {

    public static final int SIZE = 16;

    // 16×16 pixel data; value 0-15 = DyeColor ordinal, 16 = transparent
    private byte[] pixels = new byte[SIZE * SIZE];
    // Set to true when the client-side NativeImageBackedTexture needs rebuild
    private boolean textureDirty = true;

    public CustomVitrauxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUSTOM_VITRAUX, pos, state);
    }

    public byte[] getPixelsCopy() { return pixels.clone(); }

    public byte getPixel(int x, int y) { return pixels[y * SIZE + x]; }

    public void setPixels(byte[] data) {
        if (data.length == pixels.length) {
            pixels = data.clone();
            textureDirty = true;
            markDirty();
        }
    }

    public boolean isTextureDirty() { return textureDirty; }
    public void clearTextureDirty()  { textureDirty = false; }

    /**
     * Returns the average RGB color (0x00RRGGBB) of all non-transparent pixels.
     * Used by BlockColorProvider so the block model tints as the dominant hue,
     * which lets shaders cast a matching colored shadow.
     */
    /**
     * Returns the dominant hue of the design as a fully-saturated/bright RGB
     * (0x00RRGGBB, max channel always = 255).
     * Normalising prevents dark designs from casting a dark tint onto transparent
     * pixels while still giving the shader the right hue for coloured shadows.
     */
    public int getAverageColor() {
        DyeColor[] dyes = DyeColor.values();
        long rSum = 0, gSum = 0, bSum = 0, count = 0;
        for (byte b : pixels) {
            int idx = b & 0xFF;
            if (idx > 15) continue;
            int rgb = dyes[idx].getEntityColor() & 0x00FFFFFF;
            rSum += (rgb >> 16) & 0xFF;
            gSum += (rgb >> 8)  & 0xFF;
            bSum +=  rgb        & 0xFF;
            count++;
        }
        if (count == 0) return 0xFFFFFF;
        float r = rSum / (float) count;
        float g = gSum / (float) count;
        float b2 = bSum / (float) count;
        // Normalise: scale so the brightest channel = 255.
        // Keeps the hue accurate but ensures it is never perceived as dark.
        float maxC = Math.max(Math.max(r, g), b2);
        if (maxC > 0) {
            float s = 255f / maxC;
            r  = Math.min(255, r  * s);
            g  = Math.min(255, g  * s);
            b2 = Math.min(255, b2 * s);
        }
        return ((int) r << 16) | ((int) g << 8) | (int) b2;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putByteArray("Pixels", pixels);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        byte[] saved = nbt.getByteArray("Pixels");
        if (saved.length == pixels.length) {
            pixels = saved;
        }
        textureDirty = true;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}

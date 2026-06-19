package com.alexis.vitraux.block.entity;

import com.alexis.vitraux.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class GlaziersBenchBlockEntity extends BlockEntity {

    public static final int MAX_W = 4;
    public static final int MAX_H = 4;
    public static final int CELL  = 16;

    private int canvasWidth  = 1;
    private int canvasHeight = 1;
    // pixels[y * canvasWidth*CELL + x], value 0-15 = DyeColor, 16 = transparent
    private byte[] pixels = new byte[MAX_W * CELL * MAX_H * CELL];

    public GlaziersBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GLAZIERS_BENCH, pos, state);
    }

    // ── accessors ────────────────────────────────────────────────────────────

    public int getCanvasWidth()  { return canvasWidth; }
    public int getCanvasHeight() { return canvasHeight; }

    public void setDimensions(int w, int h) {
        canvasWidth  = Math.clamp(w, 1, MAX_W);
        canvasHeight = Math.clamp(h, 1, MAX_H);
        markDirty();
    }

    public byte[] getPixelsCopy() {
        return pixels.clone();
    }

    public void setPixel(int px, int py, byte color) {
        int stride = canvasWidth * CELL;
        if (px < 0 || py < 0 || px >= canvasWidth * CELL || py >= canvasHeight * CELL) return;
        pixels[py * stride + px] = color;
        markDirty();
    }

    /** Returns a flat copy of all pixels for the active canvas area. */
    public byte[] getActivePixels() {
        int w = canvasWidth  * CELL;
        int h = canvasHeight * CELL;
        int fullStride = MAX_W * CELL;
        byte[] out = new byte[w * h];
        for (int row = 0; row < h; row++) {
            System.arraycopy(pixels, row * fullStride, out, row * w, w);
        }
        return out;
    }

    // ── NBT persistence ───────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putByte("CanvasWidth",  (byte) canvasWidth);
        nbt.putByte("CanvasHeight", (byte) canvasHeight);
        nbt.putByteArray("Pixels", pixels);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        canvasWidth  = nbt.getByte("CanvasWidth");
        canvasHeight = nbt.getByte("CanvasHeight");
        byte[] saved = nbt.getByteArray("Pixels");
        if (saved.length == pixels.length) {
            pixels = saved;
        }
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

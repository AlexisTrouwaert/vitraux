package com.alexis.vitraux.block.entity;

import com.alexis.vitraux.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class GlaziersBenchBlockEntity extends BlockEntity implements Inventory {

    public static final int MAX_W = 4;
    public static final int MAX_H = 4;
    public static final int CELL  = 16;

    private int canvasWidth  = 1;
    private int canvasHeight = 1;
    // pixels[y * canvasWidth*CELL + x], value 0-15 = DyeColor, 16 = transparent
    private byte[] pixels = new byte[MAX_W * CELL * MAX_H * CELL];

    // Slot 0 = blank template consumed when creating a template, slot 1 = saved blueprint (reusable)
    private final DefaultedList<ItemStack> templateSlot = DefaultedList.ofSize(2, ItemStack.EMPTY);

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
        int stride = MAX_W * CELL;
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

    /** Replaces the canvas dimensions and pixel data, e.g. when loading a saved blueprint. */
    public void loadDesign(int width, int height, byte[] data) {
        canvasWidth  = Math.clamp(width, 1, MAX_W);
        canvasHeight = Math.clamp(height, 1, MAX_H);
        int srcStride = width  * CELL;
        int dstStride = MAX_W  * CELL;
        int copyW = Math.min(srcStride, dstStride);
        int copyH = Math.min(height * CELL, MAX_H * CELL);
        for (int row = 0; row < copyH; row++) {
            System.arraycopy(data, row * srcStride, pixels, row * dstStride, copyW);
        }
        markDirty();
    }

    // ── Inventory (slot 0 = blank template, slot 1 = saved blueprint) ───────────

    @Override
    public int size() { return 2; }

    @Override
    public boolean isEmpty() { return templateSlot.get(0).isEmpty() && templateSlot.get(1).isEmpty(); }

    @Override
    public ItemStack getStack(int slot) { return templateSlot.get(slot); }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(templateSlot, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = templateSlot.get(slot);
        templateSlot.set(slot, ItemStack.EMPTY);
        markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        templateSlot.set(slot, stack);
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) { return true; }

    @Override
    public void clear() {
        templateSlot.clear();
        markDirty();
    }

    // ── NBT persistence ───────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putByte("CanvasWidth",  (byte) canvasWidth);
        nbt.putByte("CanvasHeight", (byte) canvasHeight);
        nbt.putByteArray("Pixels", pixels);
        Inventories.writeNbt(nbt, templateSlot, registries);
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
        Inventories.readNbt(nbt, templateSlot, registries);
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

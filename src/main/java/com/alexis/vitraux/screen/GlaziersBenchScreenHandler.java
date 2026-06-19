package com.alexis.vitraux.screen;

import com.alexis.vitraux.block.entity.GlaziersBenchBlockEntity;
import com.alexis.vitraux.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;

public class GlaziersBenchScreenHandler extends ScreenHandler {

    // Indices: 0=canvasWidth, 1=canvasHeight, 2=selectedColor
    private final PropertyDelegate delegate;

    private final GlaziersBenchBlockEntity blockEntity;

    // Client-side canvas copy (filled by CanvasSyncS2CPayload)
    private byte[] clientCanvas = new byte[
        GlaziersBenchBlockEntity.MAX_W * GlaziersBenchBlockEntity.CELL
        * GlaziersBenchBlockEntity.MAX_H * GlaziersBenchBlockEntity.CELL];

    /** Server-side constructor. */
    public GlaziersBenchScreenHandler(int syncId, PlayerInventory inv, GlaziersBenchBlockEntity be) {
        super(ModScreenHandlers.GLAZIERS_BENCH, syncId);
        this.blockEntity = be;
        this.delegate = new ArrayPropertyDelegate(3);
        this.delegate.set(0, be.getCanvasWidth());
        this.delegate.set(1, be.getCanvasHeight());
        this.delegate.set(2, 16); // default: transparent
        addProperties(delegate);
    }

    /** Client-side constructor (registered via ScreenHandlerType factory). */
    public GlaziersBenchScreenHandler(int syncId, PlayerInventory inv) {
        super(ModScreenHandlers.GLAZIERS_BENCH, syncId);
        this.blockEntity = null;
        this.delegate = new ArrayPropertyDelegate(3);
        this.delegate.set(0, 1);
        this.delegate.set(1, 1);
        this.delegate.set(2, 16);
        addProperties(delegate);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getCanvasWidth()   { return delegate.get(0); }
    public int getCanvasHeight()  { return delegate.get(1); }
    public int getSelectedColor() { return delegate.get(2); }
    public void setSelectedColor(int c) { delegate.set(2, c); }
    public byte[] getClientCanvas() { return clientCanvas; }

    /** Called client-side when CanvasSyncS2CPayload arrives. */
    public void receiveCanvasSync(int width, int height, byte[] pixels) {
        delegate.set(0, width);
        delegate.set(1, height);
        System.arraycopy(pixels, 0, clientCanvas, 0, Math.min(pixels.length, clientCanvas.length));
    }

    public GlaziersBenchBlockEntity getBlockEntity() { return blockEntity; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }
}

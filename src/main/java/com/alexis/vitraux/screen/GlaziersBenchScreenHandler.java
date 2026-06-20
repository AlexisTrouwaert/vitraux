package com.alexis.vitraux.screen;

import com.alexis.vitraux.block.entity.GlaziersBenchBlockEntity;
import com.alexis.vitraux.item.TemplateItem;
import com.alexis.vitraux.network.CanvasSyncS2CPayload;
import com.alexis.vitraux.registry.ModItems;
import com.alexis.vitraux.registry.ModScreenHandlers;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class GlaziersBenchScreenHandler extends ScreenHandler {

    // Coordinates relative to the screen's top-left corner
    public static final int TEMPLATE_SLOT_X  = 8;
    public static final int TEMPLATE_SLOT_Y  = 20;
    public static final int BLUEPRINT_SLOT_X = 34;
    public static final int BLUEPRINT_SLOT_Y = 20;

    public static final int INV_X     = 8;
    public static final int INV_Y     = 246;
    public static final int HOTBAR_Y  = 304;

    // Slot indices: 0 = blank template, 1 = saved blueprint, 2..28 = main inventory, 29..37 = hotbar
    private static final int MAIN_INV_START = 2;
    private static final int HOTBAR_START   = 29;
    private static final int SLOT_COUNT     = 38;

    // Indices: 0=canvasWidth, 1=canvasHeight, 2=selectedColor
    private final PropertyDelegate delegate;

    private final GlaziersBenchBlockEntity blockEntity;
    private final PlayerInventory playerInventory;

    // Client-side canvas copy (filled by CanvasSyncS2CPayload)
    private byte[] clientCanvas = new byte[
        GlaziersBenchBlockEntity.MAX_W * GlaziersBenchBlockEntity.CELL
        * GlaziersBenchBlockEntity.MAX_H * GlaziersBenchBlockEntity.CELL];

    /** Server-side constructor. */
    public GlaziersBenchScreenHandler(int syncId, PlayerInventory inv, GlaziersBenchBlockEntity be) {
        super(ModScreenHandlers.GLAZIERS_BENCH, syncId);
        this.blockEntity = be;
        this.playerInventory = inv;
        this.delegate = new ArrayPropertyDelegate(3);
        this.delegate.set(0, be.getCanvasWidth());
        this.delegate.set(1, be.getCanvasHeight());
        this.delegate.set(2, 16); // default: transparent
        addProperties(delegate);
        addSpecialSlots(be);
        addPlayerInventorySlots(inv);
    }

    /** Client-side constructor (registered via ScreenHandlerType factory). */
    public GlaziersBenchScreenHandler(int syncId, PlayerInventory inv) {
        super(ModScreenHandlers.GLAZIERS_BENCH, syncId);
        this.blockEntity = null;
        this.playerInventory = inv;
        this.delegate = new ArrayPropertyDelegate(3);
        this.delegate.set(0, 1);
        this.delegate.set(1, 1);
        this.delegate.set(2, 16);
        addProperties(delegate);
        addSpecialSlots(new SimpleInventory(2));
        addPlayerInventorySlots(inv);
    }

    private void addSpecialSlots(net.minecraft.inventory.Inventory inv) {
        addSlot(new Slot(inv, 0, TEMPLATE_SLOT_X, TEMPLATE_SLOT_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.BLANK_TEMPLATE);
            }
        });
        addSlot(new Slot(inv, 1, BLUEPRINT_SLOT_X, BLUEPRINT_SLOT_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.BLUEPRINT);
            }

            @Override
            public void setStack(ItemStack stack) {
                super.setStack(stack);
                autoLoadBlueprint(stack);
            }
        });
    }

    /** Loads the blueprint's design into the canvas as soon as it's placed in its slot. */
    private void autoLoadBlueprint(ItemStack stack) {
        if (blockEntity == null || stack.isEmpty() || !stack.isOf(ModItems.BLUEPRINT)) return;

        TemplateItem.CanvasData data = TemplateItem.readData(stack);
        if (data == null) return;

        blockEntity.loadDesign(data.width(), data.height(), data.pixels());
        if (playerInventory.player instanceof ServerPlayerEntity sp) {
            ServerPlayNetworking.send(sp, new CanvasSyncS2CPayload(
                syncId, blockEntity.getCanvasWidth(), blockEntity.getCanvasHeight(), blockEntity.getActivePixels()
            ));
        }
    }

    private void addPlayerInventorySlots(PlayerInventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, INV_X + col * 18, HOTBAR_Y));
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getCanvasWidth()   { return delegate.get(0); }
    public int getCanvasHeight()  { return delegate.get(1); }
    public int getSelectedColor() { return delegate.get(2); }
    public void setSelectedColor(int c) { delegate.set(2, c); }
    public byte[] getClientCanvas() { return clientCanvas; }

    /** True once a blank template has been inserted into the input slot. */
    public boolean hasBlankTemplate() {
        return !getSlot(0).getStack().isEmpty();
    }

    /** True once a saved blueprint has been inserted into its slot. */
    public boolean hasBlueprint() {
        return !getSlot(1).getStack().isEmpty();
    }

    /** Called client-side when CanvasSyncS2CPayload arrives. */
    public void receiveCanvasSync(int width, int height, byte[] pixels) {
        delegate.set(0, width);
        delegate.set(1, height);
        System.arraycopy(pixels, 0, clientCanvas, 0, Math.min(pixels.length, clientCanvas.length));
    }

    public GlaziersBenchBlockEntity getBlockEntity() { return blockEntity; }

    private boolean tryInsertSpecial(ItemStack stack) {
        if (stack.isOf(ModItems.BLANK_TEMPLATE)) return insertItem(stack, 0, 1, false);
        if (stack.isOf(ModItems.BLUEPRINT))      return insertItem(stack, 1, 2, false);
        return false;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasStack()) return ItemStack.EMPTY;

        ItemStack original = slot.getStack();
        ItemStack copy = original.copy();

        if (slotIndex == 0 || slotIndex == 1) {
            if (!insertItem(original, MAIN_INV_START, SLOT_COUNT, true)) return ItemStack.EMPTY;
        } else if (slotIndex < HOTBAR_START) {
            if (!tryInsertSpecial(original) && !insertItem(original, HOTBAR_START, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!tryInsertSpecial(original) && !insertItem(original, MAIN_INV_START, HOTBAR_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        if (original.getCount() == copy.getCount()) return ItemStack.EMPTY;

        slot.onTakeItem(player, original);
        return copy;
    }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }
}

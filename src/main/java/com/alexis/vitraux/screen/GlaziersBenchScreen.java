package com.alexis.vitraux.screen;

import com.alexis.vitraux.block.entity.GlaziersBenchBlockEntity;
import com.alexis.vitraux.network.CreateTemplateC2SPayload;
import com.alexis.vitraux.network.FillCellC2SPayload;
import com.alexis.vitraux.network.FillRectC2SPayload;
import com.alexis.vitraux.network.LoadBlueprintC2SPayload;
import com.alexis.vitraux.network.SaveDesignC2SPayload;
import com.alexis.vitraux.network.SetDimensionsC2SPayload;
import com.alexis.vitraux.network.SetPixelC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.lwjgl.glfw.GLFW;

public class GlaziersBenchScreen extends HandledScreen<GlaziersBenchScreenHandler> {

    private static final int MODE_OVERVIEW  = 0;
    private static final int MODE_CELL_EDIT = 1;

    private int mode = MODE_OVERVIEW;
    private int editCellCol = -1;
    private int editCellRow = -1;

    // Left panel: full-canvas preview (stage 2 only)
    private static final int PREV_X    = 7;
    private static final int PREV_Y    = 44;
    private static final int PREV_SIZE = 128;

    // Right panel: cell editor (stage 2 only, visible in MODE_CELL_EDIT)
    private static final int EDIT_X    = 145;
    private static final int EDIT_Y    = 44;
    private static final int EDIT_SIZE = 128;   // 16 texels × 8 px/texel
    private static final int EDIT_PX   = 8;

    // Neighbor reference strip thickness (outside the editor border)
    private static final int NEIGHBOR_PX = 4;

    // Palette: horizontal row below the panels (stage 2 only)
    private static final int PAL_X      = 7;
    private static final int PAL_Y      = 180;
    private static final int SWATCH     = 12;
    private static final int SWATCH_GAP = 2;    // slot width = SWATCH + SWATCH_GAP = 14

    // Dimension / fill / back buttons row (stage 2 only)
    private static final int BUTTON_ROW_Y = 196;

    // Name field + create button row (stage 2 only)
    private static final int CREATE_ROW_Y = 216;

    // Stage 2 placeholder bounding box (shown instead when no template inserted)
    private static final int STAGE2_X0 = 7,   STAGE2_Y0 = 44;
    private static final int STAGE2_X1 = 273, STAGE2_Y1 = 234;

    private static final int[] PALETTE_ARGB = buildPalette();

    private ButtonWidget wMinusButton, wPlusButton, hMinusButton, hPlusButton;
    private ButtonWidget backButton;
    private ButtonWidget fillButton;
    private ButtonWidget loadButton;
    private ButtonWidget createButton;
    private ButtonWidget saveButton;
    private TextFieldWidget nameField;
    private boolean hadBlueprintLoaded = false;

    // Rectangle selection state (right-click drag)
    private boolean rectDragging = false;
    private int rectStartPx = -1, rectStartPy = -1;
    private int rectCurrPx  = -1, rectCurrPy  = -1;

    public GlaziersBenchScreen(GlaziersBenchScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth  = 280;
        this.backgroundHeight = 330;
    }

    @Override
    protected void init() {
        super.init();

        wMinusButton = ButtonWidget.builder(Text.literal("W-"), b -> changeDim(-1, 0))
            .dimensions(x + 7,  y + BUTTON_ROW_Y, 20, 16).build();
        wPlusButton = ButtonWidget.builder(Text.literal("W+"), b -> changeDim(+1, 0))
            .dimensions(x + 29, y + BUTTON_ROW_Y, 20, 16).build();
        hMinusButton = ButtonWidget.builder(Text.literal("H-"), b -> changeDim(0, -1))
            .dimensions(x + 53, y + BUTTON_ROW_Y, 20, 16).build();
        hPlusButton = ButtonWidget.builder(Text.literal("H+"), b -> changeDim(0, +1))
            .dimensions(x + 75, y + BUTTON_ROW_Y, 20, 16).build();
        addDrawableChild(wMinusButton);
        addDrawableChild(wPlusButton);
        addDrawableChild(hMinusButton);
        addDrawableChild(hPlusButton);

        loadButton = ButtonWidget.builder(Text.translatable("gui.vitraux.load_design"), b -> loadDesign())
            .dimensions(x + 99, y + BUTTON_ROW_Y, 74, 16).build();
        addDrawableChild(loadButton);

        fillButton = ButtonWidget.builder(Text.literal("Fill"), b -> fillCell())
            .dimensions(x + 177, y + BUTTON_ROW_Y, 36, 16).build();
        addDrawableChild(fillButton);

        backButton = ButtonWidget.builder(Text.literal("< Back"), b -> {
            mode = MODE_OVERVIEW;
            editCellCol = -1;
            editCellRow = -1;
            rectDragging = false;
        }).dimensions(x + 216, y + BUTTON_ROW_Y, 58, 16).build();
        addDrawableChild(backButton);

        nameField = new TextFieldWidget(textRenderer, x + 7, y + CREATE_ROW_Y + 1, 100, 16,
            Text.translatable("gui.vitraux.template_name"));
        nameField.setMaxLength(32);
        nameField.setText("Template");
        addDrawableChild(nameField);

        createButton = ButtonWidget.builder(
                Text.translatable("gui.vitraux.create_button"), b -> createTemplate())
            .dimensions(x + 111, y + CREATE_ROW_Y, 70, 18).build();
        addDrawableChild(createButton);

        saveButton = ButtonWidget.builder(
                Text.translatable("gui.vitraux.save_design"), b -> saveDesign())
            .dimensions(x + 185, y + CREATE_ROW_Y, 88, 18).build();
        addDrawableChild(saveButton);
    }

    private boolean stage2Active() {
        return handler.hasBlankTemplate() || handler.hasBlueprint();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (nameField == null) return;

        ItemStack blueprintStack = handler.getSlot(1).getStack();
        boolean hasBlueprint = !blueprintStack.isEmpty();
        if (hasBlueprint && !hadBlueprintLoaded) {
            nameField.setText(blueprintStack.getName().getString());
        } else if (!hasBlueprint && hadBlueprintLoaded) {
            nameField.setText("Template");
        }
        hadBlueprintLoaded = hasBlueprint;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        // HandledScreen doesn't call this on its own; every concrete screen must trigger it itself.
        this.drawMouseoverTooltip(ctx, mx, my);
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mx, int my) {
        boolean stage2 = stage2Active();
        boolean hasBlankTemplate = handler.hasBlankTemplate();
        boolean hasBlueprint = handler.hasBlueprint();

        wMinusButton.visible = stage2;
        wPlusButton.visible  = stage2;
        hMinusButton.visible = stage2;
        hPlusButton.visible  = stage2;
        loadButton.visible   = hasBlueprint;
        fillButton.visible   = stage2 && mode == MODE_CELL_EDIT;
        backButton.visible   = stage2 && mode == MODE_CELL_EDIT;
        nameField.visible    = stage2;
        createButton.visible = hasBlankTemplate;
        saveButton.visible   = stage2;

        int bg = 0xFFC6C6C6;
        int brd = 0xFF555555;
        ctx.fill(x, y, x + backgroundWidth, y + backgroundHeight, brd);
        ctx.fill(x + 1, y + 1, x + backgroundWidth - 1, y + backgroundHeight - 1, bg);

        ctx.drawText(textRenderer, getTitle(), x + 8, y + 6, 0x404040, false);

        // Input slot backgrounds (items themselves are drawn by HandledScreen)
        drawSlotBackground(ctx,
            x + GlaziersBenchScreenHandler.TEMPLATE_SLOT_X,
            y + GlaziersBenchScreenHandler.TEMPLATE_SLOT_Y);
        drawSlotBackground(ctx,
            x + GlaziersBenchScreenHandler.BLUEPRINT_SLOT_X,
            y + GlaziersBenchScreenHandler.BLUEPRINT_SLOT_Y);

        // Player inventory + hotbar slot backgrounds (always visible)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBackground(ctx,
                    x + GlaziersBenchScreenHandler.INV_X + col * 18,
                    y + GlaziersBenchScreenHandler.INV_Y + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotBackground(ctx,
                x + GlaziersBenchScreenHandler.INV_X + col * 18,
                y + GlaziersBenchScreenHandler.HOTBAR_Y);
        }

        if (!hasBlankTemplate) {
            ctx.drawText(textRenderer, Text.translatable("gui.vitraux.insert_hint"),
                x + GlaziersBenchScreenHandler.TEMPLATE_SLOT_X + 50, y + 25, 0x404040, false);
        }
        if (!hasBlueprint) {
            ctx.drawText(textRenderer, Text.translatable("gui.vitraux.insert_blueprint_hint"),
                x + GlaziersBenchScreenHandler.TEMPLATE_SLOT_X + 50, y + 34, 0x707070, false);
        }

        if (!stage2) {
            ctx.fill(x + STAGE2_X0, y + STAGE2_Y0, x + STAGE2_X1, y + STAGE2_Y1, 0xFF999999);
            ctx.drawBorder(x + STAGE2_X0, y + STAGE2_Y0, STAGE2_X1 - STAGE2_X0, STAGE2_Y1 - STAGE2_Y0, 0xFF555555);
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("gui.vitraux.insert_hint"),
                x + (STAGE2_X0 + STAGE2_X1) / 2, y + (STAGE2_Y0 + STAGE2_Y1) / 2 - 4, 0xDDDDDD);
        } else {
            GlaziersBenchScreenHandler h = handler;
            String dimLabel = h.getCanvasWidth() + " x " + h.getCanvasHeight();
            ctx.drawText(textRenderer, dimLabel, x + EDIT_X, y + PAL_Y, 0x404040, false);

            drawPreviewPanel(ctx, mx, my);

            if (mode == MODE_CELL_EDIT && editCellCol >= 0) {
                drawEditorPanel(ctx, mx, my);
            } else {
                ctx.fill(x + EDIT_X, y + EDIT_Y,
                         x + EDIT_X + EDIT_SIZE, y + EDIT_Y + EDIT_SIZE, 0xFF888888);
                ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Click a cell"),
                    x + EDIT_X + EDIT_SIZE / 2, y + EDIT_Y + EDIT_SIZE / 2 - 4, 0xAAAAAA);
            }

            drawPalette(ctx, mx, my);
        }
    }

    @Override
    protected void drawForeground(DrawContext ctx, int mx, int my) {
        // suppress default title/inventory labels
    }

    // ── Preview panel ─────────────────────────────────────────────────────────

    private void drawPreviewPanel(DrawContext ctx, int mx, int my) {
        GlaziersBenchScreenHandler h = handler;
        int cw = h.getCanvasWidth();
        int ch = h.getCanvasHeight();
        byte[] canvas = h.getClientCanvas();
        int stride = cw * 16;

        int panelX = x + PREV_X;
        int panelY = y + PREV_Y;

        ctx.fill(panelX - 1, panelY - 1, panelX + PREV_SIZE + 1, panelY + PREV_SIZE + 1, 0xFF555555);
        ctx.fill(panelX, panelY, panelX + PREV_SIZE, panelY + PREV_SIZE, 0xFF777777);

        int cellPx = PREV_SIZE / Math.max(cw, ch);

        int hovCol = -1, hovRow = -1;
        int rMx = mx - panelX, rMy = my - panelY;
        if (rMx >= 0 && rMy >= 0 && rMx < PREV_SIZE && rMy < PREV_SIZE) {
            int hc = rMx / cellPx;
            int hr = rMy / cellPx;
            if (hc < cw && hr < ch) {
                hovCol = hc;
                hovRow = hr;
            }
        }

        for (int row = 0; row < ch; row++) {
            for (int col = 0; col < cw; col++) {
                int sx = panelX + col * cellPx;
                int sy = panelY + row * cellPx;

                drawCheckerboard(ctx, sx, sy, cellPx, cellPx);

                float scale = (float) cellPx / 16;
                for (int py = 0; py < 16; py++) {
                    for (int px = 0; px < 16; px++) {
                        int gx = col * 16 + px;
                        int gy = row * 16 + py;
                        byte idx = canvas[gy * stride + gx];
                        int argb = paletteArgb(idx);
                        if ((argb >>> 24) == 0) continue;
                        int rx = sx + Math.round(px * scale);
                        int ry = sy + Math.round(py * scale);
                        int rw = Math.max(1, (int) Math.ceil(scale));
                        ctx.fill(rx, ry, rx + rw, ry + rw, argb);
                    }
                }

                if (col == hovCol && row == hovRow) {
                    for (int g = 0; g <= 16; g++) {
                        int gx2 = sx + Math.round(g * scale);
                        int gy2 = sy + Math.round(g * scale);
                        ctx.fill(gx2, sy, gx2 + 1, sy + cellPx, 0x44000000);
                        ctx.fill(sx, gy2, sx + cellPx, gy2 + 1, 0x44000000);
                    }
                    ctx.drawBorder(sx - 1, sy - 1, cellPx + 2, cellPx + 2, 0xFFFFFFFF);
                } else {
                    ctx.drawBorder(sx, sy, cellPx, cellPx, 0xFF404040);
                }

                if (col == editCellCol && row == editCellRow) {
                    ctx.drawBorder(sx, sy, cellPx, cellPx, 0xFFFFAA00);
                }
            }
        }
    }

    // ── Editor panel ──────────────────────────────────────────────────────────

    private void drawEditorPanel(DrawContext ctx, int mx, int my) {
        GlaziersBenchScreenHandler h = handler;
        int cw = h.getCanvasWidth();
        int ch = h.getCanvasHeight();
        byte[] canvas = h.getClientCanvas();
        int stride = cw * 16;

        int panelX = x + EDIT_X;
        int panelY = y + EDIT_Y;

        // Neighbor reference strips (drawn outside the editor border)
        drawNeighborStrips(ctx, canvas, stride, cw, ch, panelX, panelY);

        // Panel border
        ctx.fill(panelX - 1, panelY - 1, panelX + EDIT_SIZE + 1, panelY + EDIT_SIZE + 1, 0xFF555555);

        // Pixels
        for (int py = 0; py < 16; py++) {
            for (int px = 0; px < 16; px++) {
                int gx = editCellCol * 16 + px;
                int gy = editCellRow * 16 + py;
                byte idx = canvas[gy * stride + gx];

                int sx = panelX + px * EDIT_PX;
                int sy = panelY + py * EDIT_PX;

                drawCheckerboard(ctx, sx, sy, EDIT_PX, EDIT_PX);
                int argb = paletteArgb(idx);
                if ((argb >>> 24) != 0) {
                    ctx.fill(sx, sy, sx + EDIT_PX, sy + EDIT_PX, argb);
                }
                ctx.drawBorder(sx, sy, EDIT_PX, EDIT_PX, 0x60000000);
            }
        }

        // Hover highlight (suppressed during rect drag)
        int rMx = mx - panelX, rMy = my - panelY;
        boolean inEditor = rMx >= 0 && rMy >= 0 && rMx < EDIT_SIZE && rMy < EDIT_SIZE;
        if (!rectDragging && inEditor) {
            int hpx = rMx / EDIT_PX;
            int hpy = rMy / EDIT_PX;
            if (hpx < 16 && hpy < 16) {
                ctx.drawBorder(panelX + hpx * EDIT_PX, panelY + hpy * EDIT_PX,
                               EDIT_PX, EDIT_PX, 0xFFFFFFFF);
            }
        }

        // Rectangle selection preview
        if (rectDragging && rectStartPx >= 0) {
            int x0 = Math.min(rectStartPx, rectCurrPx);
            int x1 = Math.max(rectStartPx, rectCurrPx);
            int y0 = Math.min(rectStartPy, rectCurrPy);
            int y1 = Math.max(rectStartPy, rectCurrPy);
            int sx = panelX + x0 * EDIT_PX;
            int sy = panelY + y0 * EDIT_PX;
            int sw = (x1 - x0 + 1) * EDIT_PX;
            int sh = (y1 - y0 + 1) * EDIT_PX;
            // Semi-transparent fill in selected color
            int selColor = handler.getSelectedColor();
            int fillArgb = (PALETTE_ARGB[selColor] & 0x00FFFFFF) | 0x66000000;
            ctx.fill(sx, sy, sx + sw, sy + sh, fillArgb);
            ctx.drawBorder(sx, sy, sw, sh, 0xFFFFFFFF);
        }
    }

    // ── Neighbor reference strips ─────────────────────────────────────────────

    private void drawNeighborStrips(DrawContext ctx, byte[] canvas, int stride, int cw, int ch,
                                    int panelX, int panelY) {
        // TOP: last row (py=15) of the cell above
        if (editCellRow > 0) {
            int gy = (editCellRow - 1) * 16 + 15;
            int sy = panelY - NEIGHBOR_PX - 1;
            ctx.fill(panelX, sy, panelX + EDIT_SIZE, sy + NEIGHBOR_PX, 0xFF2A2A2A);
            for (int px = 0; px < 16; px++) {
                int gx = editCellCol * 16 + px;
                int argb = paletteArgb(canvas[gy * stride + gx]);
                if ((argb >>> 24) != 0)
                    ctx.fill(panelX + px * EDIT_PX, sy,
                             panelX + px * EDIT_PX + EDIT_PX, sy + NEIGHBOR_PX,
                             (argb & 0x00FFFFFF) | 0xC0000000);
            }
        }

        // BOTTOM: first row (py=0) of the cell below
        if (editCellRow < ch - 1) {
            int gy = (editCellRow + 1) * 16;
            int sy = panelY + EDIT_SIZE + 1;
            ctx.fill(panelX, sy, panelX + EDIT_SIZE, sy + NEIGHBOR_PX, 0xFF2A2A2A);
            for (int px = 0; px < 16; px++) {
                int gx = editCellCol * 16 + px;
                int argb = paletteArgb(canvas[gy * stride + gx]);
                if ((argb >>> 24) != 0)
                    ctx.fill(panelX + px * EDIT_PX, sy,
                             panelX + px * EDIT_PX + EDIT_PX, sy + NEIGHBOR_PX,
                             (argb & 0x00FFFFFF) | 0xC0000000);
            }
        }

        // LEFT: last column (px=15) of the cell to the left
        if (editCellCol > 0) {
            int gx = (editCellCol - 1) * 16 + 15;
            int sx = panelX - NEIGHBOR_PX - 1;
            ctx.fill(sx, panelY, sx + NEIGHBOR_PX, panelY + EDIT_SIZE, 0xFF2A2A2A);
            for (int py = 0; py < 16; py++) {
                int gy = editCellRow * 16 + py;
                int argb = paletteArgb(canvas[gy * stride + gx]);
                if ((argb >>> 24) != 0)
                    ctx.fill(sx, panelY + py * EDIT_PX,
                             sx + NEIGHBOR_PX, panelY + py * EDIT_PX + EDIT_PX,
                             (argb & 0x00FFFFFF) | 0xC0000000);
            }
        }

        // RIGHT: first column (px=0) of the cell to the right
        if (editCellCol < cw - 1) {
            int gx = (editCellCol + 1) * 16;
            int sx = panelX + EDIT_SIZE + 1;
            ctx.fill(sx, panelY, sx + NEIGHBOR_PX, panelY + EDIT_SIZE, 0xFF2A2A2A);
            for (int py = 0; py < 16; py++) {
                int gy = editCellRow * 16 + py;
                int argb = paletteArgb(canvas[gy * stride + gx]);
                if ((argb >>> 24) != 0)
                    ctx.fill(sx, panelY + py * EDIT_PX,
                             sx + NEIGHBOR_PX, panelY + py * EDIT_PX + EDIT_PX,
                             (argb & 0x00FFFFFF) | 0xC0000000);
            }
        }
    }

    // ── Palette ───────────────────────────────────────────────────────────────

    private void drawPalette(DrawContext ctx, int mx, int my) {
        int selected = handler.getSelectedColor();
        int slotW = SWATCH + SWATCH_GAP;

        for (int i = 0; i <= 16; i++) {
            int sx = x + PAL_X + i * slotW;
            int sy = y + PAL_Y;

            drawCheckerboard(ctx, sx, sy, SWATCH, SWATCH);
            ctx.fill(sx, sy, sx + SWATCH, sy + SWATCH, PALETTE_ARGB[i]);

            if (i == selected) {
                ctx.drawBorder(sx - 1, sy - 1, SWATCH + 2, SWATCH + 2, 0xFFFFFFFF);
            } else {
                ctx.drawBorder(sx, sy, SWATCH, SWATCH, 0xFF404040);
            }
        }
    }

    // ── Mouse interaction ─────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (nameField != null && nameField.visible && !nameField.isMouseOver(mx, my)) {
            nameField.setFocused(false);
        }

        boolean stage2 = stage2Active();

        if (button == 0 && stage2) {
            // Palette click
            int prx = (int) mx - x - PAL_X;
            int pry = (int) my - y - PAL_Y;
            if (prx >= 0 && pry >= 0 && pry < SWATCH) {
                int slot = prx / (SWATCH + SWATCH_GAP);
                if (slot >= 0 && slot <= 16) {
                    handler.setSelectedColor(slot);
                    return true;
                }
            }

            // Preview click (enter/paint cell)
            int prevRx = (int) mx - x - PREV_X;
            int prevRy = (int) my - y - PREV_Y;
            if (prevRx >= 0 && prevRy >= 0 && prevRx < PREV_SIZE && prevRy < PREV_SIZE) {
                GlaziersBenchScreenHandler h = handler;
                int cw = h.getCanvasWidth();
                int ch = h.getCanvasHeight();
                int cellPx = PREV_SIZE / Math.max(cw, ch);
                int col = prevRx / cellPx;
                int row = prevRy / cellPx;
                if (col < cw && row < ch) {
                    editCellCol = col;
                    editCellRow = row;
                    mode = MODE_CELL_EDIT;
                    rectDragging = false;
                }
                return true;
            }

            // Editor left-click (paint single pixel)
            if (mode == MODE_CELL_EDIT && editCellCol >= 0) {
                int erx = (int) mx - x - EDIT_X;
                int ery = (int) my - y - EDIT_Y;
                if (erx >= 0 && ery >= 0 && erx < EDIT_SIZE && ery < EDIT_SIZE) {
                    paintPixel(erx, ery);
                    return true;
                }
            }
        } else if (button == 1 && stage2) {
            // Editor right-click: start rectangle selection
            if (mode == MODE_CELL_EDIT && editCellCol >= 0) {
                int erx = (int) mx - x - EDIT_X;
                int ery = (int) my - y - EDIT_Y;
                if (erx >= 0 && ery >= 0 && erx < EDIT_SIZE && ery < EDIT_SIZE) {
                    rectStartPx = erx / EDIT_PX;
                    rectStartPy = ery / EDIT_PX;
                    rectCurrPx  = rectStartPx;
                    rectCurrPy  = rectStartPy;
                    rectDragging = true;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0 && stage2Active() && mode == MODE_CELL_EDIT && editCellCol >= 0) {
            int erx = (int) mx - x - EDIT_X;
            int ery = (int) my - y - EDIT_Y;
            if (erx >= 0 && ery >= 0 && erx < EDIT_SIZE && ery < EDIT_SIZE) {
                paintPixel(erx, ery);
                return true;
            }
        } else if (button == 1 && rectDragging) {
            int erx = (int) mx - x - EDIT_X;
            int ery = (int) my - y - EDIT_Y;
            rectCurrPx = Math.clamp(erx / EDIT_PX, 0, 15);
            rectCurrPy = Math.clamp(ery / EDIT_PX, 0, 15);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 1 && rectDragging) {
            applyRect();
            rectDragging = false;
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    // ── Keyboard interaction ─────────────────────────────────────────────────
    // The name field only consumes printable characters via charTyped, not raw
    // keyPresses (W/A/S/D/E etc.), so without this override those keys would
    // leak through to the player's movement / inventory-toggle keybindings
    // while typing and yank the player out of the GUI.

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField != null && nameField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.close();
                return true;
            }
            nameField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (nameField != null && nameField.isFocused()) {
            return nameField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    private void paintPixel(int erx, int ery) {
        int px = erx / EDIT_PX;
        int py = ery / EDIT_PX;
        if (px >= 16 || py >= 16) return;

        GlaziersBenchScreenHandler h = handler;
        int gx = editCellCol * 16 + px;
        int gy = editCellRow * 16 + py;
        byte color = (byte) h.getSelectedColor();

        int stride = h.getCanvasWidth() * 16;
        h.getClientCanvas()[gy * stride + gx] = color;

        ClientPlayNetworking.send(new SetPixelC2SPayload(handler.syncId, gx, gy, color));
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void applyRect() {
        if (editCellCol < 0 || rectStartPx < 0) return;
        GlaziersBenchScreenHandler h = handler;
        byte color = (byte) h.getSelectedColor();
        int stride = h.getCanvasWidth() * 16;
        byte[] canvas = h.getClientCanvas();

        int lx0 = Math.min(rectStartPx, rectCurrPx);
        int lx1 = Math.max(rectStartPx, rectCurrPx);
        int ly0 = Math.min(rectStartPy, rectCurrPy);
        int ly1 = Math.max(rectStartPy, rectCurrPy);

        int gx0 = editCellCol * 16 + lx0;
        int gy0 = editCellRow * 16 + ly0;
        int gx1 = editCellCol * 16 + lx1;
        int gy1 = editCellRow * 16 + ly1;

        // Optimistic local update
        for (int gy = gy0; gy <= gy1; gy++)
            for (int gx = gx0; gx <= gx1; gx++)
                canvas[gy * stride + gx] = color;

        ClientPlayNetworking.send(new FillRectC2SPayload(handler.syncId, gx0, gy0, gx1, gy1, color));
    }

    private void fillCell() {
        if (editCellCol < 0) return;
        GlaziersBenchScreenHandler h = handler;
        byte color = (byte) h.getSelectedColor();
        int stride = h.getCanvasWidth() * 16;
        byte[] canvas = h.getClientCanvas();
        int baseX = editCellCol * 16;
        int baseY = editCellRow * 16;
        for (int py = 0; py < 16; py++)
            for (int px = 0; px < 16; px++)
                canvas[(baseY + py) * stride + (baseX + px)] = color;
        ClientPlayNetworking.send(new FillCellC2SPayload(handler.syncId, editCellCol, editCellRow, color));
    }

    private void changeDim(int dw, int dh) {
        GlaziersBenchScreenHandler h = handler;
        int nw = Math.clamp(h.getCanvasWidth()  + dw, 1, GlaziersBenchBlockEntity.MAX_W);
        int nh = Math.clamp(h.getCanvasHeight() + dh, 1, GlaziersBenchBlockEntity.MAX_H);
        ClientPlayNetworking.send(new SetDimensionsC2SPayload(handler.syncId, nw, nh));
    }

    private void createTemplate() {
        if (!handler.hasBlankTemplate()) return;
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Template";
        ClientPlayNetworking.send(new CreateTemplateC2SPayload(handler.syncId, name));
    }

    private void saveDesign() {
        if (!stage2Active()) return;
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Blueprint";
        ClientPlayNetworking.send(new SaveDesignC2SPayload(handler.syncId, name));
    }

    private void loadDesign() {
        if (!handler.hasBlueprint()) return;
        ClientPlayNetworking.send(new LoadBlueprintC2SPayload(handler.syncId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void drawSlotBackground(DrawContext ctx, int sx, int sy) {
        ctx.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF8B8B8B);
        ctx.fill(sx, sy, sx + 16, sy + 16, 0xFF373737);
    }

    private void drawCheckerboard(DrawContext ctx, int sx, int sy, int sw, int sh) {
        ctx.fill(sx, sy, sx + sw, sy + sh, 0xFFAAAAAA);
        int hx = sw / 2, hy = sh / 2;
        if (hx > 0 && hy > 0) {
            ctx.fill(sx,      sy,      sx + hx, sy + hy, 0xFF888888);
            ctx.fill(sx + hx, sy + hy, sx + sw, sy + sh, 0xFF888888);
        }
    }

    private static int[] buildPalette() {
        int[] p = new int[17];
        DyeColor[] colors = DyeColor.values();
        for (int i = 0; i < 16; i++) {
            p[i] = 0xFF000000 | (colors[i].getEntityColor() & 0x00FFFFFF);
        }
        p[16] = 0x00000000; // transparent
        return p;
    }

    private int paletteArgb(byte idx) {
        int i = idx & 0xFF;
        return (i < PALETTE_ARGB.length) ? PALETTE_ARGB[i] : 0x00000000;
    }
}

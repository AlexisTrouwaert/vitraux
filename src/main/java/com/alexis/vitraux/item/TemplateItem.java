package com.alexis.vitraux.item;

import com.alexis.vitraux.block.BlankVitrauxPaneBlock;
import com.alexis.vitraux.block.CustomVitrauxBlock;
import com.alexis.vitraux.block.entity.CustomVitrauxBlockEntity;
import com.alexis.vitraux.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class TemplateItem extends Item {

    public static final String NBT_WIDTH  = "TemplateWidth";
    public static final String NBT_HEIGHT = "TemplateHeight";
    public static final String NBT_PIXELS = "TemplatePixels";

    public TemplateItem(Settings settings) {
        super(settings);
    }

    /** Write canvas data into an ItemStack using the custom data component. */
    public static void writeData(ItemStack stack, int width, int height, byte[] pixels) {
        NbtCompound nbt = new NbtCompound();
        nbt.putByte(NBT_WIDTH,  (byte) width);
        nbt.putByte(NBT_HEIGHT, (byte) height);
        nbt.putByteArray(NBT_PIXELS, pixels);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public record CanvasData(int width, int height, byte[] pixels) {}

    /** Read canvas data back out of an ItemStack written by {@link #writeData}, or null if absent/invalid. */
    public static CanvasData readData(ItemStack stack) {
        NbtComponent comp = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = comp.copyNbt();
        if (nbt.isEmpty()) return null;

        int width  = nbt.getByte(NBT_WIDTH)  & 0xFF;
        int height = nbt.getByte(NBT_HEIGHT) & 0xFF;
        byte[] pixels = nbt.getByteArray(NBT_PIXELS);
        if (width == 0 || height == 0 || pixels.length != width * 16 * height * 16) return null;

        return new CanvasData(width, height, pixels);
    }

    /** Appends a "W x H cells" tooltip line so designs stay distinguishable even when sharing a name. */
    public static void appendCanvasTooltip(ItemStack stack, List<Text> tooltip) {
        CanvasData data = readData(stack);
        if (data != null) {
            tooltip.add(Text.translatable("tooltip.vitraux.canvas_size", data.width(), data.height())
                .formatted(Formatting.GRAY));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        appendCanvasTooltip(stack, tooltip);
    }

    /**
     * BFS through connected BlankVitrauxPaneBlocks from `origin`.
     * Assigns each one a CustomVitrauxBlock with the appropriate pixel slice.
     */
    public static void applyTemplate(ItemStack stack, World world, BlockPos origin) {
        NbtComponent comp = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = comp.copyNbt();
        if (nbt.isEmpty()) return;

        int templateW = nbt.getByte(NBT_WIDTH)  & 0xFF;
        int templateH = nbt.getByte(NBT_HEIGHT) & 0xFF;
        byte[] pixels = nbt.getByteArray(NBT_PIXELS);
        int expectedLen = templateW * 16 * templateH * 16;
        if (pixels.length != expectedLen || templateW == 0 || templateH == 0) return;

        // BFS to collect connected blank panes
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        List<BlockPos> blanks = new ArrayList<>();
        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            if (world.getBlockState(cur).getBlock() instanceof BlankVitrauxPaneBlock) {
                blanks.add(cur);
                for (Direction dir : Direction.values()) {
                    BlockPos next = cur.offset(dir);
                    if (!visited.contains(next)) {
                        visited.add(next);
                        queue.add(next);
                    }
                }
            }
        }

        if (blanks.isEmpty()) return;

        // Compute bounding box for 2D projection
        int minX = blanks.stream().mapToInt(BlockPos::getX).min().orElse(0);
        int maxX = blanks.stream().mapToInt(BlockPos::getX).max().orElse(0);
        int minY = blanks.stream().mapToInt(BlockPos::getY).min().orElse(0);
        int maxY = blanks.stream().mapToInt(BlockPos::getY).max().orElse(0);
        int minZ = blanks.stream().mapToInt(BlockPos::getZ).min().orElse(0);
        int maxZ = blanks.stream().mapToInt(BlockPos::getZ).max().orElse(0);

        boolean alongX = (maxX - minX) >= (maxZ - minZ);

        for (BlockPos pos : blanks) {
            int col = alongX ? (pos.getX() - minX) : (pos.getZ() - minZ);
            int row = maxY - pos.getY();

            if (col >= templateW || row >= templateH) continue;

            int cellPxX = col * 16;
            int cellPxY = row * 16;
            int templateStride = templateW * 16;
            byte[] cellPixels = new byte[16 * 16];
            for (int py = 0; py < 16; py++) {
                System.arraycopy(
                    pixels, (cellPxY + py) * templateStride + cellPxX,
                    cellPixels, py * 16,
                    16
                );
            }

            BlockState oldState = world.getBlockState(pos);
            BlockState newState = ModBlocks.CUSTOM_VITRAUX.getDefaultState()
                .with(PaneBlock.NORTH, oldState.get(PaneBlock.NORTH))
                .with(PaneBlock.SOUTH, oldState.get(PaneBlock.SOUTH))
                .with(PaneBlock.EAST,  oldState.get(PaneBlock.EAST))
                .with(PaneBlock.WEST,  oldState.get(PaneBlock.WEST));
            world.setBlockState(pos, newState, 3);

            if (world.getBlockEntity(pos) instanceof CustomVitrauxBlockEntity be) {
                be.setPixels(cellPixels);
                be.markDirty();
                world.updateListeners(pos, newState, newState, 3);
            }
        }

        stack.decrement(1);
    }
}

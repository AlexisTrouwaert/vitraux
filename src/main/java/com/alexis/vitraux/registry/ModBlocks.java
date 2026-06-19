package com.alexis.vitraux.registry;

import com.alexis.vitraux.VitrauxMod;
import com.alexis.vitraux.block.BlankVitrauxPaneBlock;
import com.alexis.vitraux.block.CustomVitrauxBlock;
import com.alexis.vitraux.block.GlazedVitrauxPaneBlock;
import com.alexis.vitraux.block.GlaziersBenchBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

public class ModBlocks {

    public static final Map<DyeColor, GlazedVitrauxPaneBlock> VITRAUX_BY_COLOR = new EnumMap<>(DyeColor.class);

    public static final BlankVitrauxPaneBlock BLANK_VITRAUX;
    public static final CustomVitrauxBlock    CUSTOM_VITRAUX;
    public static final GlaziersBenchBlock    GLAZIERS_BENCH;

    static {
        for (DyeColor color : DyeColor.values()) {
            VITRAUX_BY_COLOR.put(
                color,
                new GlazedVitrauxPaneBlock(color, AbstractBlock.Settings.copy(Blocks.WHITE_STAINED_GLASS_PANE))
            );
        }
        BLANK_VITRAUX  = new BlankVitrauxPaneBlock(AbstractBlock.Settings.copy(Blocks.GLASS_PANE).nonOpaque());
        CUSTOM_VITRAUX = new CustomVitrauxBlock(AbstractBlock.Settings.copy(Blocks.GLASS_PANE).nonOpaque());
        GLAZIERS_BENCH = new GlaziersBenchBlock(AbstractBlock.Settings.copy(Blocks.CRAFTING_TABLE));
    }

    public static void register() {
        for (DyeColor color : DyeColor.values()) {
            Registry.register(
                Registries.BLOCK,
                Identifier.of(VitrauxMod.MOD_ID, color.getName() + "_vitraux"),
                VITRAUX_BY_COLOR.get(color)
            );
        }
        Registry.register(Registries.BLOCK, Identifier.of(VitrauxMod.MOD_ID, "blank_vitraux"),  BLANK_VITRAUX);
        Registry.register(Registries.BLOCK, Identifier.of(VitrauxMod.MOD_ID, "custom_vitraux"), CUSTOM_VITRAUX);
        Registry.register(Registries.BLOCK, Identifier.of(VitrauxMod.MOD_ID, "glaziers_bench"), GLAZIERS_BENCH);
    }
}

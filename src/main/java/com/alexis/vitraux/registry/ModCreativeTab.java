package com.alexis.vitraux.registry;

import com.alexis.vitraux.VitrauxMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class ModCreativeTab {

    public static final ItemGroup VITRAUX_GROUP = FabricItemGroup.builder()
        .icon(() -> new ItemStack(ModBlocks.VITRAUX_BY_COLOR.get(DyeColor.CYAN)))
        .displayName(Text.translatable("itemGroup.vitraux.vitraux"))
        .entries((context, entries) -> {
            for (DyeColor color : DyeColor.values()) {
                entries.add(ModBlocks.VITRAUX_BY_COLOR.get(color));
            }
            entries.add(ModBlocks.BLANK_VITRAUX);
            entries.add(ModBlocks.GLAZIERS_BENCH);
            entries.add(ModItems.PINCETTE);
            entries.add(ModItems.BLANK_TEMPLATE);
            entries.add(ModItems.TEMPLATE);
            entries.add(ModItems.BLUEPRINT);
        })
        .build();

    public static void register() {
        Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(VitrauxMod.MOD_ID, "vitraux"),
            VITRAUX_GROUP
        );
    }
}

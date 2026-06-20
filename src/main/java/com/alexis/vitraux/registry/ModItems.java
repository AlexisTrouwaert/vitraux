package com.alexis.vitraux.registry;

import com.alexis.vitraux.VitrauxMod;
import com.alexis.vitraux.item.BlueprintItem;
import com.alexis.vitraux.item.TemplateItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item PINCETTE = new Item(new Item.Settings().maxCount(1));
    public static final Item TEMPLATE = new TemplateItem(new Item.Settings().maxCount(16));
    public static final Item BLANK_TEMPLATE = new Item(new Item.Settings());
    public static final Item BLUEPRINT = new BlueprintItem(new Item.Settings().maxCount(16));

    public static void register() {
        // Coloured vitraux block items
        for (DyeColor color : DyeColor.values()) {
            var block = ModBlocks.VITRAUX_BY_COLOR.get(color);
            Registry.register(
                Registries.ITEM,
                Identifier.of(VitrauxMod.MOD_ID, color.getName() + "_vitraux"),
                new BlockItem(block, new Item.Settings())
            );
        }

        // v2 block items
        Registry.register(Registries.ITEM, Identifier.of(VitrauxMod.MOD_ID, "blank_vitraux"),
            new BlockItem(ModBlocks.BLANK_VITRAUX, new Item.Settings()));
        Registry.register(Registries.ITEM, Identifier.of(VitrauxMod.MOD_ID, "glaziers_bench"),
            new BlockItem(ModBlocks.GLAZIERS_BENCH, new Item.Settings()));

        // Tools / items
        Registry.register(Registries.ITEM, Identifier.of(VitrauxMod.MOD_ID, "pincette"), PINCETTE);
        Registry.register(Registries.ITEM, Identifier.of(VitrauxMod.MOD_ID, "template"), TEMPLATE);
        Registry.register(Registries.ITEM, Identifier.of(VitrauxMod.MOD_ID, "blank_template"), BLANK_TEMPLATE);
        Registry.register(Registries.ITEM, Identifier.of(VitrauxMod.MOD_ID, "blueprint"), BLUEPRINT);
    }
}

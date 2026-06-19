package com.alexis.vitraux.registry;

import com.alexis.vitraux.VitrauxMod;
import com.alexis.vitraux.block.entity.CustomVitrauxBlockEntity;
import com.alexis.vitraux.block.entity.GlaziersBenchBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static BlockEntityType<CustomVitrauxBlockEntity> CUSTOM_VITRAUX;
    public static BlockEntityType<GlaziersBenchBlockEntity> GLAZIERS_BENCH;

    public static void register() {
        CUSTOM_VITRAUX = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(VitrauxMod.MOD_ID, "custom_vitraux"),
            BlockEntityType.Builder.create(CustomVitrauxBlockEntity::new, ModBlocks.CUSTOM_VITRAUX).build()
        );
        GLAZIERS_BENCH = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(VitrauxMod.MOD_ID, "glaziers_bench"),
            BlockEntityType.Builder.create(GlaziersBenchBlockEntity::new, ModBlocks.GLAZIERS_BENCH).build()
        );
    }
}

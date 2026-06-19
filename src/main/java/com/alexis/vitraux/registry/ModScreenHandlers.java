package com.alexis.vitraux.registry;

import com.alexis.vitraux.VitrauxMod;
import com.alexis.vitraux.screen.GlaziersBenchScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

    public static ScreenHandlerType<GlaziersBenchScreenHandler> GLAZIERS_BENCH;

    public static void register() {
        GLAZIERS_BENCH = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(VitrauxMod.MOD_ID, "glaziers_bench"),
            new ScreenHandlerType<>(GlaziersBenchScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );
    }
}

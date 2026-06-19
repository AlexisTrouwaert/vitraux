package com.alexis.vitraux.client;

import com.alexis.vitraux.VitrauxMod;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Runtime detection of optional shader mods and entry-point for future
 * shader-based coloured light projection.
 *
 * Phase 2 (current): detection only, fallback to vanilla DyeColor tint.
 * Phase 3 (planned): inject custom Iris shader program for per-pixel
 *   coloured shadow projection matching the glazed terracotta pattern.
 */
public class VitrauxShaderCompat {

    public static final boolean IRIS_PRESENT    = FabricLoader.getInstance().isModLoaded("iris");
    public static final boolean SODIUM_PRESENT  = FabricLoader.getInstance().isModLoaded("sodium");
    public static final boolean OCULUS_PRESENT  = FabricLoader.getInstance().isModLoaded("oculus"); // Forge port of Iris

    public static void init() {
        if (IRIS_PRESENT) {
            VitrauxMod.LOGGER.info("[Vitraux] Iris détecté — projection lumière colorée activée (Phase 3 à venir).");
        } else if (OCULUS_PRESENT) {
            VitrauxMod.LOGGER.info("[Vitraux] Oculus détecté — projection lumière colorée activée (Phase 3 à venir).");
        } else {
            VitrauxMod.LOGGER.info("[Vitraux] Aucun shader mod détecté — fallback sur la teinte DyeColor vanilla.");
        }

        if (SODIUM_PRESENT) {
            VitrauxMod.LOGGER.info("[Vitraux] Sodium détecté — rendu optimisé disponible.");
        }
    }

    /** Returns true if any shader pipeline capable of coloured light is active. */
    public static boolean hasShaderSupport() {
        return IRIS_PRESENT || OCULUS_PRESENT;
    }
}

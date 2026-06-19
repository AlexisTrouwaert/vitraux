package com.alexis.vitraux.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Extracts the bundled vitraux_shadows shader pack from the mod JAR into
 * the game's shaderpacks/ directory so Iris can find it.
 *
 * The shader files live at:
 *   src/main/resources/vitraux_shaders/...
 * which are included in the JAR root as:
 *   vitraux_shaders/...
 *
 * On first run (or whenever pack.mcmeta changes) they are written to:
 *   <gameDir>/shaderpacks/vitraux_shadows/
 *
 * The user then selects "vitraux_shadows" in Iris's shader settings.
 */
public final class VitrauxShaderInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger("vitraux/shader-installer");

    private static final String RESOURCE_ROOT  = "/vitraux_shaders";
    private static final String SHADERPACK_NAME = "vitraux_shadows";

    private static final String[] SHADER_FILES = {
        "pack.mcmeta",
        "shaders/shaders.properties",
        "shaders/shadow.vsh",
        "shaders/shadow.fsh",
        "shaders/gbuffers_terrain.vsh",
        "shaders/gbuffers_terrain.fsh",
        "shaders/gbuffers_water.vsh",
        "shaders/gbuffers_water.fsh",
    };

    private VitrauxShaderInstaller() {}

    public static void installIfNeeded() {
        if (!VitrauxIrisCompat.isIrisPresent()) return;

        Path gameDir     = FabricLoader.getInstance().getGameDir();
        Path shaderPack  = gameDir.resolve("shaderpacks").resolve(SHADERPACK_NAME);

        try {
            // Check if pack.mcmeta already matches what we'd write
            Path mcmeta = shaderPack.resolve("pack.mcmeta");
            if (Files.exists(mcmeta)) {
                byte[] existing = Files.readAllBytes(mcmeta);
                byte[] bundled  = readResource(RESOURCE_ROOT + "/pack.mcmeta");
                if (java.util.Arrays.equals(existing, bundled)) {
                    LOGGER.info("[Vitraux] Shader pack already up-to-date at {}", shaderPack);
                    return;
                }
            }

            // Extract all shader files
            for (String file : SHADER_FILES) {
                Path dest = shaderPack.resolve(file.replace("/", FileSystems.getDefault().getSeparator()));
                Files.createDirectories(dest.getParent());
                byte[] data = readResource(RESOURCE_ROOT + "/" + file);
                Files.write(dest, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            LOGGER.info("[Vitraux] Shader pack installed to {}", shaderPack);
            LOGGER.info("[Vitraux] Select '{}' in Iris shader settings to enable coloured shadows.", SHADERPACK_NAME);

        } catch (IOException e) {
            LOGGER.error("[Vitraux] Failed to install shader pack: {}", e.getMessage());
        }
    }

    private static byte[] readResource(String path) throws IOException {
        try (InputStream in = VitrauxShaderInstaller.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Resource not found in JAR: " + path);
            }
            return in.readAllBytes();
        }
    }
}

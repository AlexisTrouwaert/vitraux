package com.alexis.vitraux.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Thin reflection wrapper around the Iris API.
 * This lets the mod compile and run without Iris on the class-path;
 * all Iris calls go through reflection and silently no-op when Iris
 * is absent or the API shape changes.
 */
public final class VitrauxIrisCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("vitraux/iris");

    private static boolean irisPresent = false;
    private static Object  irisApi     = null;   // IrisApi instance

    private static Method methodIsShaderPackInUse    = null;
    private static Method methodIsRenderingShadow    = null;
    private static Method methodGetSunPathRotation   = null;

    private VitrauxIrisCompat() {}

    public static void init() {
        irisPresent = FabricLoader.getInstance().isModLoaded("iris");
        if (!irisPresent) {
            LOGGER.info("[Vitraux] Iris absent — coloured shadows disabled");
            return;
        }
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Method getInstance = apiClass.getMethod("getInstance");
            irisApi = getInstance.invoke(null);

            methodIsShaderPackInUse  = apiClass.getMethod("isShaderPackInUse");
            methodIsRenderingShadow  = apiClass.getMethod("isRenderingShadowPass");
            methodGetSunPathRotation = apiClass.getMethod("getSunPathRotation");

            LOGGER.info("[Vitraux] Iris API v0 found — coloured shadows active");
        } catch (Exception e) {
            LOGGER.warn("[Vitraux] Iris present but API reflection failed: {}", e.getMessage());
            irisApi = null;
        }
    }

    public static boolean isShaderPackInUse() {
        if (irisApi == null) return false;
        try {
            return Boolean.TRUE.equals(methodIsShaderPackInUse.invoke(irisApi));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRenderingShadowPass() {
        if (irisApi == null) return false;
        try {
            return Boolean.TRUE.equals(methodIsRenderingShadow.invoke(irisApi));
        } catch (Exception e) {
            return false;
        }
    }

    public static float getSunPathRotation() {
        if (irisApi == null) return 0f;
        try {
            Object result = methodGetSunPathRotation.invoke(irisApi);
            return result instanceof Number ? ((Number) result).floatValue() : 0f;
        } catch (Exception e) {
            return 0f;
        }
    }

    public static boolean isIrisPresent() {
        return irisPresent;
    }
}

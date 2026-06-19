package com.alexis.vitraux;

import com.alexis.vitraux.network.ModNetworking;
import com.alexis.vitraux.registry.*;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VitrauxMod implements ModInitializer {

    public static final String MOD_ID = "vitraux";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();
        ModCreativeTab.register();
        ModNetworking.registerServerReceivers();
        LOGGER.info("Vitraux mod initialized.");
    }
}

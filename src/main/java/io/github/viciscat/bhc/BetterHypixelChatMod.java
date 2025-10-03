package io.github.viciscat.bhc;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterHypixelChatMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("BetterHypixelChat");
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.9";
    public static final String NAMESPACE = "better_hypixel_chat";

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");
        //? if >=1.21.9 {
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(ResourceType.CLIENT_RESOURCES)
                .registerReloader(AddVanillaFont.ID, new AddVanillaFont());
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(ResourceType.CLIENT_RESOURCES)
                .addReloaderOrdering(
                        net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Client.FONTS,
                        AddVanillaFont.ID
                );
        //?} else {
        /*net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new AddVanillaFont());
        *///?}
    }

    /**
     * Adapts to the {@link Identifier} changes introduced in 1.21.
     */
    public static Identifier id(String path) {
        return Identifier.of(NAMESPACE, path);
    }
}
package io.github.viciscat.bhc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class BetterHypixelChatMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("BetterHypixelChat");
    public static final String VERSION = /*$ mod_version*/ "0.2.1";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.10";
    public static final String NAMESPACE = "better_hypixel_chat";

    private static boolean onHypixel = false;

    public static boolean isOnHypixel() {
        return onHypixel;
    }

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

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String serverAddress = (client.getCurrentServerEntry() != null) ? client.getCurrentServerEntry().address.toLowerCase(Locale.ENGLISH) : "";
            String serverBrand = (client.player != null && client.player.networkHandler != null && client.player.networkHandler.getBrand() != null) ? client.player.networkHandler.getBrand() : "";

            onHypixel = serverAddress.contains("hypixel.net") || serverAddress.contains("hypixel.io") || serverBrand.contains("Hypixel BungeeCord");
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, sender) -> onHypixel = false);
        // fix for chat patches' ChatLog.hideRecentMessages changing visibleMessages and breaking the reference map.
        if (FabricLoader.getInstance().isModLoaded("chatpatches")) {
            Identifier identifier = id("chatpatches_compat");
            ClientPlayConnectionEvents.JOIN.register(identifier, (handler, sender, client) -> {
                if (isOnHypixel()) client.inGameHud.getChatHud().reset();
            });
            ClientPlayConnectionEvents.JOIN.addPhaseOrdering(Event.DEFAULT_PHASE, identifier); // run after chat patches
        }
    }

    /**
     * Adapts to the {@link Identifier} changes introduced in 1.21.
     */
    public static Identifier id(String path) {
        return Identifier.of(NAMESPACE, path);
    }
}
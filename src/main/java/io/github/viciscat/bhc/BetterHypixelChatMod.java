package io.github.viciscat.bhc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
//? if >=1.21.11 {
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
*///?}
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class BetterHypixelChatMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("BetterHypixelChat");
    public static final String VERSION = /*$ mod_version*/ "0.3.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.11";
    public static final String NAMESPACE = "better_hypixel_chat";

    public static final Collection<LineRendererProvider> PROVIDERS = Util.make(new ArrayList<>(), list -> {
        list.add(new CenteredLine.Provider());
        list.add(new SeparationLine.Provider());
        list.add(new CenteredSeparationLine.Provider());
    });

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
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloader(AddVanillaFont.ID, new AddVanillaFont());
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .addReloaderOrdering(
                        net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Client.FONTS,
                        AddVanillaFont.ID
                );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String serverAddress = (client.getCurrentServer() != null) ? client.getCurrentServer().ip.toLowerCase(Locale.ENGLISH) : "";
            String serverBrand = (client.player != null && client.player.connection != null && client.player.connection.serverBrand() != null) ? client.player.connection.serverBrand() : "";

            onHypixel = serverAddress.contains("hypixel.net") || serverAddress.contains("hypixel.io") || serverBrand.contains("Hypixel BungeeCord");
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, sender) -> onHypixel = false);
        // fix for chat patches' ChatLog.hideRecentMessages changing visibleMessages and breaking the reference map.
        if (FabricLoader.getInstance().isModLoaded("chatpatches")) {
            Identifier identifier = id("chatpatches_compat");
            ClientPlayConnectionEvents.JOIN.register(identifier, (handler, sender, client) -> {
                if (isOnHypixel()) client.gui.getChat().rescaleChat();
            });
            ClientPlayConnectionEvents.JOIN.addPhaseOrdering(Event.DEFAULT_PHASE, identifier); // run after chat patches
        }
    }

    /**
     * Adapts to the {@link Identifier} changes introduced in 1.21.
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }
}
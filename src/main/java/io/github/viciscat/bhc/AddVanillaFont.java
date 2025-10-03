package io.github.viciscat.bhc;

import com.google.common.collect.Lists;
import io.github.viciscat.bhc.mixin.FontManagerAccessor;
import io.github.viciscat.bhc.mixin.LifecycledResourceManagerImplAccessor;
import io.github.viciscat.bhc.mixin.MinecraftClientAccessor;
import io.github.viciscat.bhc.mixin.ReloadableResourceManagerImplAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//? if >=1.21.9 {
public class AddVanillaFont implements ResourceReloader {
//?} else {
/*public class AddVanillaFont implements net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener {
*///?}

    public static final Identifier ID = BetterHypixelChatMod.id("vanilla_font");

    //? if <1.21.9 {
    /*@Override
    public Identifier getFabricId() {
        return ID;
    }
    *///?}

    @Override
    //? if >=1.21.9 {
    public CompletableFuture<Void> reload(Store store, Executor prepareExecutor, Synchronizer synchronizer, Executor applyExecutor) {
        ResourceManager manager = store.getResourceManager();
    //?} else {
    /*public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
    *///?}
        return CompletableFuture.supplyAsync(() -> loadVanillaIndex(manager), prepareExecutor)
                .thenCompose(synchronizer::whenPrepared)
                .thenAcceptAsync(index -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    MinecraftClientAccessor mcAcc = (MinecraftClientAccessor) mc;
                    FontManagerAccessor fontManager = (FontManagerAccessor) mcAcc.getFontManager();
                    index.fontSets().forEach((id, fonts) -> {
                        id = id.withPrefixedPath("vanilla_");
                        //? if >=1.21.9 {
                        FontStorage fontStorage = new FontStorage(new net.minecraft.client.font.GlyphBaker(mc.getTextureManager(), id));
                        //?} else {
                        /*FontStorage fontStorage = new FontStorage(mc.getTextureManager(), id);
                        *///?}
                        Set<FontFilterType> filters = FontManagerAccessor.invokeGetActiveFilters(mc.options);
                        filters.remove(FontFilterType.UNIFORM);
                        fontStorage.setFonts(Lists.reverse(fonts), filters);
                        fontManager.getFontStorages().put(id, fontStorage);
                    });
                    fontManager.getFonts().addAll(index.allProviders());
                }, applyExecutor);
    }

    private FontManager.ProviderIndex loadVanillaIndex(ResourceManager manager) {
        LifecycledResourceManagerImpl impl;
        if (manager instanceof LifecycledResourceManagerImpl) {
            impl = (LifecycledResourceManagerImpl) manager;
        } else if (manager instanceof ReloadableResourceManagerImpl reloadableResourceManager) {
            LifecycledResourceManager lifecycledResourceManager = ((ReloadableResourceManagerImplAccessor) reloadableResourceManager).getActiveManager();
            if (lifecycledResourceManager instanceof LifecycledResourceManagerImpl) {
                impl = (LifecycledResourceManagerImpl) lifecycledResourceManager;
            } else throw new RuntimeException("Invalid resource manager");
        } else  {
            throw new RuntimeException("Invalid resource manager");
        }
        MinecraftClientAccessor mc = (MinecraftClientAccessor) MinecraftClient.getInstance();
        FontManagerAccessor fontManager = (FontManagerAccessor) mc.getFontManager();
        return fontManager.invokeLoadIndex(((LifecycledResourceManagerImplAccessor) impl).getSubManagers().get("minecraft"), Runnable::run).join();
    }

    //? if <1.21.9 {
    /*@Override
    public java.util.Collection<Identifier> getFabricDependencies() {
        return java.util.Collections.singleton(net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys.FONTS);
    }
    *///?}
}

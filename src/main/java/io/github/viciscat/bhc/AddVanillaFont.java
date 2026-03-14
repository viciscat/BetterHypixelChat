package io.github.viciscat.bhc;

import com.google.common.collect.Lists;
import io.github.viciscat.bhc.mixin.FontManagerAccessor;
import io.github.viciscat.bhc.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AddVanillaFont implements PreparableReloadListener {

    public static final Identifier ID = BetterHypixelChatMod.id("vanilla_font");

    @Override
    public @NotNull CompletableFuture<Void> reload(@NotNull SharedState store, @NotNull Executor prepareExecutor, PreparationBarrier synchronizer, @NotNull Executor applyExecutor) {
        return CompletableFuture.supplyAsync(this::loadVanillaIndex, prepareExecutor)
                .thenCompose(synchronizer::wait)
                .thenAcceptAsync(index -> {
                    Minecraft mc = Minecraft.getInstance();
                    MinecraftAccessor mcAcc = (MinecraftAccessor) mc;
                    FontManagerAccessor fontManager = (FontManagerAccessor) mcAcc.getFontManager();
                    index.fontSets().forEach((id, fonts) -> {
                        id = id.withPrefix("vanilla_");
                        FontSet fontStorage = new FontSet(new net.minecraft.client.gui.font.GlyphStitcher(mc.getTextureManager(), id));
                        Set<FontOption> filters = FontManagerAccessor.invokeGetFontOptions(mc.options);
                        filters.remove(FontOption.UNIFORM);
                        fontStorage.reload(Lists.reverse(fonts), filters);
                        fontManager.getFontSets().put(id, fontStorage);
                    });
                    fontManager.getProvidersToClose().addAll(index.allProviders());
                }, applyExecutor);
    }

    private FontManager.Preparation loadVanillaIndex() {
        VanillaPackResources pack = Minecraft.getInstance().getVanillaPackResources();
        FallbackResourceManager resourceManager = new FallbackResourceManager(PackType.CLIENT_RESOURCES, "minecraft");
        resourceManager.push(pack);
        MinecraftAccessor mc = (MinecraftAccessor) Minecraft.getInstance();
        FontManagerAccessor fontManager = (FontManagerAccessor) mc.getFontManager();
        return fontManager.invokePrepare(resourceManager, Runnable::run).join();
    }
}

package io.github.viciscat.bhc.mixin;

import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.Options;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(FontManager.class)
public interface FontManagerAccessor {

    @Invoker("prepare")
    CompletableFuture<FontManager.Preparation> invokePrepare(ResourceManager resourceManager, Executor executor);

    @Accessor("fontSets")
    Map<Identifier, FontSet> getFontSets();

    @Accessor("providersToClose")
    List<GlyphProvider> getProvidersToClose();

    @Invoker("getFontOptions")
    static Set<FontOption> invokeGetFontOptions(Options options) {
        throw new UnsupportedOperationException();
    }
}

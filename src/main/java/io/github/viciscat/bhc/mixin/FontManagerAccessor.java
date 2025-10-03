package io.github.viciscat.bhc.mixin;

import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.option.GameOptions;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
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

    @Invoker("loadIndex")
    CompletableFuture<FontManager.ProviderIndex> invokeLoadIndex(ResourceManager resourceManager, Executor executor);

    @Accessor("fontStorages")
    Map<Identifier, FontStorage> getFontStorages();

    @Accessor("fonts")
    List<Font> getFonts();

    @Invoker("getActiveFilters")
    static Set<FontFilterType> invokeGetActiveFilters(GameOptions options) {
        throw new UnsupportedOperationException();
    }
}

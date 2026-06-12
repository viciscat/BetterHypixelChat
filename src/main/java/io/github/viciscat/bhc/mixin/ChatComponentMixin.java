package io.github.viciscat.bhc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.viciscat.bhc.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"ShadowModifiers", "LocalMayUseName"}) // Remove LocalMayUseName once 1.21.11 is dropped
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements ChatComponentAccess {

    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Shadow
    abstract int getWidth();

    @Shadow
    abstract double getScale();

    @Unique
    private final Map<GuiMessage.Line, CustomLineRenderer> customLineRenderers = new Reference2ObjectOpenHashMap<>();

    @Inject(method = "clearMessages", at = @At("HEAD"))
    private void clear(boolean history, CallbackInfo ci) {
        customLineRenderers.clear();
    }

    //? if >=26.1 {
    @WrapOperation(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/GuiMessage;splitLines(Lnet/minecraft/client/gui/Font;I)Ljava/util/List;"))
    //? } else {
    /*@WrapOperation(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage;splitLines(Lnet/minecraft/client/gui/Font;I)Ljava/util/List;"))
    *///? }
    private List<FormattedCharSequence> processCustomLines(GuiMessage instance, Font font, int maxWidth, Operation<List<FormattedCharSequence>> original, @Share("custom_line_renderers") LocalRef<List<@Nullable CustomLineRenderer>> renderersRef, @Local(argsOnly = true) GuiMessage message) {
        if (!BetterHypixelChatMod.isOnHypixel()) return original.call(instance, font, maxWidth);
        // split all inner linebreaks and remove chat formattings.
        TextBuilder builder = new TextBuilder();
        message.content().getVisualOrderText().accept(builder);
        Collection<MutableComponent> texts = builder.getTexts();

        List<FormattedCharSequence> result = new ArrayList<>(texts.size());
        List<@Nullable CustomLineRenderer> renderers = new ArrayList<>(texts.size());

        for (MutableComponent text : texts) {
            String string = text.getString();
            String trimmed = string.trim();
            List<LineRendererProvider.Line> lines = null;
            for (LineRendererProvider provider : BetterHypixelChatMod.PROVIDERS) {
                Optional<List<LineRendererProvider.Line>> list = provider.getLineRenderers(font, text, string, trimmed, getScaledWidth());
                if (list.isPresent()) {
                    lines = list.get();
                    break;
                }
            }

            if (lines == null) {
                List<FormattedCharSequence> orderedTexts = ComponentRenderUtils.wrapComponents(text, getScaledWidth(), font);
                result.addAll(orderedTexts);
                for (int i = 0; i < orderedTexts.size(); i++) renderers.add(null);
            } else {
                for (LineRendererProvider.Line line : lines) {
                    result.add(line.text());
                    renderers.add(line.renderer());
                }
            }

        }
        if (result.size() != renderers.size()) throw new IllegalStateException("Result and Renderer lists aren't the same size!");
        renderersRef.set(renderers);
        return result;
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void addCustomLineRenderer(CallbackInfo ci, @Share("custom_line_renderers") LocalRef<@Nullable List<@Nullable CustomLineRenderer>> renderersRef, @Local(ordinal = 1) int i) {
        if (!BetterHypixelChatMod.isOnHypixel()) return;
        List<@Nullable CustomLineRenderer> renderers = renderersRef.get();
        if (renderers == null || i >= renderers.size()) {
            BetterHypixelChatMod.LOGGER.warn("Custom line renderer not found or is too small! {}", trimmedMessages.getFirst());
            return;
        }
        CustomLineRenderer renderer = renderers.get(i);
        if (renderer != null) {
            customLineRenderers.put(trimmedMessages.getFirst(), renderer);
        }
    }

    @WrapOperation(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;removeLast()Ljava/lang/Object;"))
    private <E> E removeCustomLineRenderer(List<E> instance, Operation<E> original) {
        GuiMessage.Line visible = (GuiMessage.Line) instance.getLast();
        customLineRenderers.remove(visible);
        return original.call(instance);
    }

    @Inject(method = "refreshTrimmedMessages", at = @At("HEAD"))
    private void clearOnRefresh(CallbackInfo ci) {
        customLineRenderers.clear();
    }

    @Unique
    private int getScaledWidth() {
        return Mth.floor(getWidth() / getScale());
    }

    @Override
    public int bhc$getScaledWidth() {
        return getScaledWidth();
    }

    @Override
    public @Nullable CustomLineRenderer bhc$getCustomLineRenderer(GuiMessage.Line line) {
        return customLineRenderers.get(line);
    }

    @Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1")
    public static class Renderer {
        @Shadow(aliases = "field_63873")
        @Final
        ChatComponent this$0;

        @WrapOperation(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleMessage(IFLnet/minecraft/util/FormattedCharSequence;)Z"))
        private boolean drawMessage(ChatComponent.ChatGraphicsAccess instance, int textTop, float alpha, FormattedCharSequence formattedCharSequence, Operation<Boolean> original, @Local(ordinal = 1) int entryBottom, @Local(ordinal = 2) int entryTop, @Local(argsOnly = true) GuiMessage.Line line) {
            ChatComponentAccess access = (ChatComponentAccess) this$0;
            GuiGraphicsSupplier supplier = GuiGraphicsSupplier.of(instance);
            ChatGraphics chatGraphics = new ChatGraphics(instance);
            CustomLineRenderer lineRenderer = access.bhc$getCustomLineRenderer(line);
            if (lineRenderer != null) {
                AtomicReference<@Nullable ScreenRectangle> previousScissor = new AtomicReference<>();
                supplier.bhc$applyParameters(p -> {
                    previousScissor.set(p.scissor());
                    return p.withScissor(0, access.bhc$getScaledWidth(), entryTop, entryBottom);
                });
                lineRenderer.render(chatGraphics, formattedCharSequence, 0, entryTop, access.bhc$getScaledWidth(), entryBottom - entryTop, textTop, alpha);
                supplier.bhc$applyParameters(p -> new ActiveTextCollector.Parameters(p.pose(), p.opacity(), previousScissor.get()));
                return chatGraphics.hovered;
            }
            return original.call(instance, textTop, alpha, formattedCharSequence);
        }
    }
}
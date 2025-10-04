package io.github.viciscat.bhc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.viciscat.bhc.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.*;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.BiConsumer;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    private int scrolledLines;

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract double getChatScale();

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private final Map<ChatHudLine.Visible, CustomLineRenderer> customLineRenderers = new Reference2ObjectOpenHashMap<>();

    @Inject(method = "clear", at = @At("HEAD"))
    private void clear(boolean clearHistory, CallbackInfo ci) {
        customLineRenderers.clear();
    }

    @Inject(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"))
    private void processCustomLines(ChatHudLine message, CallbackInfo ci, @Local LocalRef<List<OrderedText>> localRef, @Share("custom_line_renderers") LocalRef<List<CustomLineRenderer>> renderersRef) {
        if (!BetterHypixelChatMod.isOnHypixel()) return;
        // split all inner linebreaks and remove chat formattings.
        TextBuilder builder = new TextBuilder();
        message.content().asOrderedText().accept(builder);
        Collection<MutableText> texts = builder.getTexts();

        List<OrderedText> result = new ArrayList<>(texts.size());
        List<CustomLineRenderer> renderers = new ArrayList<>(texts.size());

        TextRenderer textRenderer = client.textRenderer;
        for (MutableText text : texts) {
            String string = text.getString();
            String trimmed = string.trim();
            // check if the text is all -
            if (!trimmed.isEmpty() && trimmed.chars().allMatch(c -> c == '-' || c == '—')) {
                result.add(text.asOrderedText());
                renderers.add(new SeparationLine(getFirstColor(text).map(color -> ColorHelper.fullAlpha(color.getRgb())).orElse(-1), 1));
            } else if (!trimmed.isEmpty() && trimmed.chars().allMatch(c -> c == '▬')) {
                result.add(text.asOrderedText());
                renderers.add(new SeparationLine(getFirstColor(text).map(color -> ColorHelper.fullAlpha(color.getRgb())).orElse(-1), 3));
            } else if (string.startsWith(" ") && !trimmed.isEmpty()) {
                // find last space in the string
                int firstNonSpaceChar;
                for (firstNonSpaceChar = 0; firstNonSpaceChar < string.length(); firstNonSpaceChar++) {
                    if (string.charAt(firstNonSpaceChar) != ' ') break;
                }
                MutableBoolean reachedText = new MutableBoolean(false);
                MutableText trimmedText = Text.empty(); // still has trailing spaces, shouldn't be an issue?
                text.visit((style, asString) -> {
                    if (reachedText.booleanValue()) {
                        trimmedText.append(Text.literal(asString).setStyle(style));
                    } else {
                        if (asString.isBlank()) return Optional.empty();
                        trimmedText.append(Text.literal(asString.stripLeading()).setStyle(style));
                        reachedText.setTrue();
                    }
                    return Optional.empty();
                }, Style.EMPTY);

                String s = string.substring(0, firstNonSpaceChar);
                int leadingSpace = textRenderer.getWidth(withFont(Text.literal(s)));
                int textWidth = textRenderer.getWidth(withFont(trimmedText));
                int abs = Math.abs(ChatConstants.DEFAULT_WIDTH / 2 - (leadingSpace + textWidth / 2));
                //System.out.println("trimmedText: " + withFont(trimmedText));
                //System.out.println("leadingSpace: " +  leadingSpace + " textWidth: " + textWidth + " abs: " + abs);
                if (abs < 6) { // if true this text is supposed to be centered!
                    List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(trimmedText, getScaledWidth(), textRenderer);
                    for (OrderedText orderedText : list) {
                        result.add(orderedText);
                        renderers.add(new CenteredLine(orderedText));
                    }
                } else {
                    List<OrderedText> orderedTexts = ChatMessages.breakRenderedChatMessageLines(message.content(), getScaledWidth(), textRenderer);
                    result.addAll(orderedTexts);
                    for (int i = 0; i < orderedTexts.size(); i++) renderers.add(null);
                }

            } else {
                List<OrderedText> orderedTexts = ChatMessages.breakRenderedChatMessageLines(message.content(), getScaledWidth(), textRenderer);
                result.addAll(orderedTexts);
                for (int i = 0; i < orderedTexts.size(); i++) renderers.add(null);
            }
        }
        if (result.size() != renderers.size()) throw new IllegalStateException("Result and Renderer lists aren't the same size!");
        localRef.set(result);
        renderersRef.set(renderers);
    }

    @Inject(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER))
    private void addCustomLineRenderer(CallbackInfo ci, @Share("custom_line_renderers") LocalRef<List<CustomLineRenderer>> renderersRef, @Local(ordinal = 1) int i) {
        CustomLineRenderer renderer = renderersRef.get().get(i);
        if (renderer != null) {
            customLineRenderers.put(visibleMessages.getFirst(), renderer);
        }
    }

    @WrapOperation(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(I)Ljava/lang/Object;"))
    private <E> E removeCustomLineRenderer(List<E> instance, int i, Operation<E> original) {
        ChatHudLine.Visible visible = (ChatHudLine.Visible) instance.get(i);
        customLineRenderers.remove(visible);
        return original.call(instance, i);
    }

    @WrapOperation(method = "getTextStyleAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextHandler;getStyleAt(Lnet/minecraft/text/OrderedText;I)Lnet/minecraft/text/Style;"))
    private Style customLineStyle(TextHandler instance, OrderedText text, int x, Operation<Style> original, @Local ChatHudLine.Visible visible) {
        CustomLineRenderer renderer = customLineRenderers.get(visible);
        if (renderer == null) return original.call(instance, text, x);
        return renderer.getStyleAt(client.textRenderer, 0, x, getScaledWidth());
    }

    @Unique
    private Optional<TextColor> getFirstColor(MutableText text) {
        return text.visit((style, asString) -> Optional.ofNullable(style.getColor()), Style.EMPTY);
    }

    @Inject(method = "refresh", at = @At("HEAD"))
    private void clearOnRefresh(CallbackInfo ci) {
        customLineRenderers.clear();
    }

    //? if >=1.21.6 {
    @WrapOperation(method = "method_71991", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)V"))
    //?} else {
    /*@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
    *///?}
    private /*? if <=1.21.5 {*/ /*int *//*?} else {*/ void /*?}*/
    drawCustomRenderers(DrawContext instance, TextRenderer textRenderer, OrderedText orderedText, int x, int y, int color, Operation<Void> original,
                        /*? if <=1.21.5 {*//*@Local(ordinal = 12)*//*?} else {*/@Local(argsOnly = true, ordinal = 4)/*?}*/ int index) {
        index += this.scrolledLines;
        CustomLineRenderer renderer = customLineRenderers.get(visibleMessages.get(index));
        if (renderer == null) original.call(instance, textRenderer, orderedText, x, y, color);
        else {
            renderer.render(textRenderer, instance, orderedText, x, y, color, getScaledWidth());
        }
        //? if <=1.21.5
        /*return index;*/
    }

    @Unique
    private int getScaledWidth() {
        return MathHelper.floor(getWidth() / getChatScale());
    }

    @Unique
    MutableText withFont(Text text) {
        //? if >=1.21.9 {
        return Text.empty().setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(ChatConstants.VANILLA_FONT))).append(text);
        //?} else {
        /*return Text.empty().setStyle(Style.EMPTY.withFont(ChatConstants.VANILLA_FONT)).append(text);
        *///?}
    }
}
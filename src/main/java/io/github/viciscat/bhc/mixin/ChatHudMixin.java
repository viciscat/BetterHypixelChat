package io.github.viciscat.bhc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.viciscat.bhc.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
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

    @Shadow
    public abstract boolean isChatFocused();

    @Shadow
    private boolean hasUnreadNewMessages;

    @Shadow
    public abstract void scroll(int scroll);

    @Unique
    private final Map<ChatHudLine.Visible, CustomLineRenderer> customLineRenderers = new Reference2ObjectOpenHashMap<>();

    @Inject(method = "clear", at = @At("HEAD"))
    private void clear(boolean clearHistory, CallbackInfo ci) {
        customLineRenderers.clear();
    }

    @Inject(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"), cancellable = true)
    private void customLine(ChatHudLine message, CallbackInfo ci, @Local LocalRef<List<OrderedText>> localRef) {
        if (!BetterHypixelChatMod.isOnHypixel()) return;
        localRef.set(List.of());
        // split all inner linebreaks and remove chat formattings.
        TextBuilder builder = new TextBuilder();
        message.content().asOrderedText().accept(builder);
        Collection<MutableText> texts = builder.getTexts();

        TextRenderer textRenderer = client.textRenderer;
        for (MutableText text : texts) {
            String string = text.getString();
            String trimmed = string.trim();
            // check if the text is all -
            if (!trimmed.isEmpty() && trimmed.chars().allMatch(c -> c == '-' || c == '—')) {
                ChatHudLine.Visible visible = new ChatHudLine.Visible(message.creationTick(), text.asOrderedText(), message.indicator(), true);
                visibleMessages.addFirst(visible);
                customLineRenderers.put(visible, new SeparationLine(getFirstColor(text).map(color -> ColorHelper.fullAlpha(color.getRgb())).orElse(-1), 1));
            } else if (!trimmed.isEmpty() && trimmed.chars().allMatch(c -> c == '▬')) {
                ChatHudLine.Visible visible = new ChatHudLine.Visible(message.creationTick(), text.asOrderedText(), message.indicator(), true);
                visibleMessages.addFirst(visible);
                customLineRenderers.put(visible, new SeparationLine(getFirstColor(text).map(color -> ColorHelper.fullAlpha(color.getRgb())).orElse(-1), 3));
            } else if (string.startsWith(" ") && !trimmed.isEmpty()) {
                // find last space in the string
                int i;
                for (i = 0; i < string.length(); i++) {
                    if (string.charAt(i) != ' ') break;
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

                String s = string.substring(0, i);
                int leadingSpace = textRenderer.getWidth(withFont(Text.literal(s)));
                int textWidth = textRenderer.getWidth(withFont(trimmedText));
                int abs = Math.abs(ChatConstants.DEFAULT_WIDTH / 2 - (leadingSpace + textWidth / 2));
                //System.out.println("trimmedText: " + withFont(trimmedText));
                //System.out.println("leadingSpace: " +  leadingSpace + " textWidth: " + textWidth + " abs: " + abs);
                if (abs < 6) { // if true this text is supposed to be centered!
                    List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(trimmedText, getScaledWidth(), textRenderer);
                    for (int j = 0; j < list.size(); j++) {
                        OrderedText orderedText = list.get(j);
                        if (isChatFocused() && this.scrolledLines > 0) {
                            hasUnreadNewMessages = true;
                            scroll(1);
                        }
                        ChatHudLine.Visible visible = new ChatHudLine.Visible(message.creationTick(), orderedText, message.indicator(), j == list.size() - 1);
                        this.visibleMessages.addFirst(visible);
                        customLineRenderers.put(visible, new CenteredLine(orderedText));
                    }
                } else {
                    addNormalText(message, textRenderer);
                }

            } else {
                addNormalText(message, textRenderer);
            }
        }

        while (this.visibleMessages.size() > 100) {
            ChatHudLine.Visible visible = this.visibleMessages.removeLast();
            customLineRenderers.remove(visible);
        }

        ci.cancel();
    }

    @Unique
    private Optional<TextColor> getFirstColor(MutableText text) {
        return text.visit((style, asString) -> Optional.ofNullable(style.getColor()), Style.EMPTY);
    }

    @Inject(method = "refresh", at = @At("HEAD"))
    private void clearOnRefresh(CallbackInfo ci) {
        customLineRenderers.clear();
    }

    @Unique
    private void addNormalText(ChatHudLine message, TextRenderer textRenderer) {
        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(message.content(), getScaledWidth(), textRenderer);
        for (int j = 0; j < list.size(); j++) {
            OrderedText orderedText = list.get(j);
            if (isChatFocused() && this.scrolledLines > 0) {
                hasUnreadNewMessages = true;
                scroll(1);
            }
            this.visibleMessages.addFirst(new ChatHudLine.Visible(message.creationTick(), orderedText, message.indicator(), j == list.size() - 1));
        }
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